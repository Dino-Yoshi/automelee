package automelee;

/** The client-side call that performs one automatic attack. */
enum AttackPath {
  VANILLA,
  RLCOMBAT;

  static AttackPath select(boolean rlcombatLoaded) {
    return rlcombatLoaded ? RLCOMBAT : VANILLA;
  }
}
