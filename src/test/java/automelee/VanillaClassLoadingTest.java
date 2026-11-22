package automelee;

import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;
import org.junit.jupiter.api.Test;

class VanillaClassLoadingTest {

  @Test
  void vanillaSwordIsAnItem() {
    assertTrue(Item.class.isAssignableFrom(ItemSword.class));
  }
}
