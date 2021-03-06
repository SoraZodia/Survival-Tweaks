package sorazodia.survival.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.minecraft.item.Item;

public abstract class IO
{
	protected Path path;
	protected ItemTracker tracker;
	
	public IO(String path, String dirName, ItemTracker tracker)
	{
		this.path = Paths.get(path, dirName);
		this.tracker = tracker;
	}
	
	public void read()
	{
		try
		{
			File dir = this.path.toFile();
			BufferedReader reader;
			String str;

			if (!dir.exists())
				dir.mkdirs();

			for (File file : dir.listFiles())
			{
				
				if (!file.getName().endsWith(".txt"))
					return;
				
				reader = new BufferedReader(new FileReader(file));
				
				while ((str = reader.readLine()) != null)
					tracker.addItem(str);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void reset()
	{
		tracker.clear();
		this.read();
	}
	
	public boolean isValid(Item item)
	{
		return this.tracker.exists(item);
	}
}
