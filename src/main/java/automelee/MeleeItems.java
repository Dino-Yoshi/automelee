package automelee;

import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemSword;

public final class MeleeItems {

  private MeleeItems() {}

  public static boolean isMelee(Item item) {
    return item instanceof ItemSword || item instanceof ItemAxe;
  }
}
