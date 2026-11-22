package automelee;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Runs at the end of each client tick, after vanilla has handled input. */
public final class ClientTickHandler {

  @SubscribeEvent
  public void onClientTick(TickEvent.ClientTickEvent event) {
    if (event.phase != TickEvent.Phase.END) {
      return;
    }
    Minecraft mc = Minecraft.getMinecraft();
    EntityPlayerSP player = mc.player;
    if (player == null || mc.world == null) {
      return;
    }

    Entity target = TargetFinder.find(mc, player);
    Snapshot snapshot =
        Snapshot.builder()
            .meleeItem(MeleeItems.isMelee(player.getHeldItemMainhand().getItem()))
            .attackKeyHeld(mc.gameSettings.keyBindAttack.isKeyDown())
            .usingItem(player.isHandActive())
            .rowing(player.isRowingBoat())
            .guiOpen(mc.currentScreen != null)
            .spectator(player.isSpectator())
            .alive(player.isEntityAlive())
            .cooledStrength(player.getCooledAttackStrength(0.5F))
            .targetPresent(target != null)
            .targetLivingAndNotSelf(Targets.isValid(target, player))
            .build();

    if (AttackDecision.shouldAttack(snapshot)) {
      AttackDispatcher.attack(mc, player, target);
    }
  }
}
