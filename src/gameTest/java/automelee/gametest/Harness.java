package automelee.gametest;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;

/**
 * Bridges the scenario thread and the game threads. Scenario code runs off the client thread and
 * reads or changes game state through onClient / onServer, so every access happens on the thread
 * that owns that state.
 */
final class Harness {

  private static final long WAIT_MS = 10_000L;
  private static final int LOAD_TICKS = 1200;

  private final AtomicInteger ticks = new AtomicInteger();
  private final AttackLog attacks = new AttackLog(ticks);
  private volatile int groundY;
  private volatile UUID playerId;

  AttackLog attacks() {
    return attacks;
  }

  /** Called from the client END tick. */
  void onClientTick() {
    ticks.incrementAndGet();
    Minecraft mc = Minecraft.getMinecraft();
    if (mc.world != null) {
      mc.inGameHasFocus = true;
    }
  }

  int tick() {
    return ticks.get();
  }

  int groundY() {
    return groundY;
  }

  void check(boolean ok, String message) {
    if (!ok) {
      throw new ScenarioFailed(message);
    }
  }

  // Thread bridge.

  <T> T onClient(Callable<T> task) throws Exception {
    return await(Minecraft.getMinecraft().addScheduledTask(task));
  }

  <T> T onServer(Callable<T> task) throws Exception {
    IntegratedServer server = Minecraft.getMinecraft().getIntegratedServer();
    if (server == null) {
      throw new ScenarioFailed("no integrated server running");
    }
    FutureTask<T> future = new FutureTask<>(task);
    server.addScheduledTask(future);
    return await(future);
  }

  private static <T> T await(Future<T> future) throws Exception {
    try {
      return future.get(WAIT_MS, TimeUnit.MILLISECONDS);
    } catch (ExecutionException e) {
      if (e.getCause() instanceof Exception) {
        throw (Exception) e.getCause();
      }
      throw e;
    }
  }

  void awaitTicks(int count) throws InterruptedException {
    int target = ticks.get() + count;
    long deadline = System.currentTimeMillis() + WAIT_MS;
    while (ticks.get() < target) {
      if (System.currentTimeMillis() > deadline) {
        throw new ScenarioFailed("the game stopped ticking");
      }
      Thread.sleep(2L);
    }
  }

  /** Polls the condition once per client tick, on the client or server thread. */
  void awaitUntil(String what, boolean onServerThread, Callable<Boolean> condition, int maxTicks)
      throws Exception {
    for (int i = 0; ; i++) {
      boolean met = Boolean.TRUE.equals(onServerThread ? onServer(condition) : onClient(condition));
      if (met) {
        return;
      }
      if (i >= maxTicks) {
        throw new ScenarioFailed("timed out waiting for " + what);
      }
      awaitTicks(1);
    }
  }

  // World setup.

  void launchWorld(String name) throws Exception {
    onClient(
        () -> {
          Minecraft mc = Minecraft.getMinecraft();
          deleteRecursively(new File(mc.gameDir, "saves/" + name));
          WorldSettings settings =
              new WorldSettings(1L, GameType.SURVIVAL, false, false, WorldType.FLAT);
          mc.launchIntegratedServer(name, name, settings);
          return null;
        });
  }

  void awaitWorld() throws Exception {
    awaitUntil(
        "the world to load",
        false,
        () -> {
          Minecraft mc = Minecraft.getMinecraft();
          return mc.world != null && mc.player != null && mc.getIntegratedServer() != null;
        },
        LOAD_TICKS);
    playerId = onClient(() -> Minecraft.getMinecraft().player.getUniqueID());
    awaitUntil("the server player", true, () -> serverPlayer() != null, 200);
  }

  void prepareWorld() throws Exception {
    onServer(
        () -> {
          WorldServer world = world();
          world.getGameRules().setOrCreateGameRule("doMobSpawning", "false");
          world.getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
          world.setWorldTime(6000L);
          groundY = world.getTopSolidOrLiquidBlock(new BlockPos(0, 0, 0)).getY();
          return null;
        });
    onClient(
        () -> {
          Minecraft.getMinecraft().gameSettings.renderDistanceChunks = 2;
          return null;
        });
  }

  /** Clears the arena, teleports the player to it, and releases all input. */
  void resetArena() throws Exception {
    releaseInputs();
    onServer(
        () -> {
          WorldServer world = world();
          for (Entity entity : new ArrayList<>(world.loadedEntityList)) {
            if (!(entity instanceof EntityPlayer)) {
              entity.setDead();
            }
          }
          for (BlockPos pos : arenaBlocks()) {
            world.setBlockToAir(pos);
          }
          EntityPlayerMP player = serverPlayer();
          player.inventory.clear();
          player.setHealth(player.getMaxHealth());
          player.connection.setPlayerLocation(0.5D, groundY, 0.5D, 0F, 0F);
          return null;
        });
    awaitUntil(
        "the player to reach the arena",
        false,
        () -> Minecraft.getMinecraft().player.getDistanceSq(0.5D, groundY, 0.5D) < 0.01D,
        100);
    setGameType(GameType.SURVIVAL);
    awaitSpectator(false);
    setFoodLevel(20);
  }

