package automelee;

/** Decides whether an automatic attack should fire this tick. */
public final class AttackDecision {

  private AttackDecision() {}

  public static boolean shouldAttack(Snapshot s) {
    return s.meleeItem()
        && s.attackKeyHeld()
        && !s.usingItem()
        && !s.rowing()
        && !s.guiOpen()
        && !s.spectator()
        && s.alive()
        && s.cooledStrength() >= 1.0F
        && s.targetPresent()
        && s.targetLivingAndNotSelf();
  }
}
