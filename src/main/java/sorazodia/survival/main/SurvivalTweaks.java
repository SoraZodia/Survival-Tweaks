package sorazodia.survival.main;

import static sorazodia.survival.main.SurvivalTweaks.*;

import java.nio.file.Paths;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import org.apache.logging.log4j.Logger;

import sorazodia.survival.config.ConfigHandler;
import sorazodia.survival.io.IO;
import sorazodia.survival.mechanics.BlockBreakEvent;
import sorazodia.survival.mechanics.EnderEvent;
import sorazodia.survival.mechanics.EntityTickEvent;
import sorazodia.survival.mechanics.PlayerActionEvent;
import sorazodia.survival.mechanics.PlayerSleepEvent;
import sorazodia.survival.mechanics.trackers.BlackListTracker;
import sorazodia.survival.mechanics.trackers.ParachuteTracker;
import sorazodia.survival.mechanics.trackers.WhiteListTracker;
import sorazodia.survival.server.command.CommandDimensionTeleport;
import sorazodia.survival.server.command.CommandReloadList;
import sorazodia.survival.server.command.DimensionChecker;

@Mod(name = NAME, version = VERSION, modid = MODID, guiFactory = GUI_FACTORY)
public class SurvivalTweaks
{
	public static final String MODID = "survivaltweaks";
	public static final String VERSION = "5.1.0";
	public static final String NAME = "Survival Tweaks";
	public static final String GUI_FACTORY = "sorazodia.survival.config.ConfigGUIFactory";

	private static ConfigHandler configHandler;
	private static Logger log;
	
	private static IO[] trackers = new IO[3];//0=parachute, 1=whitelist, 2=blacklist

	@EventHandler
	public void serverStart(FMLServerStartingEvent preServerEvent)
	{
		if (!(Loader.isModLoaded("Mystcraft") || Loader.isModLoaded("rftools")))
			preServerEvent.registerServerCommand(new CommandDimensionTeleport());
		DimensionChecker.clear();
		
		preServerEvent.registerServerCommand(new CommandReloadList());
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent preEvent)
	{
		String path = Paths.get(preEvent.getModConfigurationDirectory().getAbsolutePath().toString(), "survivalTweaks").toString();
		log = preEvent.getModLog();
		
		log.info(path);
		log.info("Syncing config and registering events");
		configHandler = new ConfigHandler(preEvent);
		trackers[0] = new ParachuteTracker(path);
		trackers[1] = new WhiteListTracker(path);
		trackers[2] = new BlackListTracker(path);

		MinecraftForge.EVENT_BUS.register(new PlayerActionEvent());
		MinecraftForge.EVENT_BUS.register(new EnderEvent());
		MinecraftForge.EVENT_BUS.register(new EntityTickEvent());
		MinecraftForge.EVENT_BUS.register(new BlockBreakEvent());
		MinecraftForge.EVENT_BUS.register(new DimensionChecker());
		MinecraftForge.EVENT_BUS.register(new PlayerSleepEvent());
		MinecraftForge.EVENT_BUS.register(configHandler);

		for (IO paser: trackers)
			paser.read();

		log.info("Mod Loaded");
	}
	
	public static IO getParachuteTracker()
	{
		return trackers[0];
	}
	
	public static IO getWhiteListTracker()
	{
		return trackers[1];
	}
	
	public static IO getBlackListTracker()
	{
		return trackers[2];
	}

	public static Logger getLogger()
	{
		return log;
	}
	
	public void debug(String message)
	{
		log.debug(message);
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

	public static void playSound(SoundEvent sound, SoundCategory category, World world, EntityPlayer player, boolean sendToAll)
	{
		BlockPos pos = player.getPosition();
		
		if (sendToAll)
			player = null;
		
		world.playSound(player, pos, sound, category, 1, 1);
	}

}
