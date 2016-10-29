package sorazodia.survival.mechanics;

import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sorazodia.survival.config.ConfigHandler;
import sorazodia.survival.main.SurvivalTweaks;

public class EnderEvent
{
	@SubscribeEvent
	public void teleInEnd(EnderTeleportEvent enderEvent)
	{
		if (ConfigHandler.doPearlEndDamage() && enderEvent.getEntityLiving() instanceof EntityPlayer && enderEvent.getEntityLiving().dimension == 1)
		{
			enderEvent.setAttackDamage(0);
		}
	}
	
	@SubscribeEvent
	public void itemRightClick(PlayerInteractEvent.RightClickItem useEvent)
	{
		EntityPlayer player = useEvent.getEntityPlayer();

		if (useEvent.getItemStack() != null)
		{
			ItemStack heldStack = useEvent.getItemStack();
			Item heldItem = heldStack.getItem();
			World world = useEvent.getWorld();

			if (ConfigHandler.allowPearlCreative() && heldItem == Items.ENDER_PEARL)
				throwPearl(world, player, heldStack);

			if (ConfigHandler.doEnderTeleport() && heldItem == Items.ENDER_EYE)
				teleportToStronghold(useEvent.getWorld(), player);
		}

	}
	
	private void teleportToStronghold(World world, EntityPlayer player)
	{
		if (!player.capabilities.isCreativeMode || !player.isSneaking())
			return;

		BlockPos nearestStrongHold = ((WorldServer)world).getChunkProvider().getStrongholdGen(world, "Stronghold", player.getPosition());	

		if (!world.isRemote && nearestStrongHold != null)
		{
			int x = nearestStrongHold.getX();
			int y = nearestStrongHold.getY();
			int z = nearestStrongHold.getZ();
			
			nearestStrongHold = ((WorldServer)world).getChunkProvider().getStrongholdGen(world, "Stronghold", player.getPosition());
			
			player.setPositionAndUpdate(x, y, z);
		}
	}
	
	private void throwPearl(World world, EntityPlayer player, ItemStack heldItem)
	{
		if (!player.capabilities.isCreativeMode)
			return;

		SurvivalTweaks.playSound(SoundEvents.ENTITY_ENDERPEARL_THROW, player.worldObj, player);

		if (!world.isRemote)
			world.spawnEntityInWorld(new EntityEnderPearl(world, player));
	}

}
