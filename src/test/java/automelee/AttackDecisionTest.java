package automelee;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AttackDecisionTest {

  @Test
  void attacksWhenEveryRuleIsMet() {
    assertTrue(AttackDecision.shouldAttack(attackable().build()));
  }

  @Test
  void doesNotAttackWithoutMeleeItem() {
    assertFalse(AttackDecision.shouldAttack(attackable().meleeItem(false).build()));
  }

  @Test
  void doesNotAttackWhenKeyIsNotHeld() {
    assertFalse(AttackDecision.shouldAttack(attackable().attackKeyHeld(false).build()));
  }

  @Test
  void attacksAtExactlyFullStrength() {
    assertTrue(AttackDecision.shouldAttack(attackable().cooledStrength(1.0F).build()));
  }

  @Test
  void doesNotAttackJustBelowFullStrength() {
    float justBelow = Math.nextDown(1.0F);
    assertFalse(AttackDecision.shouldAttack(attackable().cooledStrength(justBelow).build()));
  }

  @Test
  void doesNotAttackWithoutTarget() {
    assertFalse(AttackDecision.shouldAttack(attackable().targetPresent(false).build()));
  }

  @Test
  void doesNotAttackWhenTargetIsNotLivingOrIsSelf() {
    assertFalse(AttackDecision.shouldAttack(attackable().targetLivingAndNotSelf(false).build()));
  }

  /** A snapshot where every rule would attack, so each test can break exactly one. */
  private static Snapshot.Builder attackable() {
    return Snapshot.builder()
        .meleeItem(true)
        .attackKeyHeld(true)
        .cooledStrength(1.0F)
        .targetPresent(true)
        .targetLivingAndNotSelf(true);
  }
}
