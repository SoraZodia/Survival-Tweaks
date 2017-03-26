package sorazodia.survival.mechanics;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
		EntityLivingBase entity = updateEvent.getEntityLiving();

		if (ConfigHandler.applyStepAssist())
			stepAssist(entity);

		if (ConfigHandler.doBurn())
			burnPlayer(entity);

		if (ConfigHandler.doCollision())
			entityCollide(entity);

	}

	private void entityCollide(EntityLivingBase entity)
	{
		double r = 0.32;

		AxisAlignedBB box = new AxisAlignedBB(entity.posX, entity.posY, entity.posZ, entity.posX + r, entity.posY + r, entity.posZ + r);
		for (Object o : entity.world.getEntitiesWithinAABB(EntityLivingBase.class, box))
		{
			EntityLivingBase living = (EntityLivingBase) o;
			entity.applyEntityCollision(living);
		}
	}

	private void stepAssist(EntityLivingBase entity)
	{

		if (entity.getActivePotionEffect(MobEffects.JUMP_BOOST) != null && entity.isSprinting())
		{
			assistIncrease = entity.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1;

			if (assistIncrease > ConfigHandler.getMaxBoost() && ConfigHandler.getMaxBoost() > -1)
				assistIncrease = ConfigHandler.getMaxBoost();

			if (hasStepAssist)
				entity.stepHeight = assistIncrease + stepAssistBoost;
			else
				entity.stepHeight = assistIncrease;
		}
		else if (entity.getActivePotionEffect(MobEffects.JUMP_BOOST) != null)
		{
			entity.stepHeight = 1 + stepAssistBoost;
			assistIncrease = 1;
		}
		else
		{
			entity.stepHeight -= assistIncrease;
			assistIncrease = 0;
			if (entity.stepHeight >= 1.0)
			{
				hasStepAssist = true;
				stepAssistBoost = entity.stepHeight;
			}
			else
			{
				hasStepAssist = false;
				stepAssistBoost = 0;
			}
		}

	}

	private void burnPlayer(EntityLivingBase entity)
	{
		if (entity.dimension == -1 && entity.ticksExisted % 50 == 0 && !entity.isAirBorne)
		{
			World world = entity.world;
			BlockPos entityPos = entity.getPosition();
			BlockPos ground = new BlockPos(entityPos.getX(), entityPos.getY() - 1, entityPos.getZ());
			Block block = world.getBlockState(ground).getBlock();
			int burnTime = 5;

			if ((entity instanceof EntityPlayer && !((EntityPlayer) entity).capabilities.isCreativeMode || !(entity instanceof EntityPlayer)) && ((entity.getActivePotionEffect(MobEffects.FIRE_RESISTANCE) == null || !entity.isImmuneToFire())) && (block == Blocks.NETHERRACK || block == Blocks.QUARTZ_ORE))
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
