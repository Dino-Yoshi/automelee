package automelee;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(
    modid = "automelee",
    name = "Auto Melee",
    version = Tags.VERSION,
    clientSideOnly = true,
    acceptableRemoteVersions = "*",
    dependencies = "after:bettercombatmod;after:reachfix")
public final class AutoMelee {

  @Mod.EventHandler
  public void init(FMLInitializationEvent event) {
    MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
  }
}
