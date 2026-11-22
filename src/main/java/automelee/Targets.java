package automelee;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;

public final class Targets {

  private Targets() {}

  public static boolean isValid(Entity target, EntityPlayer self) {
    return target != null && target != self && isLivingTargetType(target.getClass());
  }

  static boolean isLivingTargetType(Class<? extends Entity> type) {
    // Armor stands extend EntityLivingBase in vanilla but are decorations.
    return EntityLivingBase.class.isAssignableFrom(type)
        && !EntityArmorStand.class.isAssignableFrom(type);
  }
}
