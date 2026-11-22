package automelee;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.Loader;

final class TargetFinder {

  private static final String REACHFIX_ID = "reachfix";

  private TargetFinder() {}

  static Entity find(Minecraft mc, EntityPlayer player) {
    return Loader.isModLoaded(REACHFIX_ID)
        ? ReachFixTargets.pointed(mc, player)
        : vanillaTarget(mc);
  }

  private static Entity vanillaTarget(Minecraft mc) {
    RayTraceResult hit = mc.objectMouseOver;
    return hit != null && hit.typeOfHit == RayTraceResult.Type.ENTITY ? hit.entityHit : null;
  }
}
