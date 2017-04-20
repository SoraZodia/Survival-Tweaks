package sorazodia.survival.main;

import java.util.ArrayList;

import net.minecraft.item.Item;

public class ItemTracker
{
	private final static ArrayList<String> ITEMS = new ArrayList<>();
		
	public void addItem(String item)
	{
		ItemTracker.ITEMS.add(item);
	}
	
	public boolean exists(Item item)
	{
		return ITEMS.indexOf(Item.getIdFromItem(item)) != -1 || ITEMS.indexOf(item.getUnlocalizedName()) != -1 || ITEMS.indexOf(Item.itemRegistry.getNameForObject(item).toString()) != -1;
	}
}
