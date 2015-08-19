package sorazodia.survival.mechanics;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
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
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import sorazodia.survival.config.ConfigHandler;
import sorazodia.survival.main.SurvivalTweaks;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.relauncher.Side;

public class PlayerActionEvent
{

	@SubscribeEvent
	public void onEntityAttack(LivingHurtEvent hurtEvent)
	{
		if (hurtEvent.entity instanceof EntityPlayer && hurtEvent.source instanceof EntityDamageSource)
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
		arrowEvent.charge = (int) calculateDamage(arrowEvent.charge, arrowEvent.entityLiving);
	}

	@SubscribeEvent
	public void onSleep(PlayerTickEvent tickEvent)
	{
		EntityPlayer player = tickEvent.player;

		if (player.isPlayerFullyAsleep())
		{
			FoodStats hunger = player.getFoodStats();

			player.heal(20F);
			player.curePotionEffects(new ItemStack(Items.milk_bucket));

			for (int id : ConfigHandler.getPotionIDs())
			{
				if (player.isPotionActive(id))
					player.removePotionEffect(id);
			}

			if (hunger.getFoodLevel() > 0)
				hunger.addStats(-10, 0);

			if (tickEvent.side == Side.CLIENT)
			{
				Minecraft.getMinecraft().getNetHandler().handleUpdateHealth(new S06PacketUpdateHealth(player.getHealth(), hunger.getFoodLevel(), hunger.getSaturationLevel()));
			}

		}

	}

	@SubscribeEvent
	public void itemRightClick(PlayerInteractEvent useEvent)
	{
		EntityPlayer player = useEvent.entityPlayer;

		if (player.getCurrentEquippedItem() != null && useEvent.action != Action.LEFT_CLICK_BLOCK)
		{
			ItemStack heldStack = player.getCurrentEquippedItem();
			Item heldItem = heldStack.getItem();
			World world = useEvent.world;

			if (heldItem instanceof ItemArmor)
				switchArmor(player, world, heldStack);

			if (heldItem == Items.arrow)
				throwArrow(world, player, heldStack);

			if ((heldItem instanceof ItemTool || heldItem.isDamageable()) && useEvent.action == Action.RIGHT_CLICK_BLOCK)
				placeBlocks(world, player, heldStack, useEvent.x, useEvent.y, useEvent.z, useEvent.face);
		}

	}

	private void placeBlocks(World world, EntityPlayer player, ItemStack heldItem, int x, int y, int z, int face)
	{
		InventoryPlayer inventory = player.inventory;
		int heldItemIndex = inventory.currentItem;
		ItemStack toPlace = player.inventory.getStackInSlot((heldItemIndex + 1) % 9);

		if (toPlace == null || !(toPlace.getItem() instanceof ItemBlock))
		{
			if (heldItemIndex - 1 >= 0)
				toPlace = player.inventory.getStackInSlot((heldItemIndex - 1) % 9);
			else
				toPlace = player.inventory.getStackInSlot(8); //Stops a ArrayOutOfBoundsException... % don't like negatives for some reasons...
		}

		if (toPlace != null && toPlace.getItem() instanceof ItemBlock)
		{
			ItemBlock block = (ItemBlock) toPlace.getItem();
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
						block.placeBlockAt(toPlace, player, world, x, y, z, face, (float) x, (float) y, (float) z, toPlace.getItemDamage());
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
					block.placeBlockAt(toPlace, player, world, x, y, z, face, (float) x, (float) y, (float) z, toPlace.getItemDamage());
				}
				if (!isPlayerCreative)
				{
					heldItem.damageItem(1, player);
					inventory.consumeInventoryItem(toPlace.getItem());
				}
			}

		}

	}

	private boolean canItemHarvest(ItemStack harvestItem, Block blockToBreak)
	{
		for (String classes : harvestItem.getItem().getToolClasses(harvestItem))
		{
			if (blockToBreak.isToolEffective(classes, harvestItem.getItemDamage()))
				return true;
		}
		return false;
	}

	private void throwArrow(World world, EntityPlayer player, ItemStack heldItem)
	{
		if (!player.capabilities.isCreativeMode)
			heldItem.stackSize--;

		double damage = calculateDamage(4.0, player);

		EntityArrow arrow = new EntityArrow(world, player, (float) calculateDamage(0.5, player));
		arrow.setDamage(damage);

		player.swingItem();

		SurvivalTweaks.playSound("random.bow", world, player);

		if (!world.isRemote)
			world.spawnEntityInWorld(arrow);
	}

	private void switchArmor(EntityPlayer player, World world, ItemStack heldItem)
	{
		InventoryPlayer inventory = player.inventory;
		int heldItemIndex = player.inventory.currentItem;
		int armorIndex = EntityLiving.getArmorPosition(heldItem) - 1;

		if (player.getCurrentArmor(armorIndex) == null)
			return;

		if (heldItem.getItem().getUnlocalizedName().equals("item.openblocks.sleepingbag")) //Bandage fix for now
			return;

		ItemStack equipedArmor = player.getCurrentArmor(armorIndex);

		player.setCurrentItemOrArmor(armorIndex + 1, heldItem);

		if (!player.capabilities.isCreativeMode)
			inventory.setInventorySlotContents(heldItemIndex, equipedArmor);

		SurvivalTweaks.playSound("mob.irongolem.throw", world, player);

		if (player.worldObj.isRemote)
			Minecraft.getMinecraft().getNetHandler().handleConfirmTransaction(new S32PacketConfirmTransaction());
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
