package automelee;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Runs at the end of each client tick, after vanilla has handled input. */
public final class ClientTickHandler {

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
    // The off-hand step goes here, after the main hand.
  }

  private static void attackWith(Minecraft mc, EntityPlayerSP player, EnumHand hand) {
    Entity target = TargetFinder.find(mc, player, hand);
    Snapshot snapshot = snapshot(mc, player, hand, target);
    if (AttackDecision.shouldAttack(snapshot)) {
      AttackDispatcher.attack(mc, player, target);
    }
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
