package sorazodia.survival.config;

import java.util.ArrayList;

import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.config.Configuration;
import sorazodia.survival.main.SurvivalTweaks;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public class ConfigHandler
{
	public static Configuration configFile;
	private static String[] potionList = {};
	private static ArrayList<Integer> potionIDs = new ArrayList<>();
	private static ArrayList<String> invalidEntry = new ArrayList<>();
	
	public ConfigHandler(FMLPreInitializationEvent event)
	{
		configFile = new Configuration(event.getSuggestedConfigurationFile());
		syncConfig();
	}

	public static void syncConfig()
	{
		addToIDList(configFile.getStringList(StatCollector.translateToLocal("survivaltweaks.config"), Configuration.CATEGORY_GENERAL, potionList, StatCollector.translateToLocal("survivaltweaks.config.description")));
		if (configFile.hasChanged())
			configFile.save();
	}

	private static void addToIDList(String[] stringList)
	{
		
		for (int x = 0; x < stringList.length; x++)
		{
			if(SurvivalTweaks.isInteger(stringList[x]))
				potionIDs.add(Integer.parseInt(stringList[x]));
			else
			{
				FMLLog.info("%s is not a valid number", stringList[x]);
				invalidEntry.add(stringList[x]);
			}
		}
	}
	
	public static ArrayList<Integer> getPotionIDs()
	{
		return potionIDs;
	}
	
	public static ArrayList<String> getInvalidIDs()
	{
		return invalidEntry;
	}
	
	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent config)
	{
		if (config.modID.equals(SurvivalTweaks.MODID))
			syncConfig();
	}
	
	@SubscribeEvent
	public void alertPlayer(PlayerLoggedInEvent joinEvent)
	{
		if (invalidEntry.size() > 0)
			joinEvent.player.addChatComponentMessage(new ChatComponentTranslation("survivaltweaks.invalid.potion", invalidEntry.toString()));
	}

}
