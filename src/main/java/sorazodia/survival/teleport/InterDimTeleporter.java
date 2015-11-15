package sorazodia.survival.teleport;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class InterDimTeleporter extends Teleporter
{

	private WorldServer worldServer;
	private double x;
	private double y;
	private double z;

	public InterDimTeleporter(WorldServer worldServer, double x, double z)
	{
		this(worldServer, x, getY((int)x, (int)z, worldServer.getSpawnPoint().posY, worldServer.getActualHeight(), worldServer), z);
	}
	
	public InterDimTeleporter(WorldServer worldServer, double x, double y, double z)
	{
		super(worldServer);
		this.worldServer = worldServer;
		this.x = x;
		this.y = y;
		this.z = z;
		
		if (worldServer.provider.dimensionId == 1) //The End is weird
		{
			this.x = 0;
			this.z = 0;
			this.y = getY((int)x, (int)z, 65, 128, worldServer);
		}
	}

	@Override
	public void placeInPortal(Entity entity, double motionX, double motionY, double motionZ, float rotation)
	{
		worldServer.theChunkProviderServer.loadChunk((int) x, (int) z);
		entity.setPosition(x, y, z);
		
		entity.motionX = motionX;
		entity.motionY = motionY;
		entity.motionZ = motionZ;
	}

	private static int getY(int x, int z, int minHeight, int maxHeight, WorldServer worldServer)
	{
		int y = 70;
		int tries = maxHeight - minHeight; //The loop should be finished before that amount of loops, since it's doing a binary search
		Block blockLower;
		Block blockUpper;
		
		while (minHeight != maxHeight && tries > 0)
		{
			y = (maxHeight + minHeight) / 2;
			blockLower = worldServer.getBlock(x, y - 1, z);
			blockUpper = worldServer.getBlock(x, y + 1, z);
			
			if (blockLower != Blocks.air && blockUpper == Blocks.air) //Safe point for player
			{	
				y += 2;
				break;
			}
			
			if (blockUpper == Blocks.air && blockLower == Blocks.air) //Player is in the air, lower y
				maxHeight = y;
			
			if (blockLower != Blocks.air && blockUpper != Blocks.air) //Player is buried, y too low;
				minHeight = y;
			
			tries--;
		}
		
		return y;
	}

	public Double getY()
	{
		return Double.valueOf(y);
	}

}
