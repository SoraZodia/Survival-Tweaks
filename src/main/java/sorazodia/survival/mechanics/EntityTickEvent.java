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
	private boolean hasStepAssist = false;
	private float stepAssistBoost = 0;

	@SubscribeEvent
	public void playerUpdate(LivingUpdateEvent updateEvent)
	{
		EntityLivingBase entity = updateEvent.entityLiving;

		if (entity.getActivePotionEffect(Potion.jump) != null)
		{
			assistIncrease = entity.getActivePotionEffect(Potion.jump).getAmplifier() + 1;

			if (hasStepAssist)
			{
				entity.stepHeight = assistIncrease + stepAssistBoost;
			} else
				entity.stepHeight = assistIncrease;
		} else
		{
			entity.stepHeight -= assistIncrease;
			assistIncrease = 0;
			if (entity.stepHeight >= 1.0)
			{
				hasStepAssist = true;
				stepAssistBoost = entity.stepHeight;
			}
		}

		if (ConfigHandler.getBurn() && entity.dimension == -1)
		{
			World world = updateEvent.entityLiving.worldObj;
			Block block = world.getBlock((int) entity.posX, (int) entity.posY - 1, (int) entity.posZ);
			int burnTime = 5;

			if (entity instanceof EntityPlayer)
			{
				EntityPlayer player = (EntityPlayer) entity;
				if (!player.capabilities.isCreativeMode && player.getActivePotionEffect(Potion.fireResistance) == null && (block == Blocks.netherrack || block == Blocks.quartz_ore))
				{
					switch (world.difficultySetting)
					{
					case PEACEFUL:
						burnTime = 1;
						break;
					case EASY:
						burnTime = 1;
						break;
					case NORMAL:
						burnTime = 5;
						break;
					case HARD:
						burnTime = 10;
						break;
					default:
						burnTime = 5;
						break;
					}
					player.setFire(burnTime);
				}
			} else if (((entity.getActivePotionEffect(Potion.fireResistance) == null || !entity.isImmuneToFire())) && (block == Blocks.netherrack || block == Blocks.quartz_ore))
			{
				switch (world.difficultySetting)
				{
				case PEACEFUL:
					burnTime = 10;
					break;
				case EASY:
					burnTime = 10;
					break;
				case NORMAL:
					burnTime = 5;
					break;
				case HARD:
					burnTime = 1;
					break;
				default:
					burnTime = 5;
					break;
				}
				entity.setFire(burnTime);

			}
		}

	}

}
