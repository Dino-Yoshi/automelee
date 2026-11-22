package automelee;

import meldexun.reachfix.hook.client.EntityRendererHook;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;

/**
 * Reads the entity under the crosshair with ReachFix's reach rules. Loaded only when ReachFix is
 * present.
 */
final class ReachFixTargets {

  private ReachFixTargets() {}

  static Entity pointed(Minecraft mc, EntityPlayer player, EnumHand hand) {
    RayTraceResult hit =
        EntityRendererHook.pointedObject(
            mc.getRenderViewEntity(), player, hand, mc.world, mc.getRenderPartialTicks());
    return hit != null && hit.typeOfHit == RayTraceResult.Type.ENTITY ? hit.entityHit : null;
  }
}
