package automelee;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Runs at the end of each client tick, after vanilla has handled input. */
public final class ClientTickHandler {

  private static final String RLCOMBAT_ID = "bettercombatmod";
  private static final boolean RLCOMBAT_LOADED = Loader.isModLoaded(RLCOMBAT_ID);

  /** True while the off-hand swing in progress was started by Auto Melee. */
  private boolean ownOffhandSwing;

  @SubscribeEvent
  public void onClientTick(TickEvent.ClientTickEvent event) {
    if (event.phase != TickEvent.Phase.END) {
      return;
    }
    Minecraft mc = Minecraft.getMinecraft();
    EntityPlayerSP player = mc.player;
    if (player == null || mc.world == null) {
      return;
    }

    attackWith(mc, player, EnumHand.MAIN_HAND);
    if (RLCOMBAT_LOADED && RLCombatOffhand.enabled()) {
      offhandAttack(mc, player);
    }
  }

  private static void attackWith(Minecraft mc, EntityPlayerSP player, EnumHand hand) {
    Entity target = TargetFinder.find(mc, player, hand);
    Snapshot snapshot = snapshot(mc, player, hand, target);
    if (AttackDecision.shouldAttack(snapshot)) {
      AttackDispatcher.attack(mc, player, target);
    }
  }

  private void offhandAttack(Minecraft mc, EntityPlayerSP player) {
    if (!isOffhandSwingInProgress(player)) {
      ownOffhandSwing = false;
    }
    Entity target = TargetFinder.find(mc, player, EnumHand.OFF_HAND);
    // Read the charge only once every other rule passes, since it swaps modifiers.
    if (!AttackDecision.shouldAttack(offhandSnapshot(mc, player, target, 1.0F))) {
      return;
    }
    float charge = RLCombatOffhand.charge(player);
    if (AttackDecision.shouldAttack(offhandSnapshot(mc, player, target, charge))) {
      RLCombatOffhand.attack(player, target);
      ownOffhandSwing = true;
    }
  }

  private Snapshot offhandSnapshot(
      Minecraft mc, EntityPlayerSP player, Entity target, float charge) {
    return Snapshot.builder()
        .meleeItem(RLCombatOffhand.allowsItem(player))
        .attackKeyHeld(isHeld(mc.gameSettings.keyBindUseItem))
        .usingItem(player.isHandActive())
        .rowing(player.isRowingBoat())
        .guiOpen(mc.currentScreen != null)
        .spectator(player.isSpectator())
        .alive(player.isEntityAlive())
        .cooledStrength(charge)
        .targetPresent(target != null)
        .targetLivingAndNotSelf(
            Targets.isValid(target, player) && RLCombatOffhand.allowsTarget(player, target))
        .clickSwingPlaying(isOffhandSwingInProgress(player) && !ownOffhandSwing)
        .build();
  }

  private static boolean isOffhandSwingInProgress(EntityPlayerSP player) {
    return player.isSwingInProgress && player.swingingHand == EnumHand.OFF_HAND;
  }

  private static Snapshot snapshot(
      Minecraft mc, EntityPlayerSP player, EnumHand hand, Entity target) {
    return Snapshot.builder()
        .meleeItem(MeleeItems.isMelee(player.getHeldItem(hand).getItem()))
        .attackKeyHeld(isHeld(mc.gameSettings.keyBindAttack))
        .usingItem(player.isHandActive())
        .rowing(player.isRowingBoat())
        .guiOpen(mc.currentScreen != null)
        .spectator(player.isSpectator())
        .alive(player.isEntityAlive())
        .cooledStrength(player.getCooledAttackStrength(0.5F))
        .targetPresent(target != null)
        .targetLivingAndNotSelf(Targets.isValid(target, player))
        .clickSwingPlaying(false)
        .build();
  }

  /**
   * RLCombat cancels the mouse event when a click lands on an entity, so the binding is never
   * marked as held. Read the physical button as well.
   */
  private static boolean isHeld(KeyBinding key) {
    return key.isKeyDown() || GameSettings.isKeyDown(key);
  }
}
