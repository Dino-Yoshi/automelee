package automelee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mujmajnkraft.bettersurvival.items.ItemBattleAxe;
import com.mujmajnkraft.bettersurvival.items.ItemDagger;
import com.mujmajnkraft.bettersurvival.items.ItemHammer;
import com.mujmajnkraft.bettersurvival.items.ItemNunchaku;
import com.mujmajnkraft.bettersurvival.items.ItemSpear;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

  private static final ClassLoader LOADER = MeleeItems.class.getClassLoader();

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

  @Test
  void betterSurvivalDaggerIsMelee() {
    assertTrue(MeleeItems.isMelee(new ItemDagger(IRON)));
  }

  @Test
  void betterSurvivalHammerIsMelee() {
    assertTrue(MeleeItems.isMelee(new ItemHammer(IRON)));
  }

  @Test
  void betterSurvivalBattleAxeIsMelee() {
    assertTrue(MeleeItems.isMelee(new ItemBattleAxe(IRON)));
  }

  @Test
  void betterSurvivalSpearIsNotMelee() {
    assertFalse(MeleeItems.isMelee(new ItemSpear(IRON)));
  }

  @Test
  void betterSurvivalNunchakuIsNotMelee() {
    assertFalse(MeleeItems.isMelee(new ItemNunchaku(IRON)));
  }

  @Test
  void subclassOfBetterSurvivalDaggerIsMelee() {
    assertTrue(MeleeItems.isMelee(new ItemDagger(IRON) {}));
  }

  @Test
  void builtInBetterSurvivalNamesAllResolveAgainstTheJar() {
    List<String> names = MeleeItems.BETTER_SURVIVAL_MELEE_CLASS_NAMES;
    assertEquals(names.size(), MeleeItems.resolve(names, LOADER).size());
  }

  @Test
  void resolverKeepsExistingClassesAndSkipsMissingOnes() {
    List<String> names =
        Arrays.asList("com.mujmajnkraft.bettersurvival.items.ItemDagger", "automelee.NoSuchWeapon");
    assertEquals(Collections.singletonList(ItemDagger.class), MeleeItems.resolve(names, LOADER));
  }

  @Test
  void betterSurvivalDaggerDoesNotCountWhenItsClassIsMissing() {
    List<Class<?>> missing =
        MeleeItems.resolve(Collections.singletonList("automelee.NoSuchWeapon"), LOADER);
    assertFalse(MeleeItems.isMelee(new ItemDagger(IRON), missing));
    assertTrue(MeleeItems.isMelee(new ItemSword(IRON), missing));
  }

  @Test
  void subclassOfListedClassCountsInTheListedForm() {
    assertTrue(
        MeleeItems.isMelee(new ItemHammer(IRON) {}, Collections.singletonList(ItemHammer.class)));
  }
}
