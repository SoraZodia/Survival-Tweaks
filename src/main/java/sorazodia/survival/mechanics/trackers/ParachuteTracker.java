package sorazodia.survival.mechanics.trackers;

import sorazodia.survival.io.IO;
import sorazodia.survival.io.ItemTracker;


public class ParachuteTracker extends IO
{
	
	public ParachuteTracker(String path)
	{
		super(path, "parachutes", new ItemTracker());
	}

}
