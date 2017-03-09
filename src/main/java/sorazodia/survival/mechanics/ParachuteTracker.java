package sorazodia.survival.mechanics;

import java.util.ArrayList;

public class ParachuteTracker
{
	private static ArrayList<String> items = new ArrayList<>();
	
	public static void addParachute(String items[])
	{
		for (String name:items)
		{
			ParachuteTracker.items.add(name);
		}
	}
	
	public static void addParachute(String item)
	{
			ParachuteTracker.items.add(item);
	}
	
	public static boolean isParachute(String item)
	{
		return ParachuteTracker.items.indexOf(item) != -1;
	}
}
