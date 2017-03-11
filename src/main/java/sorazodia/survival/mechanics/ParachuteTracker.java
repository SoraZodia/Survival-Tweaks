package sorazodia.survival.mechanics;

import java.util.ArrayList;

import net.minecraft.item.Item;

public class ParachuteTracker
{
	private final static ArrayList<String> ITEMS = new ArrayList<>();
	
	public static void addParachute(String items[])
	{
		for (String name:items)
		{
			ParachuteTracker.ITEMS.add(name);
		}
	}
	
	public static void addParachute(String item)
	{
			ParachuteTracker.ITEMS.add(item);
	}
	
	public static boolean isParachute(Item item)
	{
		return ITEMS.indexOf(Item.getIdFromItem(item)) != -1 || ITEMS.indexOf(item.getUnlocalizedName()) != -1 || ITEMS.indexOf(Item.REGISTRY.getNameForObject(item).toString()) != -1;
	}
}
