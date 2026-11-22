package automelee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TargetResolutionTest {

  private static final String DRAGON = "dragon";
  private static final String PART = "dragon-part";

  @Test
  void partResolvesToParentWhenAvailable() {
    assertEquals(DRAGON, resolve(PART, true));
  }

  @Test
  void normalTargetIsUnchangedWhenAvailable() {
    assertEquals("zombie", resolve("zombie", true));
  }

  @Test
  void partIsUnchangedWhenIntegrationUnavailable() {
    assertEquals(PART, resolve(PART, false));
  }

  @Test
  void nullTargetStaysNull() {
    assertNull(TargetResolution.resolve(null, true, s -> true, s -> DRAGON));
  }

  @Test
  void partWithoutParentFallsBackToItself() {
    assertEquals(PART, TargetResolution.resolve(PART, true, s -> true, s -> null));
  }

  private static String resolve(String target, boolean available) {
    return TargetResolution.resolve(
        target, available, PART::equals, s -> PART.equals(s) ? DRAGON : s);
  }
}
