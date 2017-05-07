package sorazodia.survival.mechanics.trackers;

import sorazodia.survival.io.IO;
import sorazodia.survival.io.ItemTracker;

public class BlackListTracker extends IO
{
	public BlackListTracker(String path)
	{
		super(path, "blacklist", new ItemTracker());
	}

}
