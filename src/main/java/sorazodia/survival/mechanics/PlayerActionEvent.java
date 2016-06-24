package sorazodia.survival.mechanics;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sorazodia.survival.config.ConfigHandler;
import sorazodia.survival.main.SurvivalTweaks;

public class PlayerActionEvent
{

	@SubscribeEvent
	public void onEntityAttack(LivingHurtEvent hurtEvent)
	{
		if (ConfigHandler.allowSwordProtection() && hurtEvent.entity instanceof EntityPlayer && hurtEvent.source instanceof EntityDamageSource)
		{
			EntityPlayer player = (EntityPlayer) hurtEvent.entity;

			if (player.isUsingItem() && player.inventory.getCurrentItem().getItem() instanceof ItemSword)
			{

				player.inventory.getCurrentItem().damageItem((int) hurtEvent.ammount, player);
				hurtEvent.ammount /= 2;

				if (player.isSneaking())
				{
					hurtEvent.ammount = 0;
					player.knockBack(player, 0, 0, 0);
					player.swingItem();
					player.setSneaking(false);
				}
			}
		}
	}

	@SubscribeEvent
	public void bowDraw(ArrowLooseEvent arrowEvent)
	{
		if (ConfigHandler.applyBowPotionBoost())
			arrowEvent.charge = (int) calculateDamage(arrowEvent.charge, arrowEvent.entityLiving);
	}

	@SubscribeEvent
	public void itemRightClick(PlayerInteractEvent interactEvent)
	{
		EntityPlayer player = interactEvent.entityPlayer;
		World world = interactEvent.world;
		BlockPos pos = interactEvent.pos;
		EnumFacing offset = interactEvent.face;
		IBlockState blockState = null;
		Block targetBlock = null;
		ItemStack heldStack = player.getCurrentEquippedItem();

		if (pos != null)
		{
			blockState = world.getBlockState(interactEvent.pos);
			targetBlock = blockState.getBlock();
		}

		if (heldStack == null)
			return;

		Item heldItem = heldStack.getItem();

		if (interactEvent.action == Action.RIGHT_CLICK_BLOCK)
		{
			if (player.isSneaking() || (offset != null && (!targetBlock.hasTileEntity(blockState))))
			{
				if (ConfigHandler.doArmorSwap() && heldItem instanceof ItemArmor && !targetBlock.onBlockActivated(world, pos, blockState, player, offset, offset.getFrontOffsetX(), offset.getFrontOffsetY(), offset.getFrontOffsetZ()))
					switchArmor(player, world, heldStack);

				if (ConfigHandler.doArrowThrow() && heldItem == Items.arrow && !targetBlock.onBlockActivated(world, pos, blockState, player, offset, offset.getFrontOffsetX(), offset.getFrontOffsetY(), offset.getFrontOffsetZ()))
					throwArrow(world, player, heldStack);

				if (ConfigHandler.doToolBlockPlace() && (heldItem instanceof ItemTool || heldItem.isDamageable()) && !targetBlock.onBlockActivated(world, pos, blockState, player, offset, offset.getFrontOffsetX(), offset.getFrontOffsetY(), offset.getFrontOffsetZ()))
					placeBlocks(world, player, blockState, targetBlock, heldStack, pos, offset);
			}
		}
		else if (interactEvent.action == Action.RIGHT_CLICK_AIR)
		{
			if (ConfigHandler.doArmorSwap() && heldItem instanceof ItemArmor)
				switchArmor(player, world, heldStack);

			if (ConfigHandler.doArrowThrow() && heldItem == Items.arrow)
				throwArrow(world, player, heldStack);
		}

	}

