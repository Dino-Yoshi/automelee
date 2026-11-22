package automelee.gametest;

/** A scenario assertion or setup check that did not hold. */
final class ScenarioFailed extends RuntimeException {

  private static final long serialVersionUID = 1L;

  ScenarioFailed(String message) {
    super(message);
  }
}
