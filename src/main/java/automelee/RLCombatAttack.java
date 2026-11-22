package automelee;

import bettercombat.mod.client.handler.EventHandlersClient;
import bettercombat.mod.network.PacketHandler;
import bettercombat.mod.network.PacketMainhandAttack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;

/**
 * Attacks the way RLCombat does on a main-hand click. RLCombat applies its own damage, sweep, crit,
 * sound and animation on its packet path, so the vanilla attack is not enough. Loaded only when
 * RLCombat is present.
 */
final class RLCombatAttack {

  private RLCombatAttack() {}

  static void attack(EntityPlayer player, Entity target) {
    Entity mover =
        player.isRiding() && player.getRidingEntity() != null ? player.getRidingEntity() : player;
    PacketHandler.instance.sendToServer(
        new PacketMainhandAttack(
            target.getEntityId(), mover.motionX, mover.motionY, mover.motionZ));
    player.attackTargetEntityWithCurrentItem(target);
    player.resetCooldown();
    player.swingArm(EnumHand.MAIN_HAND);
    EventHandlersClient.resetMainhandAnimationCooldown(player.getCooldownPeriod());
    EventHandlersClient.betterCombatMainhand.initiateAnimation();
  }
}
