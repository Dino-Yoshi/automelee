package automelee;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AttackPathTest {

  @Test
  void rlcombatLoadedSelectsRlcombatPath() {
    assertEquals(AttackPath.RLCOMBAT, AttackPath.select(true));
  }

  @Test
  void rlcombatAbsentSelectsVanillaPath() {
    assertEquals(AttackPath.VANILLA, AttackPath.select(false));
  }
}
