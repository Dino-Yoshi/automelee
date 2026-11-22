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
    synchronized (hits) {
      hits.add(new Hit(ticks.get(), player.getCooledAttackStrength(0.5F)));
    }
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

    Hit(int tick, float strength) {
      this.tick = tick;
      this.strength = strength;
    }
  }
}
