package automelee;

import bettercombat.mod.capability.CapabilityOffhandCooldown;
import bettercombat.mod.client.handler.EventHandlersClient;
import bettercombat.mod.handler.EventHandlers;
import bettercombat.mod.network.PacketHandler;
import bettercombat.mod.network.PacketOffhandAttack;
import bettercombat.mod.util.ConfigurationHandler;
import bettercombat.mod.util.Helpers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

/**
 * The off-hand rules and attack of RLCombat, mirrored for automatic attacks. Loaded only when
 * RLCombat is present.
 */
final class RLCombatOffhand {

  private RLCombatOffhand() {}

  static boolean enabled() {
    return ConfigurationHandler.server.enableOffhandAttack;
  }

  static boolean allowsItem(EntityPlayer player) {
    Item offhand = player.getHeldItemOffhand().getItem();
    return MeleeItems.isMelee(offhand)
        && ConfigurationHandler.isItemAttackUsableOffhand(offhand)
        && !(player.getHeldItemMainhand().getItem() instanceof ItemShield);
  }

  static boolean allowsTarget(EntityPlayer player, Entity target) {
    return ConfigurationHandler.isEntityAttackableOffhand(target)
        && !(target instanceof IEntityOwnable && ((IEntityOwnable) target).getOwner() == player);
  }

  /** The off-hand charge, or 0 when RLCombat's cooldown capability is missing. */
  static float charge(EntityPlayer player) {
    CapabilityOffhandCooldown cooldown = cooldown(player);
    if (cooldown == null) {
      return 0.0F;
    }
    return OffhandCharge.of(cooldown.getTicksSinceLastSwing(), period(player));
  }

  /**
   * The player's cooldown period with the off-hand item's attributes in place. The main-hand
   * attributes are always restored, even when the swap or the read throws.
   */
  static float period(EntityPlayer player) {
    ItemStack mainhand = player.getHeldItemMainhand();
    ItemStack offhand = player.getHeldItemOffhand();
    try {
      Helpers.clearOldModifiers(player, mainhand, false, true, true);
      Helpers.addNewModifiers(player, offhand, false, true, true);
      return player.getCooldownPeriod();
    } finally {
      Helpers.clearOldModifiers(player, offhand, false, true, true);
      Helpers.addNewModifiers(player, mainhand, false, true, true);
    }
  }

  /** Mirrors RLCombat's off-hand click on a hit, without the client-side attack call. */
  static void attack(EntityPlayer player, Entity target) {
    CapabilityOffhandCooldown cooldown = cooldown(player);
    if (cooldown == null) {
      return;
    }
    Entity mover =
        player.isRiding() && player.getRidingEntity() != null ? player.getRidingEntity() : player;
    PacketHandler.instance.sendToServer(
        new PacketOffhandAttack(target.getEntityId(), mover.motionX, mover.motionY, mover.motionZ));
    // Not synced: the server resets and syncs its own copy.
    cooldown.resetTicksSinceLastSwing();
    player.swingArm(EnumHand.OFF_HAND);
    EventHandlersClient.resetOffhandAnimationCooldown(period(player));
    EventHandlersClient.betterCombatOffhand.initiateAnimation();
  }

  private static CapabilityOffhandCooldown cooldown(EntityPlayer player) {
    return player.getCapability(EventHandlers.OFFHAND_COOLDOWN, null);
  }
}
