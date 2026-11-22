package automelee.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Records every attack the local player starts, with the cooldown it had at that moment. */
final class AttackLog {

  /** Named as text so the game test does not compile against Auto Melee's internals. */
  private static final String AUTO_MELEE_DISPATCHER = "automelee.AttackDispatcher";

  private final AtomicInteger ticks;
  private final List<Hit> hits = new ArrayList<>();

  AttackLog(AtomicInteger ticks) {
    this.ticks = ticks;
  }

  @SubscribeEvent
  public void onAttack(AttackEntityEvent event) {
    EntityPlayer player = event.getEntityPlayer();
    if (!player.world.isRemote || player != Minecraft.getMinecraft().player) {
      return;
    }
    boolean fromAutoMelee = calledFromAutoMelee();
    synchronized (hits) {
      hits.add(
          new Hit(
              ticks.get(),
              player.getCooledAttackStrength(0.5F),
              event.getTarget().getClass().getSimpleName() + "#" + event.getTarget().getEntityId(),
              fromAutoMelee));
    }
  }

  /** Auto Melee attacks only through its dispatcher, which is on the stack when the event fires. */
  private static boolean calledFromAutoMelee() {
    for (StackTraceElement frame : Thread.currentThread().getStackTrace()) {
      if (frame.getClassName().equals(AUTO_MELEE_DISPATCHER)) {
        return true;
      }
    }
    return false;
  }

  /** Hits from the given client tick onward. */
  List<Hit> since(int tick) {
    List<Hit> result = new ArrayList<>();
    synchronized (hits) {
      for (Hit hit : hits) {
        if (hit.tick >= tick) {
          result.add(hit);
        }
      }
    }
    return result;
  }

  static final class Hit {
    final int tick;
    final float strength;
    final String target;
    final boolean fromAutoMelee;

    Hit(int tick, float strength, String target, boolean fromAutoMelee) {
      this.tick = tick;
      this.strength = strength;
      this.target = target;
      this.fromAutoMelee = fromAutoMelee;
    }
  }
}
