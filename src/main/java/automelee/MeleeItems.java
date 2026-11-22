package automelee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemSword;

public final class MeleeItems {

  /** Matched by name, so the mod never references a BetterSurvival type. */
  static final List<String> BETTER_SURVIVAL_MELEE_CLASS_NAMES =
      Arrays.asList(
          "com.mujmajnkraft.bettersurvival.items.ItemDagger",
          "com.mujmajnkraft.bettersurvival.items.ItemHammer",
          "com.mujmajnkraft.bettersurvival.items.ItemBattleAxe");

  private MeleeItems() {}

  public static boolean isMelee(Item item) {
    return isMelee(item, BuiltIn.CLASSES);
  }

  static boolean isMelee(Item item, List<? extends Class<?>> meleeClasses) {
    if (item instanceof ItemSword || item instanceof ItemAxe) {
      return true;
    }
    for (Class<?> meleeClass : meleeClasses) {
      if (meleeClass.isInstance(item)) {
        return true;
      }
    }
    return false;
  }

  static List<Class<?>> resolve(List<String> names, ClassLoader loader) {
    List<Class<?>> classes = new ArrayList<>();
    for (String name : names) {
      try {
        classes.add(Class.forName(name, false, loader));
      } catch (ClassNotFoundException | LinkageError missing) {
        // The pack doesn't have this class, so there's nothing to match.
      }
    }
    return Collections.unmodifiableList(classes);
  }

  /** Resolved on first use, which is after every mod has loaded. */
  private static final class BuiltIn {
    static final List<Class<?>> CLASSES =
        resolve(BETTER_SURVIVAL_MELEE_CLASS_NAMES, MeleeItems.class.getClassLoader());
  }
}
