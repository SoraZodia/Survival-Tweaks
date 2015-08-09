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
		this(worldServer, x, getY((int)x, (int)z, worldServer.getHeightValue((int) x, (int) z), worldServer.getActualHeight(), worldServer), z);
	}
	
	public InterDimTeleporter(WorldServer worldServer, double x, double y, double z)
	{
		super(worldServer);
		this.worldServer = worldServer;
		this.x = x;
		this.y = y;
		this.z = z;
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
		int y = (maxHeight + minHeight) /2;
		Block blockLower = worldServer.getBlock(x, y - 1, z);
		Block blockUpper = worldServer.getBlock(x, y + 1, z);
		
		if (blockLower != Blocks.air && blockUpper == Blocks.air) //Safe point for player
			return y + 2;
		
		if (minHeight == maxHeight)
			return maxHeight; //Used as a fail-safe
		
		if (blockLower == Blocks.air && blockLower == Blocks.air) //Player is in the air, lower y
			return getY(x, z, minHeight, y, worldServer);
		
		if (blockLower != Blocks.air && blockUpper != Blocks.air) //Player is buried, y too low;
			return getY(x, z, y, maxHeight, worldServer);
		
		return getY(x, z, y, maxHeight, worldServer);
	}

	public Double getY()
	{
		return Double.valueOf(y);
	}

}
