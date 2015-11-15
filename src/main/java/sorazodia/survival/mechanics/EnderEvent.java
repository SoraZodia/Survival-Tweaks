package sorazodia.survival.mechanics;

import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import sorazodia.survival.config.ConfigHandler;
import sorazodia.survival.main.SurvivalTweaks;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EnderEvent
{
	@SubscribeEvent
	public void teleInEnd(EnderTeleportEvent enderEvent)
	{
		if (ConfigHandler.doPearlEndDamage() && enderEvent.entityLiving instanceof EntityPlayer && enderEvent.entityLiving.dimension == 1)
		{
			enderEvent.attackDamage = 0;
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

			if (ConfigHandler.allowPearlCreative() && heldItem == Items.ender_pearl)
				throwPearl(world, player, heldStack);

			if (ConfigHandler.doEnderTeleport() && heldItem == Items.ender_eye)
				teleportToStronghold(useEvent.world, player);;
		}

	}
	
	private void teleportToStronghold(World world, EntityPlayer player)
	{
		if (!player.capabilities.isCreativeMode || !player.isSneaking())
			return;

		ChunkPosition nearestStrongHold = world.findClosestStructure("Stronghold", (int) player.posX, (int) player.posY, (int) player.posZ);

		if (!world.isRemote && nearestStrongHold != null)
		{
			//rerun so player won't be inside a wall
			nearestStrongHold = world.findClosestStructure("Stronghold", nearestStrongHold.chunkPosX, nearestStrongHold.chunkPosY, nearestStrongHold.chunkPosZ);
			
			world.getChunkProvider().loadChunk(nearestStrongHold.chunkPosX, nearestStrongHold.chunkPosZ);
			player.setPositionAndUpdate(nearestStrongHold.chunkPosX, nearestStrongHold.chunkPosY, nearestStrongHold.chunkPosZ);
		}
	}
	
	private void throwPearl(World world, EntityPlayer player, ItemStack heldItem)
	{
		if (!player.capabilities.isCreativeMode)
			return;

		SurvivalTweaks.playSound("random.bow", player.worldObj, player);

		if (!world.isRemote)
			world.spawnEntityInWorld(new EntityEnderPearl(world, player));
	}

}
