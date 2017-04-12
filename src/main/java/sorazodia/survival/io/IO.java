package sorazodia.survival.io;

import java.io.IOException;

public abstract class IO
{
	@SuppressWarnings("unused")
	protected String path = "";
	
	public IO(String path, String dirName)
	{
		this.path = path + dirName;
	}
	
	public void read()
	{
		try
		{
			parse();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	protected abstract void parse() throws IOException;

}
