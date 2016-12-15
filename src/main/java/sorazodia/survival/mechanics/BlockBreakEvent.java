package sorazodia.survival.mechanics;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sorazodia.survival.config.ConfigHandler;

public class BlockBreakEvent
{
	private Block block = Blocks.AIR;
	
	//Get the block being broken since it will be air when HarvestDropsEvent fires
	@SubscribeEvent
	public void blockBreak(BreakEvent breakEvent)
	{
		if (breakEvent.getPlayer() != null)
		{
			block = breakEvent.getState().getBlock();
		}
	}

	@SubscribeEvent
	public void netherHarvest(HarvestDropsEvent harvestEvent)
	{
		if (harvestEvent.getHarvester() == null)
			return;

		EntityPlayer player = harvestEvent.getHarvester();
		ItemStack heldItem = player.getActiveItemStack();
		World world = harvestEvent.getWorld();
		BlockPos blockLocation = harvestEvent.getPos();

		if (!player.capabilities.isCreativeMode)
		{
			if (!harvestEvent.isSilkTouching())
			{
				if (ConfigHandler.spawnLava() && (block == Blocks.NETHERRACK || block == Blocks.QUARTZ_ORE) && heldItem != null && heldItem.getItem().canHarvestBlock(block.getDefaultState()))//; <- This little bugger cause all blocks mined to spawn lava regardless of config settings 
				{
					for (ItemStack drop : harvestEvent.getDrops())
					{
						ItemStack item = drop.copy();
						drop.stackSize = 0;
						player.entityDropItem(item, item.stackSize);
					}

					world.setBlockState(blockLocation, Blocks.FLOWING_LAVA.getStateFromMeta(8));
				}

				if (ConfigHandler.doNetherBlockEffect())
				{
					if (block == Blocks.NETHER_WART)
					{
						float damage = 0;
						switch (world.getDifficulty())
						{
						case PEACEFUL:
							damage = 1.0F;
							break;
						case EASY:
							damage = 1.0F;
							break;
						case NORMAL:
							damage = 2.0F;
							break;
						case HARD:
							damage = 4.0F;
							break;
						default:
							damage = 2.0F;
							break;
						}
						player.attackEntityFrom(DamageSource.magic, damage);
					}
					if (block == Blocks.SOUL_SAND)
					{
						int duration = 0;
						switch (world.getDifficulty())
						{
						case PEACEFUL:
							duration = 50;
							break;
						case EASY:
							duration = 50;
							break;
						case NORMAL:
							duration = 130;
							break;
						case HARD:
							duration = 260;
							break;
						default:
							duration = 130;
							break;
						}
						player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, duration));
					}
				}
			}
		}
	}
}