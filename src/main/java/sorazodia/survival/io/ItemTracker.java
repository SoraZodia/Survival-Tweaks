package sorazodia.survival.io;

import java.util.ArrayList;

import net.minecraft.item.Item;

public class ItemTracker
{
	private final ArrayList<String> items = new ArrayList<>();
		
	public void addItem(String item)
	{
		this.items.add(item);
	}
	
	public boolean exists(Item item)
	{
		return this.items.indexOf(Item.getIdFromItem(item)) != -1 || this.items.indexOf(item.getUnlocalizedName()) != -1 || this.items.indexOf(Item.itemRegistry.getNameForObject(item).toString()) != -1;
	}
	
	protected void clear()
	{
		items.clear();
	}
}
