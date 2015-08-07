package sorazodia.survival.mechanics;

import sorazodia.survival.config.ConfigHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
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
			assistIncrease = entity.getActivePotionEffect(Potion.jump).getAmplifier() + 1;

			if (entity.stepHeight == 1.0 || entity.stepHeight - assistIncrease == 1.0)
			{
				entity.stepHeight = assistIncrease + 1;
			} else
				entity.stepHeight = assistIncrease;
		} else
		{
			entity.stepHeight -= assistIncrease;
			assistIncrease = 0;
		}

		if (ConfigHandler.getBurn() && entity.dimension == -1)
		{
			World world = updateEvent.entityLiving.worldObj;
			Block block = world.getBlock((int) entity.posX, (int) entity.posY - 1, (int) entity.posZ);
			
			if (entity instanceof EntityPlayer)
			{
				EntityPlayer player = (EntityPlayer) entity;
				if (!player.capabilities.isCreativeMode && player.getActivePotionEffect(Potion.fireResistance) == null && (block == Blocks.netherrack || block == Blocks.quartz_ore))
				{
					player.setFire(10);
				}
			} else if (((entity.getActivePotionEffect(Potion.fireResistance) == null || !entity.isImmuneToFire())) && (block == Blocks.netherrack || block == Blocks.quartz_ore))
			{
				entity.setFire(10);

			}
		}
		
	}

}
