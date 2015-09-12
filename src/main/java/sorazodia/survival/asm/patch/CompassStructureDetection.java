package sorazodia.survival.asm.patch;

import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

public class CompassStructureDetection
{
	public static double findStructure(World world, double x, double z, double needle, double cameraDirection, boolean bool)
	{
		if (world != null && !bool)
		{
			if (world.provider.isHellWorld)
			{
				ChunkPosition locator = world.findClosestStructure("Fortress", (int) x, 70, (int) z);

				if (locator != null)
				{
					double X = locator.chunkPosX - x;
					double Z = locator.chunkPosZ - z;
					needle = -(cameraDirection * Math.PI / 180 - Math.atan2(X, Z));
				}
			}

		}

		return needle;
	}
}
