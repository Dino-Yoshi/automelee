package automelee.gametest;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraftforge.fml.common.FMLCommonHandler;

/** Creates the test world, runs every scenario, writes the report and exits the JVM. */
final class Suite {

  private static final long TIME_LIMIT_MS = 420_000L;
  private static final String ONLY_PROPERTY = "automelee.gameTestOnly";
  private static final String REPEAT_PROPERTY = "automelee.gameTestRepeat";
  private static final String WORLD_NAME = "automelee-gametest";
  private static final String REPORT_PROPERTY = "automelee.gameTestReport";
  private static final String DEFAULT_REPORT = "build/game-tests/report.txt";

  private final Harness harness;
  private final Report report =
      new Report(new File(System.getProperty(REPORT_PROPERTY, DEFAULT_REPORT)));
  private final AtomicBoolean finished = new AtomicBoolean();

  Suite(Harness harness) {
    this.harness = harness;
  }

  void start() {
    daemon(this::drive, "automelee-game-tests").start();
    daemon(this::watch, "automelee-game-watchdog").start();
  }

  private void drive() {
    try {
      harness.launchWorld(WORLD_NAME);
      harness.awaitWorld();
      harness.prepareWorld();
      for (int round = 0; round < repeat(); round++) {
        for (Scenario scenario : Scenarios.all()) {
          if (selected(scenario)) {
            runScenario(scenario);
          }
        }
      }
      finish(report.failed() == 0 ? 0 : 1);
    } catch (Exception e) {
      report.fail("setup", e.toString());
      finish(1);
    }
  }

  private void runScenario(Scenario scenario) {
    try {
      report.pass(scenario.name, scenario.body.run(harness));
    } catch (ScenarioFailed e) {
      report.fail(scenario.name, e.getMessage());
    } catch (Exception e) {
      report.fail(scenario.name, "error: " + e);
    }
  }

  private void watch() {
    try {
      Thread.sleep(TIME_LIMIT_MS * repeat());
    } catch (InterruptedException e) {
      return;
    }
    if (!finished.get()) {
      report.fail("suite", "timed out after " + TIME_LIMIT_MS * repeat() / 1000 + " s");
      finish(2);
    }
  }

  private void finish(int code) {
    if (!finished.compareAndSet(false, true)) {
      return;
    }
    report.write();
    FMLCommonHandler.instance().exitJava(code, false);
  }

  /** Scenarios whose name contains one of the comma-separated filters, or all without a filter. */
  private static boolean selected(Scenario scenario) {
    String only = System.getProperty(ONLY_PROPERTY, "");
    if (only.isEmpty()) {
      return true;
    }
    for (String filter : only.split(",")) {
      if (scenario.name.contains(filter.trim())) {
        return true;
      }
    }
    return false;
  }

  private static int repeat() {
    return Math.max(1, Integer.getInteger(REPEAT_PROPERTY, 1));
  }

  private static Thread daemon(Runnable task, String name) {
    Thread thread = new Thread(task, name);
    thread.setDaemon(true);
    return thread;
  }
}
