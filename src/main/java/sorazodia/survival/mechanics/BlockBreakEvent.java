package sorazodia.survival.mechanics;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
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

	@SubscribeEvent
	public void blockBreak(BreakEvent breakEvent)
	{
		EntityPlayer player = breakEvent.getPlayer();
		float values[] = ConfigHandler.blockEffectValue;

		if (player instanceof EntityPlayer)
		{
			//Get the block being broken since it will be air when HarvestDropsEvent fires
			block = breakEvent.getState().getBlock();
		}
		else
		{
			return;
		}

		World world = breakEvent.getWorld();
		boolean hasSilkTouch = player.getActiveHand() != null && player.getHeldItem(player.getActiveHand()) != null && (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player.getHeldItem(player.getActiveHand())) > 0);
		
		if (!hasSilkTouch)
		{
			if (ConfigHandler.doNetherBlockEffect())
			{
				if (block == Blocks.NETHER_WART)
				{
					float damage = values[2];
					switch (world.getDifficulty())
					{
					case NORMAL:
						damage *= values[3];
						break;
					case HARD:
						damage *= Math.pow(values[3], 2);
						break;
					default:
						break;
					}
					player.attackEntityFrom(DamageSource.MAGIC, damage);
				}
				if (block == Blocks.SOUL_SAND)
				{
					float duration = values[0];
					switch (world.getDifficulty())
					{
					case NORMAL:
						duration *= values[1];
						System.out.print(duration);
						break;
					case HARD:
						duration *= Math.pow(values[1], 2);
						System.out.print(duration);
						break;
					default:
						break;
					}
					player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, (int) duration));
				}
			}
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
						drop.setCount(0);
						player.entityDropItem(item, item.getCount());
					}

					world.setBlockState(blockLocation, Blocks.FLOWING_LAVA.getStateFromMeta(8));
				}

			}
		}
	}
}
