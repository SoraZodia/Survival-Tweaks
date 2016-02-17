package sorazodia.survival.mechanics;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sorazodia.survival.config.ConfigHandler;

public class EntityTickEvent
{
	private float assistIncrease = 0;
	private boolean hasStepAssist = false;
	private float stepAssistBoost = 0;

	@SubscribeEvent
	public void playerUpdate(LivingUpdateEvent updateEvent)
	{
		EntityLivingBase entity = updateEvent.entityLiving;

		if (ConfigHandler.applyStepAssist())
			stepAssist(entity);

		if (ConfigHandler.doBurn())
			burnPlayer(entity);

		if (ConfigHandler.doCollision())
			entityCollide(entity);

	}

	private void entityCollide(EntityLivingBase entity)
	{
		double r = 0.22;
		AxisAlignedBB box = entity.getCollisionBoundingBox().expand(r, 0.0, r);
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
			BlockPos entityPos = entity.getPosition();
			BlockPos ground = new BlockPos(entityPos.getX(), entityPos.getY() - 1, entityPos.getZ());
			Block block = world.getBlockState(ground).getBlock();
			int burnTime = 5;

			if (entity instanceof EntityPlayer)
			{
				EntityPlayer player = (EntityPlayer) entity;
				if (!player.capabilities.isCreativeMode && player.getActivePotionEffect(Potion.fireResistance) == null && (block == Blocks.netherrack || block == Blocks.quartz_ore))
				{
					switch (world.getDifficulty())
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
				switch (world.getDifficulty())
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
