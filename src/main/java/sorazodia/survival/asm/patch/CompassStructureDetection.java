package sorazodia.survival.asm.patch;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class CompassStructureDetection
{
	public static double findHome(World world, double x, double z, double needle, double cameraDirection, boolean bool)
	{
		if (world != null && !bool)
		{
			if (world.provider.isHellWorld)
			{
				ChunkCoordinates locator = world.getSpawnPoint();

				if (locator != null)
				{
					double X = locator.posX - x;
					double Z = locator.posZ - z;
					needle = -(cameraDirection * Math.PI / 180 - Math.atan2(X, Z));
				}
			}

		}

		return needle;
	}
}
