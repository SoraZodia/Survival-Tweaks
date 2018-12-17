package sorazodia.survival.config;

import java.util.ArrayList;

import net.minecraftforge.common.config.Configuration;
import sorazodia.survival.main.SurvivalTweaks;

public class ConfigBurnList
{
	private static String[] idList = {};
	private static ArrayList<Integer>  dimensionList = new ArrayList<>(); 
	private static ArrayList<String> invalidEntry = new ArrayList<>();
	
	public static void syncDimBurnConfig(Configuration config) 
	{
		for (String str: config.getStringList("Dimension Burn List", Configuration.CATEGORY_GENERAL, idList, "IDs of the dimensions which the burn effect will be active.")) 
		{
			if (SurvivalTweaks.isInteger(str)) {
				dimensionList.add(Integer.parseInt(str));
			}
			else {
				invalidEntry.add(str);
			}
		}
	}
	
	public static ArrayList<String> getInvalidEntries() 
	{
		return invalidEntry;
	}
}