  /** Arena reset, then the item in the main hand and a full attack cooldown. */
  void prepare(Item item) throws Exception {
    resetArena();
    holdItem(item);
    awaitUntil(
        "the attack cooldown to fill",
        false,
        () -> Minecraft.getMinecraft().player.getCooledAttackStrength(0.5F) >= 1F,
        200);
  }

  void holdItem(Item item) throws Exception {
    onServer(
        () -> {
          EntityPlayerMP player = serverPlayer();
          player.inventory.setInventorySlotContents(0, new ItemStack(item));
          player.inventory.currentItem = 0;
          return null;
        });
    onClient(
        () -> {
          Minecraft.getMinecraft().player.inventory.currentItem = 0;
          return null;
        });
    awaitUntil(
        "the client to hold " + item.getRegistryName(),
        false,
        () -> Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() == item,
        100);
  }

  // Entities and blocks.

  int spawnPig() throws Exception {
    return onServer(
        () -> {
          WorldServer world = world();
          EntityPig pig = new EntityPig(world);
          pig.setLocationAndAngles(0.5D, groundY, 2.5D, 0F, 0F);
          pig.setNoAI(true);
          pig.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1000D);
          pig.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
          pig.setHealth(1000F);
          world.spawnEntity(pig);
          return pig.getEntityId();
        });
  }

  /** A pig two blocks ahead, with the crosshair on it. */
  int spawnAimedPig() throws Exception {
    int id = spawnPig();
    lookAt(new Vec3d(0.5D, groundY + 0.5D, 2.5D));
    awaitAimAtEntity(id);
    return id;
  }

  int spawnArmorStand() throws Exception {
    return onServer(
        () -> {
          WorldServer world = world();
          EntityArmorStand stand = new EntityArmorStand(world, 0.5D, groundY, 2.5D);
          world.spawnEntity(stand);
          return stand.getEntityId();
        });
  }

  /** A stone wall with an item frame on its near face, two blocks ahead. */
  int placeItemFrame() throws Exception {
    return onServer(
        () -> {
          WorldServer world = world();
          world.setBlockState(new BlockPos(0, groundY + 1, 3), Blocks.STONE.getDefaultState());
          EntityItemFrame frame =
              new EntityItemFrame(world, new BlockPos(0, groundY + 1, 2), EnumFacing.NORTH);
          if (!frame.onValidSurface()) {
            throw new ScenarioFailed("item frame has no valid surface");
          }
          world.spawnEntity(frame);
          return frame.getEntityId();
        });
  }

  BlockPos placeDirt() throws Exception {
    BlockPos pos = new BlockPos(0, groundY, 2);
    onServer(
        () -> {
          world().setBlockState(pos, Blocks.DIRT.getDefaultState());
          return null;
        });
    return pos;
  }

  Vec3d entityPosition(int entityId) throws Exception {
    return onServer(() -> world().getEntityByID(entityId).getPositionVector());
  }

  boolean isAlive(int entityId) throws Exception {
    return onServer(
        () -> {
          Entity entity = world().getEntityByID(entityId);
          return entity != null && !entity.isDead;
        });
  }

  void awaitBlockBroken(BlockPos pos, int maxTicks) throws Exception {
    awaitUntil("the block at " + pos + " to break", true, () -> world().isAirBlock(pos), maxTicks);
  }

  // Aiming.

  /** Turns the player to face the point, on both the server and the client. */
  void lookAt(Vec3d target) throws Exception {
    onServer(
        () -> {
          EntityPlayerMP player = serverPlayer();
          double eyeY = player.posY + player.getEyeHeight();
          double dx = target.x - player.posX;
          double dy = target.y - eyeY;
          double dz = target.z - player.posZ;
          double horizontal = Math.sqrt(dx * dx + dz * dz);
          float yaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90F;
          float pitch = (float) (-Math.atan2(dy, horizontal) * 180.0D / Math.PI);
          player.connection.setPlayerLocation(player.posX, player.posY, player.posZ, yaw, pitch);
          return null;
        });
  }

  void awaitAimAtEntity(int entityId) throws Exception {
    awaitUntil(
        "the crosshair on entity " + entityId,
        false,
        () -> {
          RayTraceResult hit = Minecraft.getMinecraft().objectMouseOver;
          return hit != null
              && hit.typeOfHit == RayTraceResult.Type.ENTITY
              && hit.entityHit != null
              && hit.entityHit.getEntityId() == entityId;
        },
        60);
  }

  void awaitAimAtBlock(BlockPos pos) throws Exception {
    awaitUntil(
        "the crosshair on block " + pos,
        false,
        () -> {
          RayTraceResult hit = Minecraft.getMinecraft().objectMouseOver;
          return hit != null
              && hit.typeOfHit == RayTraceResult.Type.BLOCK
              && pos.equals(hit.getBlockPos());
        },
        60);
  }

  void awaitAimAtNothing() throws Exception {
    awaitUntil(
        "the crosshair on open air",
        false,
        () -> {
          RayTraceResult hit = Minecraft.getMinecraft().objectMouseOver;
          return hit == null || hit.typeOfHit == RayTraceResult.Type.MISS;
        },
        60);
  }

  // Input and observation.

  void holdAttack(boolean down) throws Exception {
    onClient(
        () -> {
          KeyBinding.setKeyBindState(attackKey(), down);
          return null;
        });
  }

  /** Releases attack and returns how many hits landed since the given tick. */
  int releaseAttack(int sinceTick) throws Exception {
    return onClient(
        () -> {
          KeyBinding.setKeyBindState(attackKey(), false);
          return attacks.since(sinceTick).size();
        });
  }

  /** A single click: the key goes down and vanilla handles one press. Returns the tick. */
  int pressAttack() throws Exception {
    return onClient(
        () -> {
          int key = attackKey();
          KeyBinding.setKeyBindState(key, true);
          KeyBinding.onTick(key);
          return ticks.get();
        });
  }

  List<AttackLog.Hit> hitsFrom(int tick) throws Exception {
    return onClient(() -> attacks.since(tick));
  }

  private void releaseInputs() throws Exception {
    onClient(
        () -> {
          Minecraft mc = Minecraft.getMinecraft();
          KeyBinding.setKeyBindState(attackKey(), false);
          KeyBinding.setKeyBindState(useKey(), false);
          if (mc.currentScreen instanceof GuiInventory) {
            mc.displayGuiScreen(null);
          }
          return null;
        });
  }

  void giveOffhand(Item item) throws Exception {
    onServer(
        () -> {
          serverPlayer().inventory.offHandInventory.set(0, new ItemStack(item));
          return null;
        });
    awaitUntil(
        "the client to hold " + item.getRegistryName() + " in the off-hand",
        false,
        () -> Minecraft.getMinecraft().player.getHeldItemOffhand().getItem() == item,
        100);
  }

  void setFoodLevel(int level) throws Exception {
    onServer(
        () -> {
          serverPlayer().getFoodStats().setFoodLevel(level);
          return null;
        });
    onClient(
        () -> {
          Minecraft.getMinecraft().player.getFoodStats().setFoodLevel(level);
          return null;
        });
  }

  void setGameType(GameType type) throws Exception {
    onServer(
        () -> {
          serverPlayer().setGameType(type);
          return null;
        });
  }

  void awaitSpectator(boolean spectator) throws Exception {
    awaitUntil(
        spectator ? "the client to enter spectator mode" : "the client to leave spectator mode",
        false,
        () -> Minecraft.getMinecraft().player.isSpectator() == spectator,
        100);
  }

  /** Right-click. Pressing down also starts one use, the way a real click does. */
  void holdUse(boolean down) throws Exception {
    onClient(
        () -> {
          KeyBinding.setKeyBindState(useKey(), down);
          if (down) {
            KeyBinding.onTick(useKey());
          }
          return null;
        });
  }

  void openInventory() throws Exception {
    onClient(
        () -> {
          Minecraft mc = Minecraft.getMinecraft();
          mc.displayGuiScreen(new GuiInventory(mc.player));
          return null;
        });
  }

  void closeInventory() throws Exception {
    onClient(
        () -> {
          Minecraft.getMinecraft().displayGuiScreen(null);
          return null;
        });
  }

  void awaitInventory(boolean open) throws Exception {
    awaitUntil(
        open ? "the inventory to open" : "the inventory to close",
        false,
        () -> (Minecraft.getMinecraft().currentScreen instanceof GuiInventory) == open,
        60);
  }

  boolean isHandActive() throws Exception {
    return onClient(() -> Minecraft.getMinecraft().player.isHandActive());
  }

  boolean isBlocking() throws Exception {
    return onClient(() -> Minecraft.getMinecraft().player.isActiveItemStackBlocking());
  }

  boolean inventoryOpen() throws Exception {
    return onClient(() -> Minecraft.getMinecraft().currentScreen instanceof GuiInventory);
  }

  // Helpers.

  private static int attackKey() {
    return Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode();
  }

  private static int useKey() {
    return Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode();
  }

  private List<BlockPos> arenaBlocks() {
    return Arrays.asList(
        new BlockPos(0, groundY, 2),
        new BlockPos(0, groundY + 1, 2),
        new BlockPos(0, groundY + 1, 3));
  }

  private static WorldServer world() {
    return Minecraft.getMinecraft().getIntegratedServer().getWorld(0);
  }

  private EntityPlayerMP serverPlayer() {
    return Minecraft.getMinecraft().getIntegratedServer().getPlayerList().getPlayerByUUID(playerId);
  }

  private static void deleteRecursively(File file) {
    File[] children = file.listFiles();
    if (children != null) {
      for (File child : children) {
        deleteRecursively(child);
      }
    }
    file.delete();
  }
}
