package sorazodia.survival.main;

import net.minecraftforge.common.MinecraftForge;
import sorazodia.survival.mechanics.EnderEvent;
import sorazodia.survival.mechanics.PlayerActionEvent;
import sorazodia.survival.server.command.CommandDimensionTeleport;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(name = SurvivalTweaks.NAME, version = SurvivalTweaks.VERSION, modid = SurvivalTweaks.MODID)
public class SurvivalTweaks
{
	public static final String MODID = "survivalTweaks";
	public static final String VERSION = "1.0.0";
	public static final String NAME = "Survival Tweaks";

	@EventHandler
	public void serverStart(FMLServerStartingEvent preServerEvent)
	{
		if (!Loader.isModLoaded("Mystcraft"))
			preServerEvent.registerServerCommand(new CommandDimensionTeleport());
	}

	@EventHandler
	public void init(FMLInitializationEvent initEvent)
	{
		MinecraftForge.EVENT_BUS.register(new PlayerActionEvent());
		MinecraftForge.EVENT_BUS.register(new EnderEvent());

		FMLCommonHandler.instance().bus().register(new PlayerActionEvent());

		FMLLog.info("[Survival Tweaks] Mod Loaded");
	}

}
