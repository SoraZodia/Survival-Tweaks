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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
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
		int face = interactEvent.face.ordinal();
		EntityPlayer player = interactEvent.entityPlayer;
		World world = interactEvent.world;
		EnumFacing offset = interactEvent.face;
		BlockPos pos = interactEvent.pos;
		IBlockState blockState = world.getBlockState(interactEvent.pos);
		Block targetBlock = blockState.getBlock();

		if (player.getCurrentEquippedItem() != null && interactEvent.action != Action.LEFT_CLICK_BLOCK)
		{
			ItemStack heldStack = player.getCurrentEquippedItem();
			Item heldItem = heldStack.getItem();

			if (player.isSneaking() || (!targetBlock.hasTileEntity(blockState) && !targetBlock.onBlockActivated(world, pos, blockState, player, offset, (float) offset.getFrontOffsetX(), (float) offset.getFrontOffsetY(), (float) offset.getFrontOffsetZ())))
			{
				if (ConfigHandler.doArmorSwap() && heldItem instanceof ItemArmor)
					switchArmor(player, world, heldStack);

				if (ConfigHandler.doArrowThrow() && heldItem == Items.arrow)
					throwArrow(world, player, heldStack);

				if (ConfigHandler.doToolBlockPlace() && (heldItem instanceof ItemTool || heldItem.isDamageable()) && interactEvent.action == Action.RIGHT_CLICK_BLOCK)
					placeBlocks(world, player, blockState, targetBlock, heldStack, pos, face, offset);
			}
		}

	}

	public void placeBlocks(World world, EntityPlayer player, IBlockState blockState, Block targetBlock, ItemStack heldStack, BlockPos pos, int face, EnumFacing offset)
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
			ItemBlock itemBlock = (ItemBlock) toPlace.getItem();
			boolean isPlayerCreative = player.capabilities.isCreativeMode;
			boolean canHarvest = heldStack.getItem().canHarvestBlock(targetBlock, heldStack) || canItemHarvest(heldStack, targetBlock, blockState) || (toPlace.getHasSubtypes() && targetBlock.getHarvestTool(blockState) == null);
            int x = pos.getX();
            int y = pos.getY();
			int z = pos.getZ();
			
			BlockSnapshot snapshot = new BlockSnapshot(world, pos, (IBlockState) Block.getBlockFromItem(toPlace.getItem()).getBlockState(), toPlace.getTagCompound());
			BlockEvent.PlaceEvent blockEvent = new BlockEvent.PlaceEvent(snapshot, blockState, player);
			PlayerInteractEvent interactEvent = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, pos, offset, world);
			MinecraftForge.EVENT_BUS.post(blockEvent);
			MinecraftForge.EVENT_BUS.post(interactEvent);

			if (blockEvent.getResult() == Result.DENY || blockEvent.isCanceled() || interactEvent.getResult() == Result.DENY || interactEvent.isCanceled())
				return;
			
			player.swingItem();

			if (player.isSneaking() && canHarvest)
			{
				if (targetBlock == Blocks.bedrock)
					return;

				SurvivalTweaks.playSound(targetBlock.stepSound.getBreakSound(), world, player);

				if (!world.isRemote)
				{
					targetBlock.harvestBlock(world, player, pos, blockState, blockState.getBlock().createTileEntity(world, blockState));
					itemBlock.placeBlockAt(toPlace, player, world, pos, offset, x, y, z, blockState);
				}
				if (!isPlayerCreative)
				{
					heldStack.damageItem(1, player);
					inventory.consumeInventoryItem(toPlace.getItem());
				}
			} else
			{
				x += offset.getFrontOffsetX();
				y += offset.getFrontOffsetY();
				z += offset.getFrontOffsetZ();

				if (world.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.fromBounds(x, y, z, x + 1, y + 1, z + 1)).size() == 0 && targetBlock.canPlaceBlockAt(world, pos))
				{
					SurvivalTweaks.playSound(itemBlock.getBlock().stepSound.getBreakSound(), world, player);

					if (!world.isRemote)
					{
						itemBlock.placeBlockAt(toPlace, player, world, pos, offset, x, y, z, blockState);
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

		EntityArrow arrow = new EntityArrow(world, player, (float) calculateDamage(0.5, player));
		arrow.setDamage(damage);

		player.swingItem();

		SurvivalTweaks.playSound("random.bow", world, player);

		if (!player.capabilities.isCreativeMode && !world.isRemote)
			heldItem.stackSize--;

		if (!world.isRemote)
			world.spawnEntityInWorld(arrow);
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
