package sorazodia.survival.mechanics.trackers;

import sorazodia.survival.io.IO;
import sorazodia.survival.io.ItemTracker;

public class WhiteListTracker extends IO
{

	public WhiteListTracker(String path)
	{
		super(path, "whitelist", new ItemTracker());
	}

}
