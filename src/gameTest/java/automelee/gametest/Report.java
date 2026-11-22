package automelee.gametest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Collects one line per scenario and writes them to the report file. */
final class Report {

  private static final Logger LOG = LogManager.getLogger("automelee-gametest");

  private final File file;
  private final List<String> lines = new ArrayList<>();
  private int passed;
  private int failed;

  Report(File file) {
    this.file = file;
  }

  synchronized void pass(String name, String reason) {
    passed++;
    add("PASS", name, reason);
  }

  synchronized void fail(String name, String reason) {
    failed++;
    add("FAIL", name, reason);
  }

  synchronized int failed() {
    return failed;
  }

  synchronized void write() {
    lines.add("SUMMARY  " + passed + " passed, " + failed + " failed");
    LOG.info(lines.get(lines.size() - 1));
    try {
      File parent = file.getAbsoluteFile().getParentFile();
      if (parent != null) {
        parent.mkdirs();
      }
      Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
    } catch (IOException e) {
      LOG.error("could not write " + file, e);
    }
  }

  private void add(String status, String name, String reason) {
    String line = status + "  " + name + "  " + reason;
    lines.add(line);
    LOG.info(line);
  }
}
