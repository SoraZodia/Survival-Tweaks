package sorazodia.survival.asm.patch;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import sorazodia.survival.main.SurvivalTweaks;

public class ItemBlockInteraction
{
	public static void placeBlocks(World world, EntityPlayer player, ItemStack heldItem, int x, int y, int z, int face, boolean blockActivated)
	{
		InventoryPlayer inventory = player.inventory;
		int heldItemIndex = inventory.currentItem;
		ItemStack toPlace = player.inventory.getStackInSlot((heldItemIndex + 1) % 9);
		
		if (blockActivated)
			return;

		if (toPlace == null || !(toPlace.getItem() instanceof ItemBlock))
		{
			if (heldItemIndex - 1 >= 0)
				toPlace = player.inventory.getStackInSlot((heldItemIndex - 1) % 9);
			else
				toPlace = player.inventory.getStackInSlot(8); //Stops a ArrayOutOfBoundsException... % don't like negatives for some reasons...
		}

		if (toPlace != null && toPlace.getItem() instanceof ItemBlock)
		{
			ItemBlock itemBlock = (ItemBlock) toPlace.getItem();
			ForgeDirection offset = ForgeDirection.getOrientation(face);
			Block targetBlock = world.getBlock(x, y, z);
			boolean isPlayerCreative = player.capabilities.isCreativeMode;

			player.swingItem();
			if (!player.isSneaking())
			{
				x += offset.offsetX;
				y += offset.offsetY;
				z += offset.offsetZ;

				if (world.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1)).size() == 0)
				{
					SurvivalTweaks.playSound("dig.stone", world, player);

					if (!world.isRemote)
						itemBlock.placeBlockAt(toPlace, player, world, x, y, z, face, (float) x, (float) y, (float) z, toPlace.getItemDamage());
					if (!isPlayerCreative)
						inventory.consumeInventoryItem(toPlace.getItem());
				}

			} else if (heldItem.getItem().canHarvestBlock(targetBlock, heldItem) || canItemHarvest(heldItem, targetBlock) || world.getBlock(x, y, z).getHarvestTool(toPlace.getItemDamage()) == null)
			{
				if (targetBlock == Blocks.bedrock)
					return;

				SurvivalTweaks.playSound("dig.stone", world, player);

				if (!world.isRemote)
				{
					targetBlock.harvestBlock(world, player, x, y, z, world.getBlockMetadata(x, y, z));
					itemBlock.placeBlockAt(toPlace, player, world, x, y, z, face, (float) x, (float) y, (float) z, toPlace.getItemDamage());
				}
				if (!isPlayerCreative)
				{
					heldItem.damageItem(1, player);
					inventory.consumeInventoryItem(toPlace.getItem());
				}
			}

		}

	}

	private static boolean canItemHarvest(ItemStack harvestItem, Block blockToBreak)
	{
		for (String classes : harvestItem.getItem().getToolClasses(harvestItem))
		{
			if (blockToBreak.isToolEffective(classes, harvestItem.getItemDamage()))
				return true;
		}
		return false;
	}
}
