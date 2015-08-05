package sorazodia.survival.main;

import static sorazodia.survival.main.SurvivalTweaks.*;
import net.minecraftforge.common.MinecraftForge;
import sorazodia.survival.config.ConfigHandler;
import sorazodia.survival.mechanics.EnderEvent;
import sorazodia.survival.mechanics.EntityTickEvent;
import sorazodia.survival.mechanics.PlayerActionEvent;
import sorazodia.survival.server.command.CommandDimensionTeleport;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(name = NAME, version = VERSION, modid = MODID, guiFactory = GUI_FACTORY)
public class SurvivalTweaks
{
	public static final String MODID = "survivalTweaks";
	public static final String VERSION = "1.0.0";
	public static final String NAME = "Survival Tweaks";
	public static final String GUI_FACTORY = "sorazodia.survival.config.ConfigGUIFactory";
	
	private static ConfigHandler configHandler;

	@EventHandler
	public void serverStart(FMLServerStartingEvent preServerEvent)
	{
		if (!Loader.isModLoaded("Mystcraft") || !Loader.isModLoaded("rftools"))
			preServerEvent.registerServerCommand(new CommandDimensionTeleport());
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent preEvent)
	{
		FMLLog.info("[Survival Tweaks] Initializating Mod");
		FMLLog.info("[Survival Tweaks] Syncing config");
		configHandler = new ConfigHandler(preEvent);
	}

	@EventHandler
	public void init(FMLInitializationEvent initEvent)
	{
		MinecraftForge.EVENT_BUS.register(new PlayerActionEvent());
		MinecraftForge.EVENT_BUS.register(new EnderEvent());

		FMLCommonHandler.instance().bus().register(new PlayerActionEvent());
		FMLCommonHandler.instance().bus().register(new EntityTickEvent());
		FMLCommonHandler.instance().bus().register(configHandler);

		FMLLog.info("[Survival Tweaks] Mod Loaded");
	}

	// Thank you StackOverflow
	public static boolean isInteger(String arg)
	{
		if (arg == null)
			return false;

		int length = arg.length();

		if (length == 0)
			return false;

		int x = 0;

		if (arg.charAt(0) == '-')
		{
			if (length == 1)
				return false;
			x = 1;
		}

		for (; x < length; x++)
		{
			char c = arg.charAt(x);
			if (c <= '/' || c >= ':')
				return false;
		}

		return true;
	}

}
