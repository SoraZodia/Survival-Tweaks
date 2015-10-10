package sorazodia.survival.asm.patch;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class CompassStructureDetection
{
	public static double findHome(World world, double x, double z, double needle, double cameraDirection, boolean bool)
	{
		if (world != null && !bool)
		{
			if (world.provider.dimensionId == -1)
			{
				ChunkCoordinates locator = world.getSpawnPoint();

				if (locator != null)
				{
					double X = locator.posX - x;
					double Z = locator.posZ - z;
					needle = -(cameraDirection * Math.PI / 180 - Math.atan2(X, Z));
				}
			}
			if (world.provider.dimensionId == 1)
			{
				double X = 0 - x;
				double Z = 0 - z;
				needle = -(cameraDirection * Math.PI / 180 - Math.atan2(X, Z));
				System.out.println(X);
			}
			

		}

		return needle;
	}
}
