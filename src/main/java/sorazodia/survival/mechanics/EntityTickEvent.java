package sorazodia.survival.mechanics;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EntityTickEvent
{
	private float assistIncrease = 0;

	@SubscribeEvent
	public void playerUpdate(LivingUpdateEvent updateEvent)
	{
		EntityLivingBase entity = updateEvent.entityLiving;

		if (entity.getActivePotionEffect(Potion.jump) != null)
		{
			entity.stepHeight = assistIncrease;
			assistIncrease = entity.getActivePotionEffect(Potion.jump).getAmplifier() + 1;

			if (entity.stepHeight == 1.0)
			{
				entity.stepHeight = assistIncrease + 1;
			} else
				entity.stepHeight = assistIncrease;

		} else
		{
			entity.stepHeight -= assistIncrease;
			assistIncrease = 0;
		}

	}

}
