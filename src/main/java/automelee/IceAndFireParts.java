package automelee;

import bettercombat.mod.util.inf.InFHandler;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.Loader;

/**
 * Resolves Ice and Fire multipart hitboxes to their dragon. The parts are not living entities, so
 * they would otherwise be skipped. RLCombat's helper is used because it already knows the part
 * classes. The helper is only called when RLCombat and Ice and Fire are both loaded.
 */
final class IceAndFireParts {

  private static final String RLCOMBAT_ID = "bettercombatmod";
  private static final String ICE_AND_FIRE_ID = "iceandfire";
  private static final boolean AVAILABLE =
      Loader.isModLoaded(RLCOMBAT_ID) && Loader.isModLoaded(ICE_AND_FIRE_ID);

  private IceAndFireParts() {}

  static Entity resolve(Entity target) {
    return TargetResolution.resolve(
        target, AVAILABLE, IceAndFireParts::isPart, IceAndFireParts::parentOf);
  }

  private static boolean isPart(Entity entity) {
    return InFHandler.isMultipart(entity);
  }

  private static Entity parentOf(Entity part) {
    return InFHandler.getMultipartParent(part);
  }
}
