package automelee;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.EntityZombie;
import org.junit.jupiter.api.Test;

class TargetsTest {

  @Test
  void livingMobIsAValidTarget() {
    assertTrue(Targets.isLivingTargetType(EntityZombie.class));
  }

  @Test
  void itemFrameIsNotAValidTarget() {
    assertFalse(Targets.isLivingTargetType(EntityItemFrame.class));
  }

  @Test
  void armorStandIsNotAValidTarget() {
    assertFalse(Targets.isLivingTargetType(EntityArmorStand.class));
  }

  @Test
  void boatIsNotAValidTarget() {
    assertFalse(Targets.isLivingTargetType(EntityBoat.class));
  }
}
