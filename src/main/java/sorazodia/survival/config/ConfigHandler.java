package sorazodia.survival.config;

import java.util.ArrayList;

import net.minecraft.util.ChatComponentTranslation;
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
	private static boolean doCollision = false;
	
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
		addToIDList(configFile.getStringList("Effects Cleared by Bed", Configuration.CATEGORY_GENERAL, potionList, "Effects with these IDs will be cleared when the player sleeps"));
		
		pearlEndDamage = configFile.getBoolean("No Ender Pearl damage in End", Configuration.CATEGORY_GENERAL, true, "Ender Pearls used in the End will cause no fall damage");
		enderTeleport = configFile.getBoolean("Stronghold Teleport", Configuration.CATEGORY_GENERAL, true, "Teleports a Creative mode player to a Stronghold when they uses the Eye of Ender while sneaking");
		pearlCreative = configFile.getBoolean("Ender Pearl in Creative", Configuration.CATEGORY_GENERAL, true, "Allow players to use Ender Pearls in Creative mode");
		
		stepAssist = configFile.getBoolean("Step Assist", Configuration.CATEGORY_GENERAL, true, "Jump potions grant players a step boost");
		swordProtection = configFile.getBoolean("Sword as Shield", Configuration.CATEGORY_GENERAL, true, "Blocking with the sword will cut the damage in half at the cost of durability");
		bowPotionBoost = configFile.getBoolean("Bow Boost", Configuration.CATEGORY_GENERAL, true, "Strength potions allows player to shoot arrows farer");
		sleepHeal = configFile.getBoolean("Sleep Restoration", Configuration.CATEGORY_GENERAL, true, "Sleeping will remove all potion effects from the player and heal them by 20 hearts (will reduce hunger when it happens)");
		armorSwap = configFile.getBoolean("Armor Swap", Configuration.CATEGORY_GENERAL, true, "Allow armor swapping via right-clicking");
		toolBlockPlace = configFile.getBoolean("Tools Block Place", Configuration.CATEGORY_GENERAL, true, "Tools will be able to place blocks located to the right/left of them in the hotbar");
		throwArrow = configFile.getBoolean("Arrow Throw", Configuration.CATEGORY_GENERAL, true, "Player can shot Arrows without the need for a Bow");
		doCollision = configFile.getBoolean("Player Collision", Configuration.CATEGORY_GENERAL, false, "Add collision check to the player");
		
		spawnLava = configFile.getBoolean("Spawn Lava", Configuration.CATEGORY_GENERAL, true, "Spawn lava when Netherrack or Quartz Ore is mined");
		burn = configFile.getBoolean("Burn Entities", Configuration.CATEGORY_GENERAL, false, "Burn entities when they step on Netherrack or Quartz Ore");
		netherBlockEffect = configFile.getBoolean("Nether Block Effect", Configuration.CATEGORY_GENERAL, true, "Breaking certain blocks from the Nether cause the player to get negative potion effect");
		
		
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
	
	public static boolean getCollision()
	{
		return doCollision;
	}

}
