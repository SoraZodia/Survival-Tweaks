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
	
	//ender related
	private static boolean pearlEndDamage = true;
	private static boolean enderTeleport = true;
	private static boolean pearlCreative = true;
	
	//player related
	private static boolean stepAssist = true;
	private static boolean swordProtection = true;
	private static boolean bowPotionBoost = true;
	private static boolean sleepHeal = true;
	private static boolean armorSwap = true;
	private static boolean toolBlockPlace = true;
	private static boolean throwArrow = true;
	
	//nether related
	private static boolean spawnLava = true;
	private static boolean netherBlockEffect = true;
	private static boolean burn = false;
	
	public ConfigHandler(FMLPreInitializationEvent event)
	{
		configFile = new Configuration(event.getSuggestedConfigurationFile());
		syncConfig();
	}

	public static void syncConfig()
	{
		addToIDList(configFile.getStringList(StatCollector.translateToLocal("survivaltweaks.config.bed"), Configuration.CATEGORY_GENERAL, potionList, StatCollector.translateToLocal("survivaltweaks.config.bed.description")));
		
		pearlEndDamage = configFile.getBoolean(StatCollector.translateToLocal("survivaltweaks.config.pearlEndDamage"), Configuration.CATEGORY_GENERAL, true, StatCollector.translateToLocal("survivaltweaks.config.pearlEndDamage.effect"));
		enderTeleport = configFile.getBoolean(StatCollector.translateToLocal("survivaltweaks.config.enderTeleport"), Configuration.CATEGORY_GENERAL, true, StatCollector.translateToLocal("survivaltweaks.config.enderTeleport.effect"));
		pearlCreative = configFile.getBoolean(StatCollector.translateToLocal("survivaltweaks.config.pearlCreative"), Configuration.CATEGORY_GENERAL, true, StatCollector.translateToLocal("survivaltweaks.config.pearlCreative.effect"));
		
		stepAssist = configFile.getBoolean(StatCollector.translateToLocal("survivaltweaks.config.stepAssist"), Configuration.CATEGORY_GENERAL, true, StatCollector.translateToLocal("survivaltweaks.config.stepAssist.effect"));
		swordProtection = configFile.getBoolean(StatCollector.translateToLocal("survivaltweaks.config.swordProtection"), Configuration.CATEGORY_GENERAL, true, StatCollector.translateToLocal("survivaltweaks.config.swordProtection.effect"));
		bowPotionBoost = configFile.getBoolean(StatCollector.translateToLocal("survivaltweaks.config.bowPotionBoost"), Configuration.CATEGORY_GENERAL, true, StatCollector.translateToLocal("survivaltweaks.config.bowPotionBoost.effect"));
		sleepHeal = configFile.getBoolean(StatCollector.translateToLocal("survivaltweaks.config.sleepHeal"), Configuration.CATEGORY_GENERAL, true, StatCollector.translateToLocal("survivaltweaks.config.sleepHeal.effect"));
		armorSwap = configFile.getBoolean(StatCollector.translateToLocal("survivaltweaks.config.armorSwap"), Configuration.CATEGORY_GENERAL, true, StatCollector.translateToLocal("survivaltweaks.config.armorSwap.effect"));
		toolBlockPlace = configFile.getBoolean(StatCollector.translateToLocal("survivaltweaks.config.toolBlockPlace"), Configuration.CATEGORY_GENERAL, true, StatCollector.translateToLocal("survivaltweaks.config.toolBlockPlace.effect"));
		throwArrow = configFile.getBoolean(StatCollector.translateToLocal("survivaltweaks.config.throwArrow"), Configuration.CATEGORY_GENERAL, true, StatCollector.translateToLocal("survivaltweaks.config.throwArrow.effect"));
		
		spawnLava = configFile.getBoolean(StatCollector.translateToLocal("survivaltweaks.config.lava"), Configuration.CATEGORY_GENERAL, true, StatCollector.translateToLocal("survivaltweaks.config.lava.effect"));
		burn = configFile.getBoolean(StatCollector.translateToLocal("survivaltweaks.config.burn"), Configuration.CATEGORY_GENERAL, false, StatCollector.translateToLocal("survivaltweaks.config.burn.effect"));
		netherBlockEffect = configFile.getBoolean(StatCollector.translateToLocal("survivaltweaks.config.netherBlockEffect"), Configuration.CATEGORY_GENERAL, true, StatCollector.translateToLocal("survivaltweaks.config.netherBlockEffect.effect"));
		
		
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

	public static boolean getSpawnLava()
	{
		return spawnLava;
	}

	public static boolean getBurn()
	{
		return burn;
	}

	public static boolean getPearlEndDamage()
	{
		return pearlEndDamage;
	}

	public static boolean getEnderTeleport()
	{
		return enderTeleport;
	}
	
	public static boolean getPearlCreative()
	{
		return pearlCreative;
	}

	public static boolean getStepAssist()
	{
		return stepAssist;
	}

	public static boolean getSwordProtection()
	{
		return swordProtection;
	}

	public static boolean getBowPotionBoost()
	{
		return bowPotionBoost;
	}

	public static boolean getSleepHeal()
	{
		return sleepHeal;
	}

	public static boolean getArmorSwap()
	{
		return armorSwap;
	}

	public static boolean getToolBlockPlace()
	{
		return toolBlockPlace;
	}

	public static boolean getNetherBlockEffect()
	{
		return netherBlockEffect;
	}
	
	public static boolean getArrowThrow()
	{
		return throwArrow;
	}

}
