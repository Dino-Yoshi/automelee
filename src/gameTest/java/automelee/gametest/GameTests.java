package automelee.gametest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Development-only entry point. Runs the scenario suite once the main menu is shown. */
@Mod(
    modid = "automeleegametest",
    name = "Auto Melee Game Tests",
    version = "1.0",
    clientSideOnly = true,
    acceptableRemoteVersions = "*")
public final class GameTests {

  private final Harness harness = new Harness();
  private boolean started;

  @Mod.EventHandler
  public void init(FMLInitializationEvent event) {
    Minecraft mc = Minecraft.getMinecraft();
    mc.gameSettings.pauseOnLostFocus = false;
    mc.mouseHelper = new StillMouse();
    MinecraftForge.EVENT_BUS.register(this);
    MinecraftForge.EVENT_BUS.register(harness.attacks());
  }

  /** Real clicks would count as attacks, so the suite drives the attack key on its own. */
  @SubscribeEvent
  public void onMouse(MouseEvent event) {
    event.setCanceled(true);
  }

  @SubscribeEvent
  public void onClientTick(TickEvent.ClientTickEvent event) {
    if (event.phase != TickEvent.Phase.END) {
      return;
    }
    harness.onClientTick();
    if (!started && Minecraft.getMinecraft().currentScreen instanceof GuiMainMenu) {
      started = true;
      new Suite(harness).start();
    }
  }
}
