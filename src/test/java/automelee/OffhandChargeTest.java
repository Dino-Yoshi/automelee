package automelee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OffhandChargeTest {

  // A sword's cooldown period is fractional, so the boundary is exact at 12 ticks.
  private static final float PERIOD = 12.5F;

  @Test
  void chargeIsExactlyFullAtThePeriodBoundary() {
    assertEquals(1.0F, OffhandCharge.of(12, PERIOD));
  }

  @Test
  void chargeIsBelowFullOneTickEarlier() {
    assertTrue(OffhandCharge.of(11, PERIOD) < 1.0F);
  }

  @Test
  void chargeIsClampedToZeroBeforeTheSwing() {
    assertEquals(0.0F, OffhandCharge.of(-1, PERIOD));
  }

  @Test
  void chargeIsClampedToOneLongAfterTheSwing() {
    assertEquals(1.0F, OffhandCharge.of(100, PERIOD));
  }
}
