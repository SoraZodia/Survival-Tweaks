package sorazodia.survival.mechanics;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class PlayerActionEvent
{

	@SubscribeEvent
	public void onEntityAttack(LivingHurtEvent hurtEvent)
	{
		if (hurtEvent.entity instanceof EntityPlayer && hurtEvent.source instanceof EntityDamageSource)
		{
			EntityPlayer player = (EntityPlayer) hurtEvent.entity;

			if (player.getItemInUse() != null && player.getItemInUse().getItem() instanceof ItemSword)
			{
				player.getItemInUse().damageItem((int) hurtEvent.ammount, player);
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
		int temp = arrowEvent.charge;
		
		if (arrowEvent.entityLiving.getActivePotionEffect(Potion.damageBoost) != null)
		{
			PotionEffect strength = arrowEvent.entityLiving.getActivePotionEffect(Potion.damageBoost);			
			
			arrowEvent.charge *= (1.30 * (strength.getAmplifier() + 1));
		}
		if (arrowEvent.entityLiving.getActivePotionEffect(Potion.weakness) != null)
		{
			PotionEffect weakness = arrowEvent.entityLiving.getActivePotionEffect(Potion.weakness);
			double reduction = arrowEvent.charge * (0.5 * (weakness.getAmplifier() + 1));
			
			arrowEvent.charge -= reduction;
		}
		else
		{
			arrowEvent.charge = temp;
		}
			
	}

	@SubscribeEvent
	public void onSleep(PlayerTickEvent tickEvent)
	{
		EntityPlayer player = tickEvent.player;
		FoodStats hunger = player.getFoodStats();
		if (player.isPlayerFullyAsleep())
		{
			player.heal(20F);
			hunger.setFoodLevel(hunger.getFoodLevel() - 5);
			player.curePotionEffects(new ItemStack(Items.milk_bucket));
		}

	}

	@SubscribeEvent
	public void itemRightClick(PlayerInteractEvent useEvent)
	{
		EntityPlayer player = useEvent.entityPlayer;

		if (player.getCurrentEquippedItem() != null)
		{
			ItemStack heldStack = player.getCurrentEquippedItem();
			Item heldItem = heldStack.getItem();

			if (heldItem instanceof ItemArmor)
				switchArmor(player, heldStack);

			if (heldItem == Items.arrow)
				throwArrow(useEvent.world, player, heldStack);

			if (heldItem instanceof ItemTool && useEvent.action == Action.RIGHT_CLICK_BLOCK)
				placeBlocks(useEvent.world, player, heldStack, useEvent.x, useEvent.y, useEvent.z, useEvent.face);
		}

	}

	private void placeBlocks(World world, EntityPlayer player, ItemStack heldItem, int x, int y, int z, int face)
	{
		InventoryPlayer inventory = player.inventory;
		int heldItemIndex = inventory.currentItem;
		ItemStack toPlace = player.inventory.getStackInSlot((heldItemIndex + 1) % 9);
		boolean blockPlaced = false;

		if (toPlace == null || !(toPlace.getItem() instanceof ItemBlock))
			toPlace = player.inventory.getStackInSlot((heldItemIndex - 1) % 9);

		if (toPlace != null && toPlace.getItem() instanceof ItemBlock)
		{
			ItemBlock block = (ItemBlock) toPlace.getItem();
			ForgeDirection offset = ForgeDirection.getOrientation(face);
			Block targetBlock = world.getBlock(x, y, z);

			if (!world.isRemote)
			{
				if (!player.isSneaking())
				{
					x += offset.offsetX;
					y += offset.offsetY;
					z += offset.offsetZ;
					
					if (canPlace(x, y, z, (int)player.posX, (int)player.posY, (int)player.posZ))
					{
						block.placeBlockAt(toPlace, player, world, x, y, z, face, (float) x, (float) y, (float) z, toPlace.getItemDamage());
						blockPlaced = true;
					}

				} else if (heldItem.getItem().canHarvestBlock(targetBlock, heldItem))
				{
					targetBlock.harvestBlock(world, player, x, y, z, world.getBlockMetadata(x, y, z));
					block.placeBlockAt(toPlace, player, world, x, y, z, face, (float) x, (float) y, (float) z, toPlace.getItemDamage());
					blockPlaced = true;
					
					if (!player.capabilities.isCreativeMode)
						heldItem.damageItem(1, player);
				}
			}

			if (!player.capabilities.isCreativeMode && blockPlaced)
				inventory.consumeInventoryItem(toPlace.getItem());
		}

	}

	private void throwArrow(World world, EntityPlayer player, ItemStack heldItem)
	{
		if (!player.capabilities.isCreativeMode)
			heldItem.stackSize--;

		EntityArrow arrow = new EntityArrow(world, player, 0.5F);
		arrow.setDamage(4);

		player.swingItem();

		if (!world.isRemote)
			world.spawnEntityInWorld(arrow);
	}

	private void switchArmor(EntityPlayer player, ItemStack heldItem)
	{
		InventoryPlayer inventory = player.inventory;
		int heldItemIndex = player.inventory.currentItem;
		int armorIndex = EntityLiving.getArmorPosition(heldItem) - 1;

		if (player.getCurrentArmor(armorIndex) == null)
			return;

		ItemStack equipedArmor = player.getCurrentArmor(armorIndex);

		player.setCurrentItemOrArmor(armorIndex + 1, heldItem);

		if (!player.capabilities.isCreativeMode)
			inventory.setInventorySlotContents(heldItemIndex, equipedArmor);

		player.playSound("mob.irongolem.throw", 1.0F, 1.0F);
		
		Minecraft.getMinecraft().getNetHandler().handleConfirmTransaction(new S32PacketConfirmTransaction());
	}
	
	private boolean canPlace(int targetX, int targetY, int targetZ, int currentX, int currentY, int currentZ)
	{
		boolean noCollision = false;
		
		if ((targetX != currentX || targetZ != currentZ) && targetY != currentY) //checks if the player is not in the general x, y, z area
			noCollision = true;
		
		if ((targetX != currentX || targetZ != currentZ) && targetY == currentY) //checks if the player is not in the x or z row/column
			noCollision = true;
		
		//if ((targetX == currentX || targetZ == currentZ) && (targetY != currentY && targetY != currentY + 1)) //checks if the player's body won't be inside the block
		if ((targetX == currentX || targetZ == currentZ) && targetY != currentY + 1) //checks if the player's head won't be inside the block
			noCollision = true;
		
		return noCollision;
	}

}
