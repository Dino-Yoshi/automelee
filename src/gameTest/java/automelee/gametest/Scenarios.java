package automelee.gametest;

import automelee.gametest.AttackLog.Hit;
import java.util.Arrays;
import java.util.List;
import net.minecraft.init.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/** The scenario list. Each scenario prepares the arena itself, so they can run in any order. */
final class Scenarios {

  /** Five seconds at 20 ticks per second. */
  private static final int HOLD_TICKS = 100;

  /** Two seconds. */
  private static final int QUIET_TICKS = 40;

  /** Tolerance for a cooldown that reads as full. */
  private static final float FULL = 0.999F;

  private Scenarios() {}

  static List<Scenario> all() {
    return Arrays.asList(
        new Scenario("sword hold repeats full-strength hits", Scenarios::swordHold),
        new Scenario("faster sword outpaces slower axe", Scenarios::swordOutpacesAxe),
        new Scenario("pickaxe hold lands no hits", Scenarios::pickaxeHold),
        new Scenario("item frame and armor stand are not attacked", Scenarios::frameAndStand),
        new Scenario("single press lands exactly one hit", Scenarios::singlePress),
        new Scenario("release stops further hits", Scenarios::releaseStops),
        new Scenario("aiming at air lands no hits", Scenarios::airAim),
        new Scenario("aiming at a block mines without attacks", Scenarios::blockAim));
  }

  private static String swordHold(Harness h) throws Exception {
    h.prepare(Items.IRON_SWORD);
    h.spawnAimedPig();
    int start = h.tick();
    holdFor(h, HOLD_TICKS);
    List<Hit> hits = h.hitsFrom(start);
    h.check(hits.size() >= 3, "only " + hits.size() + " hits in 5 s");
    float weakest = weakest(hits);
    h.check(weakest >= FULL, "a hit landed at strength " + weakest);
    return hits.size() + " hits in 5 s, weakest strength " + format(weakest);
  }

  private static String swordOutpacesAxe(Harness h) throws Exception {
    h.prepare(Items.IRON_SWORD);
    h.spawnAimedPig();
    int start = h.tick();
    holdFor(h, HOLD_TICKS);
    int sword = h.hitsFrom(start).size();

    h.prepare(Items.IRON_AXE);
    h.spawnAimedPig();
    start = h.tick();
    holdFor(h, HOLD_TICKS);
    int axe = h.hitsFrom(start).size();

    h.check(sword > axe && axe >= 1, "sword landed " + sword + " hits, axe " + axe);
    return "sword " + sword + " hits, axe " + axe + " in 5 s";
  }

  private static String pickaxeHold(Harness h) throws Exception {
    h.prepare(Items.IRON_PICKAXE);
    h.spawnAimedPig();
    int start = h.tick();
    holdFor(h, HOLD_TICKS);
    int hits = h.hitsFrom(start).size();
    h.check(hits == 0, "pickaxe landed " + hits + " hits");
    return "0 hits in 5 s with an iron pickaxe";
  }

  private static String frameAndStand(Harness h) throws Exception {
    h.prepare(Items.IRON_SWORD);
    int frame = h.placeItemFrame();
    h.lookAt(h.entityPosition(frame));
    h.awaitAimAtEntity(frame);
    int start = h.tick();
    holdFor(h, HOLD_TICKS);
    List<Hit> frameHits = h.hitsFrom(start);
    h.check(
        frameHits.isEmpty(), "attacked " + targets(frameHits) + " while aiming at the item frame");
    h.check(h.isAlive(frame), "item frame is gone");

    h.prepare(Items.IRON_SWORD);
    int stand = h.spawnArmorStand();
    h.lookAt(new Vec3d(0.5D, h.groundY() + 1.0D, 2.5D));
    h.awaitAimAtEntity(stand);
    start = h.tick();
    holdFor(h, HOLD_TICKS);
    List<Hit> standHits = h.hitsFrom(start);
    h.check(
        standHits.isEmpty(), "attacked " + targets(standHits) + " while aiming at the armor stand");
    h.check(h.isAlive(stand), "armor stand is gone");
    return "no hits on the item frame or the armor stand, both still present";
  }

  private static String singlePress(Harness h) throws Exception {
    h.prepare(Items.IRON_SWORD);
    h.spawnAimedPig();
    int press = h.pressAttack();
    h.awaitTicks(1);
    h.holdAttack(false);
    h.awaitTicks(6);
    List<Hit> window = between(h.hitsFrom(press), press, press + 6);
    h.check(window.size() == 1, "one press produced " + window.size() + " hits");
    h.check(
        window.get(0).tick <= press + 1,
        "the hit came at tick " + window.get(0).tick + ", press was " + press);
    return "1 hit on the press tick, none in the next 6 ticks";
  }

  private static String releaseStops(Harness h) throws Exception {
    h.prepare(Items.IRON_SWORD);
    h.spawnAimedPig();
    int start = h.tick();
    h.holdAttack(true);
    h.awaitTicks(HOLD_TICKS);
    int before = h.releaseAttack(start);
    h.check(before >= 2, "only " + before + " hits while holding");
    h.awaitTicks(QUIET_TICKS);
    int after = h.hitsFrom(start).size();
    h.check(after == before, (after - before) + " more hits after release");
    return before + " hits while held, none in the 2 s after release";
  }

  private static String airAim(Harness h) throws Exception {
    h.prepare(Items.IRON_SWORD);
    h.lookAt(new Vec3d(0.5D, h.groundY() + 6.0D, 2.5D));
    h.awaitAimAtNothing();
    int start = h.tick();
    holdFor(h, HOLD_TICKS);
    int hits = h.hitsFrom(start).size();
    h.check(hits == 0, hits + " hits while aiming at open air");
    return "no hits in 5 s while aiming at open air";
  }

  private static String blockAim(Harness h) throws Exception {
    h.prepare(Items.IRON_SWORD);
    BlockPos dirt = h.placeDirt();
    h.lookAt(new Vec3d(0.5D, h.groundY() + 0.5D, 2.5D));
    h.awaitAimAtBlock(dirt);
    int start = h.tick();
    h.holdAttack(true);
    h.awaitBlockBroken(dirt, 60);
    h.holdAttack(false);
    int broke = h.tick() - start;
    int hits = h.hitsFrom(start).size();
    h.check(hits == 0, hits + " hits while mining");
    return "dirt broke after " + broke + " ticks, no attacks";
  }

  /** Holds attack for the given number of ticks, then releases it. */
  private static void holdFor(Harness h, int ticks) throws Exception {
    h.holdAttack(true);
    h.awaitTicks(ticks);
    h.holdAttack(false);
  }

  private static List<Hit> between(List<Hit> hits, int from, int to) {
    List<Hit> result = new java.util.ArrayList<>();
    for (Hit hit : hits) {
      if (hit.tick >= from && hit.tick <= to) {
        result.add(hit);
      }
    }
    return result;
  }

  private static float weakest(List<Hit> hits) {
    float weakest = 1F;
    for (Hit hit : hits) {
      weakest = Math.min(weakest, hit.strength);
    }
    return weakest;
  }

  private static String format(float value) {
    return String.format("%.2f", value);
  }

  private static String targets(List<Hit> hits) {
    StringBuilder out = new StringBuilder();
    for (Hit hit : hits) {
      out.append(out.length() == 0 ? "" : ", ").append(hit.target).append(" @").append(hit.tick);
    }
    return out.toString();
  }
}
