package sorazodia.survival.server.command;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class DimensionChecker
{

	private static ArrayList<Integer> IDList = new ArrayList<>();
	private static HashMap<Integer, String> nameList = new HashMap<>();

	@SubscribeEvent
	public void addWorldID(WorldEvent.Load unloadEvent)
	{
		add(unloadEvent.world.provider.dimensionId, unloadEvent.world.provider.getDimensionName());
	}

	public static int getID(int index)
	{
		return IDList.get(index);
	}

	public static int size()
	{
		return IDList.size();
	}
	
	public static boolean add(Integer key, String value)
	{
		if (!nameList.containsKey(key) && !IDList.contains(key))
		{
			nameList.put(key, value);
			IDList.add(key);
			
			return true;
		}
		return false;
	}
	
	public static String getName(Integer key)
	{
		if (nameList.containsKey(key))
			return nameList.get(key);
		
		return "???";
	}

	public static ArrayList<Integer> getIDList()
	{
		return IDList;
	}
	
	public static void clear()
	{
		IDList.clear();
		nameList.clear();
	}
}
