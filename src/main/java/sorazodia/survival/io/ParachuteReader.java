package sorazodia.survival.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import sorazodia.survival.mechanics.ParachuteTracker;

public class ParachuteReader extends IO
{
	public ParachuteReader(String path)
	{
		super(path, "parachutes");
	}

	@Override
	protected void parse() throws IOException
	{
		File dir = new File(this.path);
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
}
