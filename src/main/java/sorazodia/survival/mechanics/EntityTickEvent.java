package sorazodia.survival.mechanics;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EntityTickEvent
{
	private float assistIncrease = 0;
    private float pervStepHeight = 0.5F; //Minecraft default
	
	@SubscribeEvent
	public void playerUpdate(LivingUpdateEvent updateEvent)
	{
		EntityLivingBase entity = updateEvent.entityLiving;

		if (entity.getActivePotionEffect(Potion.jump) != null)
		{
			pervStepHeight = Math.abs(entity.stepHeight - assistIncrease);
			assistIncrease = entity.getActivePotionEffect(Potion.jump).getAmplifier() + 1;
			
			entity.stepHeight = assistIncrease + pervStepHeight;
		}
		else
		{
			entity.stepHeight -= assistIncrease;
			assistIncrease = 0;
		}

		System.out.println(entity.stepHeight);

	}

}
