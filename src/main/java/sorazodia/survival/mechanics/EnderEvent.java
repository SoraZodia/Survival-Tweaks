package sorazodia.survival.mechanics;

import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sorazodia.survival.config.ConfigHandler;
import sorazodia.survival.main.SurvivalTweaks;

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
				teleportToStronghold(useEvent.world, player);
		}

	}
	
	private void teleportToStronghold(World world, EntityPlayer player)
	{
		if (!player.capabilities.isCreativeMode || !player.isSneaking())
			return;

		BlockPos nearestStrongHold = world.getStrongholdPos("Stronghold", player.getPosition());	

		if (!world.isRemote && nearestStrongHold != null)
		{
			int x = nearestStrongHold.getX();
			int y = nearestStrongHold.getY();
			int z = nearestStrongHold.getZ();
			
			nearestStrongHold = world.getStrongholdPos("Stronghold", player.getPosition());
			
			player.setPositionAndUpdate(x, y, z);
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
