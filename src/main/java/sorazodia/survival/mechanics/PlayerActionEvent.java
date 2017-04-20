package sorazodia.survival.mechanics;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent;

import org.apache.logging.log4j.Level;

import sorazodia.survival.config.ConfigHandler;
import sorazodia.survival.main.SurvivalTweaks;
import sorazodia.survival.mechanics.trackers.BlackListTracker;
import sorazodia.survival.mechanics.trackers.ParachuteTracker;
import sorazodia.survival.mechanics.trackers.WhiteListTracker;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class PlayerActionEvent
{
	private WhiteListTracker whitelist = (WhiteListTracker) SurvivalTweaks.getWhiteListTracker();
	private BlackListTracker blacklist = (BlackListTracker) SurvivalTweaks.getBlackListTracker();

	@SubscribeEvent
	public void playerTick(PlayerTickEvent event)
	{
		EntityPlayer player = event.player;

		if (player.getHeldItem() != null && player.motionY <= -0.5 && Math.abs(player.rotationPitch) == 90)
		{
			ItemStack stack = player.getHeldItem();

			if (SurvivalTweaks.getParachuteTracker().isValid(stack.getItem()))
			{
				if (player.isUsingItem())
					softenFall(player, stack);
				else if (stack.getItemUseAction() != EnumAction.block)
				{
					if (player.motionY == -1000)
						SurvivalTweaks.getLogger().printf(Level.INFO, "[%s] AHHHHHHHHHHHHHHHHH AHHHHHH AHHHHHHHHHHHHHHHHH I BELIEVE I CAN FLYYYYYYY",
								player.getDisplayName());

					player.swingProgress = 0.85F;
					softenFall(player, stack);
				}
			}
		}

	}

	private void softenFall(EntityPlayer player, ItemStack stack)
	{
		player.motionY /= 1.5;
		player.fallDistance /= 1.5;
		stack.damageItem(1, player);
	}

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
		int x = interactEvent.x;
		int y = interactEvent.y;
		int z = interactEvent.z;
		int face = interactEvent.face;
		EntityPlayer player = interactEvent.entityPlayer;
		World world = interactEvent.world;
		ForgeDirection offset = ForgeDirection.getOrientation(face);
		Block block = world.getBlock(x, y, z);

		if (player.getCurrentEquippedItem() != null && interactEvent.action != Action.LEFT_CLICK_BLOCK)
		{
			ItemStack heldStack = player.getCurrentEquippedItem();
			Item heldItem = heldStack.getItem();

			if (player.isSneaking()
					|| (!block.hasTileEntity(block.getDamageValue(world, x, y, z)) && !block.onBlockActivated(world, x, y, x, player, face, offset.offsetX,
							offset.offsetY, offset.offsetZ)))
			{
				if (interactEvent.action == Action.RIGHT_CLICK_AIR)
				{
					if (ConfigHandler.doArmorSwap() && heldItem instanceof ItemArmor)
						switchArmor(player, world, heldStack);

					if (ConfigHandler.doArrowThrow() && heldItem == Items.arrow)
						throwArrow(world, player, heldStack);
				}

				if (ConfigHandler.doToolBlockPlace() && interactEvent.action == Action.RIGHT_CLICK_BLOCK)
					if (!blacklist.isValid(heldItem) || whitelist.isValid(heldItem))
						placeBlocks(world, player, heldStack, x, y, z, face, offset);
			}
		}

	}

	private void placeBlocks(World world, EntityPlayer player, ItemStack heldStack, int x, int y, int z, int face, ForgeDirection offset)
	{
		InventoryPlayer inventory = player.inventory;
		int heldItemIndex = inventory.currentItem;

		System.out.println(heldItemIndex);

		if (heldItemIndex >= 9)
		{
			heldItemIndex = -1; // For Mine and Blade dual weapons
		}

		ItemStack toPlace = inventory.getStackInSlot((heldItemIndex + 1) % 9);

		if (!(heldStack.getItem() instanceof ItemTool || whitelist.isValid(heldStack.getItem())))
			return;

		if (toPlace == null || !(toPlace.getItem() instanceof ItemBlock))
		{
			if (heldItemIndex - 1 >= 0)
				toPlace = player.inventory.getStackInSlot((heldItemIndex - 1) % 9);
			else
				toPlace = player.inventory.getStackInSlot(8); // Stops a
																// ArrayOutOfBoundsException...
																// % don't like
																// negative
		}

		if (toPlace != null && toPlace.getItem() instanceof ItemBlock)
		{
			ItemBlock itemBlock = (ItemBlock) toPlace.getItem();
			Block targetBlock = world.getBlock(x, y, z);
			boolean isPlayerCreative = player.capabilities.isCreativeMode;
			boolean canHarvest = heldStack.getItem().canHarvestBlock(targetBlock, heldStack) || canItemHarvest(heldStack, targetBlock)
					|| (toPlace.getHasSubtypes() && world.getBlock(x, y, z).getHarvestTool(toPlace.getItemDamage()) == null);

			BlockSnapshot snapshot = new BlockSnapshot(world, x, y, z, Block.getBlockFromItem(toPlace.getItem()), itemBlock.getDamage(toPlace));
			BlockEvent.PlaceEvent blockEvent = new BlockEvent.PlaceEvent(snapshot, targetBlock, player);
			MinecraftForge.EVENT_BUS.post(blockEvent);

			if (blockEvent.getResult() == Result.DENY || blockEvent.isCanceled())
				return;

			player.swingItem();

			if (player.isSneaking() && canHarvest)
			{
				if (targetBlock == Blocks.bedrock)
					return;

				SurvivalTweaks.playSound(targetBlock.stepSound.getBreakSound(), world, player);

				if (!world.isRemote)
				{
					targetBlock.harvestBlock(world, player, x, y, z, world.getBlockMetadata(x, y, z));
					itemBlock.placeBlockAt(toPlace, player, world, x, y, z, face, (float) x, (float) y, (float) z, toPlace.getItemDamage());
				}
				if (!isPlayerCreative)
				{
					heldStack.damageItem(1, player);
					inventory.consumeInventoryItem(toPlace.getItem());
				}
			} else
			{
				x += offset.offsetX;
				y += offset.offsetY;
				z += offset.offsetZ;

				if (world.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1)).size() == 0
						&& targetBlock.canPlaceBlockAt(world, x, y, z))
				{
					SurvivalTweaks.playSound(itemBlock.field_150939_a.stepSound.getBreakSound(), world, player);

					if (!world.isRemote)
					{
						itemBlock.placeBlockAt(toPlace, player, world, x, y, z, face, (float) x, (float) y, (float) z, toPlace.getItemDamage());
					}
					if (!isPlayerCreative)
						inventory.consumeInventoryItem(toPlace.getItem());
				}
			}
		}
	}

	private static boolean canItemHarvest(ItemStack harvestItem, Block blockToBreak)
	{
		for (String classes : harvestItem.getItem().getToolClasses(harvestItem))
		{
			if (blockToBreak.isToolEffective(classes, harvestItem.getItemDamage() % 16))
				return true;
		}
		return false;
	}

	private void throwArrow(World world, EntityPlayer player, ItemStack heldItem)
	{
		double damage = calculateDamage(4.0, player);

		player.swingItem();

		SurvivalTweaks.playSound("random.bow", world, player);

		if (!player.capabilities.isCreativeMode && !world.isRemote)
			heldItem.stackSize--;

		if (!world.isRemote)
		{
			EntityArrow arrow = new EntityArrow(world, player, (float) calculateDamage(0.5, player));
			arrow.setDamage(damage);
			world.spawnEntityInWorld(arrow);
		}
	}

	private void switchArmor(EntityPlayer player, World world, ItemStack heldItem)
	{
		InventoryPlayer inventory = player.inventory;
		int heldItemIndex = player.inventory.currentItem;
		int armorIndex = EntityLiving.getArmorPosition(heldItem) - 1;

		if (player.getCurrentArmor(armorIndex) == null || heldItem.getItem().getUnlocalizedName().equals("item.openblocks.sleepingbag")) // Bandage
																																			// fix
																																			// for
																																			// now
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
