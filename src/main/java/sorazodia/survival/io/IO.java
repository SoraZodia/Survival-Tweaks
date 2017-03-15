package sorazodia.survival.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import sorazodia.survival.mechanics.ParachuteTracker;

public class IO
{
	private String path = "";

	public IO(String path)
	{
		this.path = path + "\\parachutes";
	}

	public void read()
	{
		try
		{
			File dir = new File(path);
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
					ParachuteTracker.addParachute(str);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