	public void placeBlocks(World world, EntityPlayer player, IBlockState blockState, Block targetBlock, ItemStack heldStack, BlockPos pos, EnumFacing offset)
	{
		InventoryPlayer inventory = player.inventory;
		int heldItemIndex = inventory.currentItem;
		ItemStack toPlace = inventory.getStackInSlot((heldItemIndex + 1) % 9);

		if (!(heldStack.getItem() instanceof ItemTool))
			return;

		if (toPlace == null || !(toPlace.getItem() instanceof ItemBlock))
		{
			if (heldItemIndex - 1 >= 0)
				toPlace = player.inventory.getStackInSlot((heldItemIndex - 1) % 9);
			else
				toPlace = player.inventory.getStackInSlot(8); //Stops a ArrayOutOfBoundsException... % don't like negative
		}

		if (toPlace != null && toPlace.getItem() instanceof ItemBlock)
		{
			boolean isPlayerCreative = player.capabilities.isCreativeMode;
			boolean canHarvest = heldStack.getItem().canHarvestBlock(targetBlock, heldStack) || canItemHarvest(heldStack, targetBlock, blockState) || (toPlace.getHasSubtypes() && targetBlock.getHarvestTool(blockState) == null);
			IBlockState heldBlock = Block.getBlockFromItem(toPlace.getItem()).getStateFromMeta(toPlace.getMetadata());

			player.swingItem();

			if (player.isSneaking() && canHarvest)
			{
				if (targetBlock == Blocks.bedrock)
					return;

				SurvivalTweaks.playSound(targetBlock.stepSound.getBreakSound(), world, player);

				if (!world.isRemote)
				{
					targetBlock.harvestBlock(world, player, pos, blockState, blockState.getBlock().createTileEntity(world, blockState));
					world.setBlockState(pos, heldBlock);
				}
				if (!isPlayerCreative)
				{
					heldStack.damageItem(1, player);
					inventory.consumeInventoryItem(toPlace.getItem());
				}
			}
			else
			{
				pos = pos.add(offset.getFrontOffsetX(), offset.getFrontOffsetY(), offset.getFrontOffsetZ());

				if (world.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.fromBounds(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)).size() == 0 && targetBlock.canPlaceBlockAt(world, pos))
				{
					SurvivalTweaks.playSound(heldBlock.getBlock().stepSound.getBreakSound(), world, player);

					if (!world.isRemote)
					{
						world.setBlockState(pos, heldBlock);
					}
					if (!isPlayerCreative)
						inventory.consumeInventoryItem(toPlace.getItem());
				}
			}
		}
	}

	private static boolean canItemHarvest(ItemStack harvestItem, Block blockToBreak, IBlockState blockState)
	{
		for (String classes : harvestItem.getItem().getToolClasses(harvestItem))
		{
			return blockToBreak.isToolEffective(classes, blockState);
		}
		return false;
	}

	private void throwArrow(World world, EntityPlayer player, ItemStack heldItem)
	{
		double damage = calculateDamage(4.0, player);

		SurvivalTweaks.playSound("random.bow", world, player);
		
		player.swingItem();
		
		if (!world.isRemote)
		{
			EntityArrow arrow = new EntityArrow(world, player, (float) calculateDamage(0.5, player));
			arrow.setDamage(damage);
			
			if (!player.capabilities.isCreativeMode)
				heldItem.stackSize--;

			world.spawnEntityInWorld(arrow);
		}
	}

	private void switchArmor(EntityPlayer player, World world, ItemStack heldItem)
	{
		InventoryPlayer inventory = player.inventory;
		int heldItemIndex = player.inventory.currentItem;
		int armorIndex = EntityLiving.getArmorPosition(heldItem) - 1;

		if (player.getCurrentArmor(armorIndex) == null || heldItem.getItem().getUnlocalizedName().equals("item.openblocks.sleepingbag")) //Bandage fix for now
			return;

		ItemStack equipedArmor = player.getCurrentArmor(armorIndex);

		inventory.armorInventory[armorIndex] = heldItem;

		if (!player.capabilities.isCreativeMode)
			inventory.mainInventory[heldItemIndex] = equipedArmor;

		SurvivalTweaks.playSound("mob.irongolem.throw", world, player);
	}

	private double calculateDamage(double damage, EntityLivingBase entity)
	{
		if (entity.getActivePotionEffect(Potion.damageBoost) != null)
		{
			PotionEffect strength = entity.getActivePotionEffect(Potion.damageBoost);

			damage *= (1.30 * (strength.getAmplifier() + 1));
		}
		if (entity.getActivePotionEffect(Potion.weakness) != null)
		{
			PotionEffect weakness = entity.getActivePotionEffect(Potion.weakness);
			double reduction = damage * (0.5 * (weakness.getAmplifier() + 1));

			damage -= reduction;
		}

		return damage;
	}

}
