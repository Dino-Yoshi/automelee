package automelee;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;

/** Sends one attack the way a vanilla click does. */
final class AttackDispatcher {

  private AttackDispatcher() {}

  static void attack(Minecraft mc, EntityPlayer player, Entity target) {
    mc.playerController.attackEntity(player, target);
    player.swingArm(EnumHand.MAIN_HAND);
  }
}
