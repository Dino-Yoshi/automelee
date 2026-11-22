package automelee.gametest;

import automelee.gametest.AttackLog.Hit;
import java.util.Arrays;
import java.util.List;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;

/** The scenario list. Each scenario prepares the arena itself, so they can run in any order. */
final class Scenarios {

  /** Five seconds at 20 ticks per second. */
  private static final int HOLD_TICKS = 100;

  /** Two seconds. */
  private static final int QUIET_TICKS = 40;

  /** Eating takes 32 ticks, so this stays inside one meal. */
  private static final int EAT_SAMPLE_TICKS = 20;

  /** Three seconds, for hits to resume after a pause. */
  private static final int RESUME_TICKS = 60;

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
        new Scenario("aiming at a block mines without attacks", Scenarios::blockAim),
        new Scenario("eating in the off-hand lands no hits until it stops", Scenarios::eating),
        new Scenario("blocking with a shield lands no hits", Scenarios::shieldBlock),
        new Scenario("open inventory lands no hits until it closes", Scenarios::inventory),
        new Scenario("spectator mode lands no hits", Scenarios::spectator),
        new Scenario(
            "holding use with a sword in the off-hand lands no hits", Scenarios::offhandUse),
        new Scenario(
            "attack and use with swords in both hands keep full-strength hits",
            Scenarios::dualSwordAttackAndUse),
        new Scenario(
            "BetterSurvival spear hold lands no hits and keeps the spear",
            Scenarios::betterSurvivalSpearHold),
        new Scenario(
            "BetterSurvival nunchaku hold leaves its own loop in charge",
            Scenarios::betterSurvivalNunchakuHold));
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

  private static String eating(Harness h) throws Exception {
    h.prepare(Items.IRON_SWORD);
    h.giveOffhand(Items.COOKED_BEEF);
    h.setFoodLevel(2);
    h.spawnAimedPig();
    h.holdAttack(true);
    h.holdUse(true);
    h.awaitUntil(
        "eating to start (setup: the off-hand food was not used)",
        false,
        () -> h.isHandActive(),
        20);
    int start = h.tick();
    for (int i = 0; i < EAT_SAMPLE_TICKS; i++) {
      h.awaitTicks(1);
      h.check(h.isHandActive(), "eating ended " + i + " ticks in, while use was still held");
    }
    List<Hit> during = h.hitsFrom(start);
    h.check(during.isEmpty(), "landed " + during.size() + " hits while eating");

    h.holdUse(false);
    h.awaitUntil("eating to stop after release", false, () -> !h.isHandActive(), 60);
    int resume = h.tick();
    h.awaitTicks(RESUME_TICKS);
    int after = h.hitsFrom(resume).size();
    h.holdAttack(false);
    h.check(after >= 1, "no hits in 3 s after eating stopped");
    return "0 hits in " + EAT_SAMPLE_TICKS + " ticks of eating, " + after + " hits in 3 s after";
  }

  private static String shieldBlock(Harness h) throws Exception {
    h.prepare(Items.IRON_SWORD);
    h.giveOffhand(Items.SHIELD);
    h.spawnAimedPig();
    h.holdAttack(true);
    int before = h.tick();
    h.awaitTicks(QUIET_TICKS);
    int unblocked = h.hitsFrom(before).size();
    h.check(unblocked >= 1, "no hits before blocking, so the setup is not working");

    h.holdUse(true);
    h.awaitUntil(
        "the shield to block (setup: the shield was not raised)", false, () -> h.isBlocking(), 20);
    int start = h.tick();
    for (int i = 0; i < EAT_SAMPLE_TICKS; i++) {
      h.awaitTicks(1);
      h.check(h.isBlocking(), "stopped blocking " + i + " ticks in, while use was still held");
    }
    List<Hit> blocked = h.hitsFrom(start);
    h.holdUse(false);
    h.holdAttack(false);
    h.check(blocked.isEmpty(), "landed " + blocked.size() + " hits while blocking");
    return "0 hits in " + EAT_SAMPLE_TICKS + " ticks of blocking, " + unblocked + " before";
  }

  private static String inventory(Harness h) throws Exception {
    h.prepare(Items.IRON_SWORD);
    h.spawnAimedPig();
    h.holdAttack(true);
    int before = h.tick();
    h.awaitTicks(QUIET_TICKS);
    int open = h.hitsFrom(before).size();
    h.check(open >= 1, "no hits before opening the inventory, so the setup is not working");

    h.openInventory();
    h.awaitInventory(true);
    // Opening a screen releases every key, so attack is pressed again after it opens. Vanilla
    // reports attack as not held inside a screen anyway, so this only shows no hit gets through.
    h.holdAttack(true);
    int start = h.tick();
    h.awaitTicks(HOLD_TICKS);
    h.check(h.inventoryOpen(), "the inventory closed by itself");
    int whileOpen = h.hitsFrom(start).size();
    h.check(whileOpen == 0, whileOpen + " hits while the inventory was open");

    h.closeInventory();
    h.awaitInventory(false);
    h.holdAttack(true);
    int resume = h.tick();
    h.awaitTicks(RESUME_TICKS);
    int after = h.hitsFrom(resume).size();
    h.holdAttack(false);
    h.check(after >= 1, "no hits in 3 s after closing the inventory");
    return "0 hits in 5 s with the inventory open, " + after + " hits in 3 s after closing";
  }

  private static String spectator(Harness h) throws Exception {
    h.prepare(Items.IRON_SWORD);
    int pig = h.spawnAimedPig();
    h.holdAttack(true);
    int before = h.tick();
    h.awaitTicks(QUIET_TICKS);
    int survival = h.hitsFrom(before).size();
    h.check(survival >= 1, "no hits in survival, so the setup is not working");

    h.setGameType(GameType.SPECTATOR);
    h.awaitSpectator(true);
    // Spectators fly, so the player can drift after the switch; aim again before measuring.
    h.lookAt(h.entityPosition(pig).add(0D, 0.5D, 0D));
    h.awaitAimAtEntity(pig);
    int start = h.tick();
    h.awaitTicks(HOLD_TICKS);
    int hits = h.hitsFrom(start).size();
    h.holdAttack(false);
    h.check(hits == 0, hits + " hits in spectator mode");
    return "0 hits in 5 s in spectator mode, " + survival + " in 2 s of survival";
  }

  private static String offhandUse(Harness h) throws Exception {
    h.prepare(Items.IRON_PICKAXE);
    h.giveOffhand(Items.IRON_SWORD);
    h.spawnAimedPig();
    int start = h.tick();
    h.holdUse(true);
    h.awaitTicks(HOLD_TICKS);
    h.holdUse(false);
    int hits = h.hitsFrom(start).size();
    h.check(hits == 0, hits + " hits while holding use with a sword in the off-hand");
    return "0 hits in 5 s of holding use with a sword in the off-hand";
  }

  private static String dualSwordAttackAndUse(Harness h) throws Exception {
    h.prepare(Items.IRON_SWORD);
    h.giveOffhand(Items.IRON_SWORD);
    h.spawnAimedPig();
    int start = h.tick();
    h.holdAttack(true);
    h.holdUse(true);
    h.awaitTicks(HOLD_TICKS);
    h.holdUse(false);
    h.holdAttack(false);
    List<Hit> hits = h.hitsFrom(start);
    h.check(hits.size() >= 3, "only " + hits.size() + " hits in 5 s");
    float weakest = weakest(hits);
    h.check(weakest >= FULL, "a hit landed at strength " + weakest);
    return hits.size() + " hits in 5 s, weakest strength " + format(weakest);
  }

  private static String betterSurvivalSpearHold(Harness h) throws Exception {
    Item spear = betterSurvival("itemironspear");
    h.prepare(spear);
    h.spawnAimedPig();
    int start = h.tick();
    holdFor(h, HOLD_TICKS);
    List<Hit> hits = h.hitsFrom(start);
    h.check(hits.isEmpty(), "landed " + targets(hits) + " while holding attack with a spear");
    h.check(h.heldItem() == spear, "the spear left the main hand");
    return "0 hits in 5 s with an iron spear, still in the main hand";
  }

  private static String betterSurvivalNunchakuHold(Harness h) throws Exception {
    // Control: a sword's hits come through Auto Melee, which shows the tag below can see them.
    h.prepare(Items.IRON_SWORD);
    h.spawnAimedPig();
    int control = h.tick();
    holdFor(h, QUIET_TICKS);
    long viaAutoMelee = autoMeleeHits(h.hitsFrom(control));
    h.check(viaAutoMelee >= 1, "the control sword made no Auto Melee hits");

    h.prepare(betterSurvival("itemironnunchaku"));
    h.spawnAimedPig();
    int start = h.tick();
    holdFor(h, HOLD_TICKS);
    List<Hit> hits = h.hitsFrom(start);
    h.check(hits.size() >= 3, "the nunchaku loop landed only " + hits.size() + " hits in 5 s");
    long fromAutoMelee = autoMeleeHits(hits);
    h.check(
        fromAutoMelee == 0,
        fromAutoMelee + " hits came from Auto Melee while holding the nunchaku");
    return hits.size() + " nunchaku loop hits in 5 s, none from Auto Melee";
  }

  private static Item betterSurvival(String name) {
    Item item = Item.getByNameOrId("mujmajnkraftsbettersurvival:" + name);
    if (item == null) {
      throw new IllegalStateException("BetterSurvival item missing: " + name);
    }
    return item;
  }

  private static long autoMeleeHits(List<Hit> hits) {
    return hits.stream().filter(hit -> hit.fromAutoMelee).count();
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
