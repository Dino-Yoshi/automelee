package automelee.gametest;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraftforge.fml.common.FMLCommonHandler;

/** Creates the test world, runs every scenario, writes the report and exits the JVM. */
final class Suite {

  private static final long TIME_LIMIT_MS = 180_000L;
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
      for (Scenario scenario : Scenarios.all()) {
        runScenario(scenario);
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
      Thread.sleep(TIME_LIMIT_MS);
    } catch (InterruptedException e) {
      return;
    }
    if (!finished.get()) {
      report.fail("suite", "timed out after 180 s");
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

  private static Thread daemon(Runnable task, String name) {
    Thread thread = new Thread(task, name);
    thread.setDaemon(true);
    return thread;
  }
}
