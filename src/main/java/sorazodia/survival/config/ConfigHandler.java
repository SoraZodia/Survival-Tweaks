package sorazodia.survival.config;

import java.util.ArrayList;

import net.minecraft.potion.Potion;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import sorazodia.survival.main.SurvivalTweaks;

public class ConfigHandler
{
	public static Configuration configFile;
	private static String[] potionList = {};
	private static ArrayList<String> potionIDs = new ArrayList<>();
	private static ArrayList<String> invalidEntry = new ArrayList<>();
	
	//ender related
	private static boolean pearlEndDamage = true;
	private static boolean enderTeleport = true;
	private static boolean pearlCreative = true;
	private static boolean instantTeleport = true;
	
	//player related
	private static boolean stepAssist = true;
	private static int stepAssistBoost = -1;
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
		pearlCreative = configFile.getBoolean("Ender Pearl in Creative", Configuration.CATEGORY_GENERAL, true, "[Pre 1.9] Allow players to use Ender Pearls in Creative mode\n[1.10+] Allows the removal of the Ender Pearl cooldown outside the End for players in creative mode");
		instantTeleport = configFile.getBoolean("Instant Pearl Recharge In End", Configuration.CATEGORY_GENERAL, true, "Removes cooldown of Ender Pearl when in the End");
		
		stepAssist = configFile.getBoolean("Step Assist", Configuration.CATEGORY_GENERAL, true, "Jump potions grant players a step boost");
		stepAssistBoost = configFile.getInt("Step Assist Boost", Configuration.CATEGORY_GENERAL, -1, -1, 500, "Max amount of blocks a player can walk up while having a Jump Boost effect (-1 = Unlimited), will still stack with items that grant step assist");
		swordProtection = configFile.getBoolean("Sword as Shield", Configuration.CATEGORY_GENERAL, true, "[Pre 1.10] Blocking with the sword will cut the damage in half at the cost of durability");
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

	private static void addToIDList(String[] list)
	{
		
		for (int x = 0; x < list.length; x++)
		{
			if(SurvivalTweaks.isInteger(list[x]) || Potion.getPotionFromResourceLocation(list[x]) != null)
				potionIDs.add(list[x]);
			else
			{
				FMLLog.log.debug("%s is not a valid entry", list[x]);
				invalidEntry.add(list[x]);
			}
		}
	}
	
	public static ArrayList<String> getPotionIDs()
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
		if (config.getModID().equals(SurvivalTweaks.MODID))
			syncConfig();
	}
	
	@SubscribeEvent
	public void alertPlayer(PlayerLoggedInEvent joinEvent)
	{
		if (invalidEntry.size() > 0)
			joinEvent.player.sendMessage(new TextComponentTranslation("survivaltweaks.invalid.potion", invalidEntry.toString()));
	}

	public static boolean spawnLava()
	{
		return spawnLava;
	}

	public static boolean doBurn()
	{
		return burn;
	}

	public static boolean doPearlEndDamage()
	{
		return pearlEndDamage;
	}

	public static boolean doInstantRecharge()
	{
		return instantTeleport;
	}

	
	public static boolean doEnderTeleport()
	{
		return enderTeleport;
	}
	
	public static boolean allowPearlCreative()
	{
		return pearlCreative;
	}

	public static boolean applyStepAssist()
	{
		return stepAssist;
	}
	
	public static int getMaxBoost()
	{
		return stepAssistBoost;
	}

	public static boolean allowSwordProtection()
	{
		return swordProtection;
	}

	public static boolean applyBowPotionBoost()
	{
		return bowPotionBoost;
	}

	public static boolean doSleepHeal()
	{
		return sleepHeal;
	}

	public static boolean doArmorSwap()
	{
		return armorSwap;
	}

	public static boolean doToolBlockPlace()
	{
		return toolBlockPlace;
	}

	public static boolean doNetherBlockEffect()
	{
		return netherBlockEffect;
	}
	
	public static boolean doArrowThrow()
	{
		return throwArrow;
	}
	
	public static boolean doCollision()
	{
		return doCollision;
	}

}
