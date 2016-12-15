package sorazodia.survival.mechanics;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sorazodia.survival.config.ConfigHandler;

public class EnderEvent
{
	@SubscribeEvent
	public void teleInEnd(EnderTeleportEvent event)
	{
		if (ConfigHandler.doPearlEndDamage() && event.getEntityLiving().dimension == 1)
			event.setAttackDamage(0);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void itemRightClick(RightClickItem event)
	{
		EntityPlayer player = event.getEntityPlayer();

		if (event.getItemStack() != null)
		{
			ItemStack heldStack = event.getItemStack();
			Item heldItem = heldStack.getItem();

			if (ConfigHandler.doEnderTeleport() && heldItem == Items.ENDER_EYE)
				teleportToStronghold(event.getWorld(), player);
			
			if (((ConfigHandler.allowPearlCreative() && player.capabilities.isCreativeMode) || (ConfigHandler.doInstantRecharge() && player.dimension == 1)) && !player.worldObj.isRemote && heldItem == Items.ENDER_PEARL)
			{
				event.setCanceled(true);
				heldItem.onItemRightClick(heldStack, event.getWorld(),player, event.getHand());
				player.getCooldownTracker().removeCooldown(heldItem);
				event.setResult(Result.ALLOW);
			}

		}

	}

	private void teleportToStronghold(World world, EntityPlayer player)
	{
		if (!player.capabilities.isCreativeMode || !player.isSneaking() || world.isRemote)
			return;

		BlockPos nearestStrongHold = ((WorldServer) world).getChunkProvider().getStrongholdGen(world, "Stronghold", player.getPosition());

		if (nearestStrongHold != null)
		{
			int x = nearestStrongHold.getX();
			int y = nearestStrongHold.getY();
			int z = nearestStrongHold.getZ();

			nearestStrongHold = ((WorldServer) world).getChunkProvider().getStrongholdGen(world, "Stronghold", player.getPosition());

			player.setPositionAndUpdate(x, y, z);
		}
	}

}
