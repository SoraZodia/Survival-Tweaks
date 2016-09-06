package sorazodia.survival.teleport;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import sorazodia.survival.main.SurvivalTweaks;

public class InterDimTeleporter extends Teleporter
{

	private WorldServer worldServer;
	private double x;
	private double y;
	private double z;

	public InterDimTeleporter(WorldServer worldServer, double x, double z)
	{
		this(worldServer, x, getY((int)x, (int)z, worldServer.getSpawnPoint().getY(), worldServer.getActualHeight(), worldServer), z);
	}
	
	public InterDimTeleporter(WorldServer worldServer, double x, double y, double z)
	{
		super(worldServer);
		this.worldServer = worldServer;
		this.x = x;
		this.y = y;
		this.z = z;
		
		if (worldServer.provider.getDimensionId() == 1) //The End is weird
		{
			this.x = 0;
			this.z = 0;
			this.y = getY((int)x, (int)z, 50, 128, worldServer);
		}
		
		if (worldServer.provider.getDimensionId() == -1) // Cause max height in Nether is 256 but the bedrock ceiling is at 128...
		{
			this.y = getY((int)x, (int)z, (int)y, 128, worldServer);
		}
	}

	@Override
	public void placeInPortal(Entity entity, float rotation)
	{
		worldServer.theChunkProviderServer.loadChunk((int) x, (int) z);
		entity.setPosition(x, y, z);
		
		entity.motionX = entity.motionY = entity.motionZ = 0;
	}

	private static int getY(int x, int z, int minHeight, int maxHeight, WorldServer worldServer)
	{
		int y = 70; //dummy value
		int tries = maxHeight - minHeight; //The loop should be finished before that amount of loops, since it's doing a binary search
		Block blockLower;
		Block blockUpper;
		BlockPos upperBound;
		BlockPos lowerBound;
		
		while (minHeight != maxHeight && tries > 0)
		{
			y = (maxHeight + minHeight) / 2;
			lowerBound = new BlockPos(x, y - 1, z);
			upperBound = new BlockPos(x, y + 1, z);
			blockLower = worldServer.getBlockState(lowerBound).getBlock();
			blockUpper = worldServer.getBlockState(upperBound).getBlock();
			
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
		
		if (tries <= 0)
		{
			SurvivalTweaks.getLogger().error("Unable to find a good Y value for dim " + worldServer.provider.getDimensionId()  + ", defaulting to 70!");
			y = 70;	
		}
		
		return y;
	}

	public Double getY()
	{
		return Double.valueOf(y);
	}

}
