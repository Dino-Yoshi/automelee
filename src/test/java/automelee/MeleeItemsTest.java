package automelee;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MeleeItemsTest {

  private static final Item.ToolMaterial IRON = Item.ToolMaterial.IRON;

  @BeforeAll
  static void bootstrapRegistries() {
    Bootstrap.register();
  }

  @Test
  void vanillaSwordIsMelee() {
    assertTrue(MeleeItems.isMelee(new ItemSword(IRON)));
  }

  @Test
  void vanillaAxeIsMelee() {
    assertTrue(MeleeItems.isMelee(new ItemAxe(IRON, 6.0F, -3.1F) {}));
  }

  @Test
  void pickaxeIsNotMelee() {
    assertFalse(MeleeItems.isMelee(new ItemPickaxe(IRON) {}));
  }

  @Test
  void shovelIsNotMelee() {
    assertFalse(MeleeItems.isMelee(new ItemSpade(IRON) {}));
  }

  @Test
  void hoeIsNotMelee() {
    assertFalse(MeleeItems.isMelee(new ItemHoe(IRON)));
  }

  @Test
  void bowIsNotMelee() {
    assertFalse(MeleeItems.isMelee(new ItemBow()));
  }

  @Test
  void shieldIsNotMelee() {
    assertFalse(MeleeItems.isMelee(new ItemShield()));
  }

  @Test
  void plainItemIsNotMelee() {
    assertFalse(MeleeItems.isMelee(new Item()));
  }

  @Test
  void emptyHandIsNotMelee() {
    assertFalse(MeleeItems.isMelee(ItemStack.EMPTY.getItem()));
    assertFalse(MeleeItems.isMelee(Items.AIR));
  }
}
