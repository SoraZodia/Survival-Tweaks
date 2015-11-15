package sorazodia.survival.mechanics;

import sorazodia.survival.config.ConfigHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class BlockBreakEvent
{
	@SubscribeEvent
	public void netherHarvest(HarvestDropsEvent harvestEvent)
	{
		if (harvestEvent.harvester == null)
			return;

		EntityPlayer player = harvestEvent.harvester;
		ItemStack heldItem = player.getCurrentEquippedItem();
		Block block = harvestEvent.block;
		World world = harvestEvent.world;
		int x = harvestEvent.x;
		int y = harvestEvent.y;
		int z = harvestEvent.z;

		if (!player.capabilities.isCreativeMode)
		{
			if (!harvestEvent.isSilkTouching)
			{
				if (ConfigHandler.spawnLava() && (block == Blocks.netherrack || block == Blocks.quartz_ore) && heldItem != null && heldItem.getItem().canHarvestBlock(
						block, heldItem))
				{
					for (ItemStack drop : harvestEvent.drops)
					{
						ItemStack item = drop.copy();
						drop.stackSize = 0;
						player.entityDropItem(item, item.stackSize);
					}

					world.setBlock(x, y, z, Blocks.flowing_lava);
					world.setBlockMetadataWithNotify(x, y, z, 8, 2);
				}

				if (ConfigHandler.doNetherBlockEffect())
				{
					if (block == Blocks.nether_wart)
					{
						float damage = 0;
						switch (world.difficultySetting)
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
					if (block == Blocks.soul_sand)
					{
						int duration = 0;
						switch (world.difficultySetting)
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
						player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, duration));
					}
				}
			}
		}
	}
}