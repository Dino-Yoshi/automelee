package automelee.gametest;

/** A named scenario. The body returns a short reason when it passes. */
final class Scenario {

  interface Body {
    String run(Harness harness) throws Exception;
  }

  final String name;
  final Body body;

  Scenario(String name, Body body) {
    this.name = name;
    this.body = body;
  }
}
