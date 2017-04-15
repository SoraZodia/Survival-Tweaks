package sorazodia.survival.mechanics.trackers;

import sorazodia.survival.io.IO;
import sorazodia.survival.main.ItemTracker;



public class WhiteListTracker extends IO
{

	public WhiteListTracker(String path)
	{
		super(path, "whitelist", new ItemTracker());
	}

}
