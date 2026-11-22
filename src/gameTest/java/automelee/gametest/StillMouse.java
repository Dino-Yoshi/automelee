package automelee.gametest;

import net.minecraft.util.MouseHelper;

/**
 * Ignores the real mouse. The suite forces in-game focus, so without this any movement over the
 * window would turn the camera, and the cursor would be grabbed away from the desktop.
 */
final class StillMouse extends MouseHelper {

  @Override
  public void grabMouseCursor() {}

  @Override
  public void ungrabMouseCursor() {}

  @Override
  public void mouseXYChange() {
    deltaX = 0;
    deltaY = 0;
  }
}
