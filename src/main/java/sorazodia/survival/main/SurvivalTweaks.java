package sorazodia.survival.main;

import static sorazodia.survival.main.SurvivalTweaks.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.Logger;

import sorazodia.survival.config.ConfigHandler;
import sorazodia.survival.mechanics.BlockBreakEvent;
import sorazodia.survival.mechanics.EnderEvent;
import sorazodia.survival.mechanics.EntityTickEvent;
import sorazodia.survival.mechanics.PlayerActionEvent;
import sorazodia.survival.server.command.CommandDimensionTeleport;
import sorazodia.survival.server.command.DimensionChecker;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(name = NAME, version = VERSION, modid = MODID, guiFactory = GUI_FACTORY)
public class SurvivalTweaks
{
	public static final String MODID = "survivalTweaks";
	public static final String VERSION = "1.0.2";
	public static final String NAME = "Survival Tweaks";
	public static final String GUI_FACTORY = "sorazodia.survival.config.ConfigGUIFactory";

	private static ConfigHandler configHandler;
	private static Logger log;
	private static float soundLevel = 0;

	@EventHandler
	public void serverStart(FMLServerStartingEvent preServerEvent)
	{
		preServerEvent.registerServerCommand(new CommandDimensionTeleport());
		DimensionChecker.clear();
	}

	@EventHandler
	public void serverStarted(FMLServerStartedEvent serverInitEvent)
	{
		MinecraftServer server = MinecraftServer.getServer();

		if (Loader.isModLoaded("Mystcraft") || Loader.isModLoaded("rftools"))
			if (server.getCommandManager().getCommands().containsKey(CommandDimensionTeleport.getName()))
				server.getCommandManager().getCommands().remove(CommandDimensionTeleport.getName());
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent preEvent)
	{
		log = preEvent.getModLog();
		log.info("Initializating...");
		log.info("Syncing config");
		configHandler = new ConfigHandler(preEvent);
	}

	@EventHandler
	public void init(FMLInitializationEvent initEvent)
	{
		log.info("Registering Events");
		MinecraftForge.EVENT_BUS.register(new PlayerActionEvent());
		MinecraftForge.EVENT_BUS.register(new EnderEvent());
		MinecraftForge.EVENT_BUS.register(new EntityTickEvent());
		MinecraftForge.EVENT_BUS.register(new BlockBreakEvent());
		MinecraftForge.EVENT_BUS.register(new DimensionChecker());

		FMLCommonHandler.instance().bus().register(new PlayerActionEvent());
		FMLCommonHandler.instance().bus().register(configHandler);

		log.info("Mod Loaded");
	}

	//From http://stackoverflow.com/questions/237159/whats-the-best-way-to-check-to-see-if-a-string-represents-an-integer-in-java
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

	public static void playSound(String name, World world, EntityPlayer player)
	{
		if (world.isRemote)
			soundLevel = Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.PLAYERS);
		
		player.playSound(name, soundLevel, soundLevel);
	}

}
