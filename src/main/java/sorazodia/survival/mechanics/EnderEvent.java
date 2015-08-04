package sorazodia.survival.mechanics;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EnderEvent
{
	@SubscribeEvent
	public void teleInEnd(EnderTeleportEvent enderEvent)
	{
		if (enderEvent.entityLiving instanceof EntityPlayer && enderEvent.entityLiving.dimension == 1)
		{
			enderEvent.attackDamage = 0;
		}
	}
}
