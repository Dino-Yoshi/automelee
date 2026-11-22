package automelee;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.Loader;

/** Sends one attack through the path that matches the loaded mods. */
final class AttackDispatcher {

  private static final String RLCOMBAT_ID = "bettercombatmod";
  private static final AttackPath PATH = AttackPath.select(Loader.isModLoaded(RLCOMBAT_ID));

  private AttackDispatcher() {}

  static void attack(Minecraft mc, EntityPlayer player, Entity target) {
    if (PATH == AttackPath.RLCOMBAT) {
      RLCombatAttack.attack(player, target);
      return;
    }
    mc.playerController.attackEntity(player, target);
    player.swingArm(EnumHand.MAIN_HAND);
  }
}
