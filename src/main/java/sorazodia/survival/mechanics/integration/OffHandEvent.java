package sorazodia.survival.mechanics.integration;

import java.util.Optional;

import mods.battlegear2.api.PlayerEventChild.OffhandSwingEvent;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class OffHandEvent
{
	public static Optional<ItemStack> offItem = Optional.empty();
	
	@SubscribeEvent
	public void offHandSwing(OffhandSwingEvent event)
	{
		if (event.onBlock())
		{
			offItem = Optional.ofNullable(event.offHand);
		}
	}
}
