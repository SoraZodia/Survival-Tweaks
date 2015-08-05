package sorazodia.survival.mechanics;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderPearl;
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
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import sorazodia.survival.config.ConfigHandler;
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
		arrowEvent.charge = (int) calculateDamage(arrowEvent.charge, arrowEvent.entityLiving);
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
			
			for(int id : ConfigHandler.getPotionIDs())
			{
				if (player.isPotionActive(id))
					player.removePotionEffect(id);
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
				switchArmor(player, heldStack);

			if (heldItem == Items.arrow)
				throwArrow(world, player, heldStack);

			if (heldItem == Items.ender_pearl)
				throwPearl(world, player, heldStack);

			if (heldItem == Items.ender_eye)
				teleportToStronghold(useEvent.world, player);

			if (heldItem instanceof ItemTool && useEvent.action == Action.RIGHT_CLICK_BLOCK)
				placeBlocks(world, player, heldStack, useEvent.x, useEvent.y, useEvent.z, useEvent.face);
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

					if (canPlace(x, y, z, (int) player.posX, (int) player.posY, (int) player.posZ))
					{
						block.placeBlockAt(toPlace, player, world, x, y, z, face, (float) x, (float) y, (float) z, toPlace.getItemDamage());
						blockPlaced = true;
						world.playSoundAtEntity(player, "dig.stone", getVolume(), getVolume());
						
					}

				} else if (heldItem.getItem().canHarvestBlock(targetBlock, heldItem))
				{
					if (targetBlock == Blocks.bedrock)
						return;
					
					targetBlock.harvestBlock(world, player, x, y, z, world.getBlockMetadata(x, y, z));
					block.placeBlockAt(toPlace, player, world, x, y, z, face, (float) x, (float) y, (float) z, toPlace.getItemDamage());
					blockPlaced = true;
					world.playSoundAtEntity(player, "dig.stone", getVolume(), getVolume());
					
					if (!player.capabilities.isCreativeMode)
						heldItem.damageItem(1, player);
				}
			}

			
			if (!player.capabilities.isCreativeMode && blockPlaced)
				inventory.consumeInventoryItem(toPlace.getItem());
		}

	}

	private void teleportToStronghold(World world, EntityPlayer player)
	{
		if (!player.capabilities.isCreativeMode || !player.isSneaking())
			return;

		ChunkPosition nearestStrongHold = world.findClosestStructure("Stronghold", (int) player.posX, (int) player.posY, (int) player.posZ);

		if (!world.isRemote)
		{
			//rerun so player won't be inside a wall
			nearestStrongHold = world.findClosestStructure("Stronghold", nearestStrongHold.chunkPosX, nearestStrongHold.chunkPosY, nearestStrongHold.chunkPosZ); 
			player.setPositionAndUpdate(nearestStrongHold.chunkPosX, nearestStrongHold.chunkPosY, nearestStrongHold.chunkPosZ);
		}
	}

	private void throwPearl(World world, EntityPlayer player, ItemStack heldItem)
	{
		if (!player.capabilities.isCreativeMode)
			return;

		world.playSoundAtEntity(player, "random.bow", getVolume(), getVolume());

		if (!world.isRemote)
			world.spawnEntityInWorld(new EntityEnderPearl(world, player));
	}

	private void throwArrow(World world, EntityPlayer player, ItemStack heldItem)
	{
		if (!player.capabilities.isCreativeMode)
			heldItem.stackSize--;

		double damage = calculateDamage(4.0, player);
		//damage = (Math.pow(damage, 2) + damage * 2.0) / 20.0;
		
		EntityArrow arrow = new EntityArrow(world, player, (float) calculateDamage(0.5, player));
		arrow.setDamage(damage);

		player.swingItem();
		world.playSoundAtEntity(player, "random.bow", getVolume(), getVolume());
		
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

		player.playSound("mob.irongolem.throw", getVolume(), getVolume());

		Minecraft.getMinecraft().getNetHandler().handleConfirmTransaction(new S32PacketConfirmTransaction());
	}

	private boolean canPlace(int targetX, int targetY, int targetZ, int currentX, int currentY, int currentZ)
	{
		boolean noCollision = false;

		if ((targetX != currentX || targetZ != currentZ) && targetY != currentY) //checks if the player is not in the general x, y, z area
			noCollision = true;

		if ((targetX != currentX || targetZ != currentZ) && targetY == currentY) //checks if the player is not in the x or z row/column
			noCollision = true;

		if ((targetX == currentX || targetZ == currentZ) && (targetY != currentY && targetY != currentY + 1)) //checks if the player's body won't be inside the block
		//if ((targetX == currentX || targetZ == currentZ) && targetY != currentY + 1) //checks if the player's head won't be inside the block
			noCollision = true;

		return noCollision;
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
	
	private float getVolume()
	{
		return Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.PLAYERS);
	}

}
