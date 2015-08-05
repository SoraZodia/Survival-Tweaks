package sorazodia.survival.mechanics;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class EntityTickEvent
{
	
	@SubscribeEvent
	public void playerTick(PlayerTickEvent tickEvent) 
	{
		EntityPlayer player = tickEvent.player;
		player.stepHeight = 0.5F;
		
		if (player.getActivePotionEffect(Potion.jump) != null)
		{
			player.stepHeight = player.getActivePotionEffect(Potion.jump).getAmplifier() + 1;	
		}

	}

}
