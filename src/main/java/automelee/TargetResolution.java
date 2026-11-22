package automelee;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/** Maps a hitbox part to the entity it belongs to. */
final class TargetResolution {

  private TargetResolution() {}

  static <T> T resolve(
      T target, boolean available, Predicate<T> isPart, UnaryOperator<T> parentOf) {
    if (!available || target == null || !isPart.test(target)) {
      return target;
    }
    T parent = parentOf.apply(target);
    return parent != null ? parent : target;
  }
}
