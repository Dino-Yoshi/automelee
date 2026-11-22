package automelee;

/** The off-hand's cooled strength, computed the way the main hand's is. */
final class OffhandCharge {

  private OffhandCharge() {}

  static float of(int ticksSinceLastSwing, float period) {
    float charge = (ticksSinceLastSwing + 0.5F) / period;
    return Math.max(0.0F, Math.min(1.0F, charge));
  }
}
