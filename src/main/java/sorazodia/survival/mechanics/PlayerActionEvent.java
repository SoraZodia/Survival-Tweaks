package sorazodia.survival.mechanics;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityMoveHelper.Action;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sorazodia.survival.config.ConfigHandler;
import sorazodia.survival.main.SurvivalTweaks;

public class PlayerActionEvent
{

//	@SubscribeEvent
//	public void onEntityAttack(LivingHurtEvent hurtEvent)
//	{
//		if (ConfigHandler.allowSwordProtection() && hurtEvent.getEntity() instanceof EntityPlayer && hurtEvent.getSource() instanceof EntityDamageSource)
//		{
//			EntityPlayer player = (EntityPlayer) hurtEvent.getEntity();
//
//			if (player.isUsingItem() && player.inventory.getCurrentItem().getItem() instanceof ItemSword)
//			{
//
//				player.inventory.getCurrentItem().damageItem((int) hurtEvent.ammount, player);
//				hurtEvent.ammount /= 2;
//
//				if (player.isSneaking())
//				{
//					hurtEvent.ammount = 0;
//					player.knockBack(player, 0, 0, 0);
//					player.swingItem();
//					player.setSneaking(false);
//				}
//			}
//		}
//	}

	@SubscribeEvent
	public void bowDraw(ArrowLooseEvent arrowEvent)
	{
		if (ConfigHandler.applyBowPotionBoost())
			arrowEvent.setCharge((int) calculateDamage(arrowEvent.getCharge(), arrowEvent.getEntityLiving()));
	}

	@SubscribeEvent
	public void itemRightClick(RightClickItem event)
	{
		EntityPlayer player = event.getEntityPlayer();
		World world = event.getWorld();
		ItemStack heldStack = event.getItemStack();
		
		if (heldStack == null)
			return;
		
		if (ConfigHandler.doArmorSwap() && heldStack.getItem() instanceof ItemArmor)
			switchArmor(player, world, heldStack);

		if (ConfigHandler.doArrowThrow() && heldStack.getItem() == Items.ARROW)
			throwArrow(world, player, heldStack);
	}
	
	@SubscribeEvent
	public void blockRightClick(RightClickBlock event)
	{
		EntityPlayer player = event.getEntityPlayer();
		ItemStack heldStack = event.getItemStack();
		World world = event.getWorld();
		EnumFacing offset = event.getFace();
		IBlockState blockState = world.getBlockState(event.getPos());
		
		if (heldStack == null)
			return;
		
		if (player.isSneaking() || (offset != null && event.getUseBlock() != Result.ALLOW))
		{
			if (ConfigHandler.doArmorSwap() && heldStack.getItem() instanceof ItemArmor)
				switchArmor(player, world, heldStack);

			if (ConfigHandler.doArrowThrow() && heldStack.getItem() == Items.ARROW)
				throwArrow(world, player, heldStack);

			if (ConfigHandler.doToolBlockPlace() && (heldStack.getItem() instanceof ItemTool || heldStack.getItem().isDamageable()))
				placeBlocks(world, player, blockState, blockState.getBlock(), heldStack, event.getPos(), offset);
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
			boolean canHarvest = heldStack.getItem().canHarvestBlock(targetBlock.getBlockState().getBaseState(), heldStack) || canItemHarvest(heldStack, targetBlock, blockState) || (toPlace.getHasSubtypes() && targetBlock.getHarvestTool(blockState) == null);
			IBlockState heldBlock = Block.getBlockFromItem(toPlace.getItem()).getStateFromMeta(toPlace.getMetadata());

			//player.swingItem();

			if (player.isSneaking() && canHarvest)
			{
				if (targetBlock == Blocks.BEDROCK)
					return;

				SurvivalTweaks.playSound(targetBlock.getSoundType().getBreakSound(), world, player);

				if (!world.isRemote)
				{
					targetBlock.harvestBlock(world, player, pos, blockState, blockState.getBlock().createTileEntity(world, blockState), heldStack);
					world.setBlockState(pos, heldBlock);
				}
				if (!isPlayerCreative)
				{
					heldStack.damageItem(1, player);
					toPlace.stackSize--;
				}
			}
			else
			{
				pos = pos.add(offset.getFrontOffsetX(), offset.getFrontOffsetY(), offset.getFrontOffsetZ());

				if (world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)).size() == 0 && targetBlock.canPlaceBlockAt(world, pos))
				{
					SurvivalTweaks.playSound(heldBlock.getBlock().getSoundType().getBreakSound(), world, player);

					if (!world.isRemote)
					{
						world.setBlockState(pos, heldBlock);
					}
					if (!isPlayerCreative)
						toPlace.stackSize--;
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

		SurvivalTweaks.playSound(SoundEvents.ENTITY_ARROW_SHOOT, world, player);
		
		//player.swingItem();
		
		if (!world.isRemote)
		{
			ItemArrow itemArrow = (ItemArrow)heldItem.getItem();
			EntityArrow arrow = itemArrow.createArrow(world, heldItem, player);
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
		int armorIndex = ((ItemArmor)heldItem.getItem()).armorType.getIndex();

		if (inventory.armorInventory[armorIndex] == null || heldItem.getItem().getUnlocalizedName().equals("item.openblocks.sleepingbag")) //Bandage fix for now
			return;

		ItemStack equipedArmor = inventory.armorInventory[armorIndex];

		inventory.armorInventory[armorIndex] = heldItem;

		if (!player.capabilities.isCreativeMode)
			inventory.mainInventory[heldItemIndex] = equipedArmor;

		SurvivalTweaks.playSound(SoundEvents.ENTITY_IRONGOLEM_ATTACK, world, player);
	}

	private double calculateDamage(double damage, EntityLivingBase entity)
	{
		if (entity.getActivePotionEffect(MobEffects.STRENGTH) != null)
		{
			PotionEffect strength = entity.getActivePotionEffect(MobEffects.STRENGTH);

			damage *= (1.30 * (strength.getAmplifier() + 1));
		}
		if (entity.getActivePotionEffect(MobEffects.WEAKNESS) != null)
		{
			PotionEffect weakness = entity.getActivePotionEffect(MobEffects.WEAKNESS);
			double reduction = damage * (0.5 * (weakness.getAmplifier() + 1));

			damage -= reduction;
		}

		return damage;
	}

}
