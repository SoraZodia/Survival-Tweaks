package sorazodia.survival.mechanics;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import sorazodia.survival.config.ConfigHandler;
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

		if (ConfigHandler.getStepAssist())
			stepAssist(entity);

		if (ConfigHandler.getBurn())
			burnPlayer(entity);

		if (ConfigHandler.getCollision())
			entityCollide(entity);

	}

	private void entityCollide(EntityLivingBase entity)
	{
		AxisAlignedBB box = entity.boundingBox.expand(0.15, 0.15, 0.15);
		for (Object o : entity.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, box))
		{
			EntityLivingBase living = (EntityLivingBase) o;
			entity.applyEntityCollision(living);
		}
	}

	private void stepAssist(EntityLivingBase entity)
	{
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
	}

	private void burnPlayer(EntityLivingBase entity)
	{
		if (entity.dimension == -1)
		{
			World world = entity.worldObj;
			Block block = world.getBlock((int) entity.posX, (int) entity.posY - 1,
					(int) entity.posZ);
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
				entity.setFire(burnTime);

			}
		}
	}

}
