package sorazodia.survival.mechanics;

import static net.minecraftforge.fml.common.eventhandler.Event.Result.*;

import java.lang.reflect.Method;
import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

import org.apache.logging.log4j.Level;

import sorazodia.survival.config.ConfigHandler;
import sorazodia.survival.main.SurvivalTweaks;
import sorazodia.survival.mechanics.trackers.BlackListTracker;
import sorazodia.survival.mechanics.trackers.WhiteListTracker;

public class PlayerActionEvent
{
	private WhiteListTracker whitelist = (WhiteListTracker) SurvivalTweaks.getWhiteListTracker();
	private BlackListTracker blacklist = (BlackListTracker) SurvivalTweaks.getBlackListTracker();

	private static HashMap<String, Boolean> activationMap = new HashMap<>();
	
	@SubscribeEvent
	public void itemUseTick(LivingEntityUseItemEvent.Tick event)
	{
		if (event.getEntityLiving() instanceof EntityPlayer)
			softenFall((EntityPlayer) event.getEntityLiving(), event.getItem());
	}

	@SubscribeEvent
	public void playerTick(PlayerTickEvent event)
	{
		EntityPlayer player = event.player;

		if (player.getActiveHand() != null && player.getHeldItem(player.getActiveHand()) != null)
		{
			ItemStack stack = player.getHeldItem(player.getActiveHand());

			if (stack.getItemUseAction() != EnumAction.BLOCK && Math.abs(player.rotationPitch) == 90 && player.motionY <= -0.45)
			{
				if (player.motionY == -1000)
					SurvivalTweaks.getLogger().printf(Level.INFO, "[%s] AHHHHHHHHHHHHHHHHH AHHHHHH AHHHHHHHHHHHHHHHHH I BELIEVE I CAN FLYYYYYYY", player.getDisplayName());

				player.swingProgress = 0.85F;
				softenFall(player, stack);
			}
		}

	}

	public void softenFall(EntityPlayer player, ItemStack stack)
	{
		if (stack.getItem() instanceof ItemShield || SurvivalTweaks.getParachuteTracker().isValid(stack.getItem()))
		{
			if (Math.abs(player.rotationPitch) == 90 && player.motionY <= -0.45)
			{
				player.motionY /= 1.5;
				player.fallDistance /= 1.5;
				stack.damageItem(1, player);
			}
		}
	}

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

		if (ConfigHandler.doArrowThrow() && heldStack.getItem() instanceof ItemArrow)
			throwArrow(world, player, heldStack, event.getHand());
	}

	@SubscribeEvent
	public void blockRightClick(RightClickBlock event)
	{
		Item heldItem = event.getItemStack().getItem();
		World world = event.getWorld();

		if (whitelist.isValid(heldItem) || (!blacklist.isValid(heldItem) && (heldItem instanceof ItemTool)))
		{
			if (!world.isRemote && ConfigHandler.doToolBlockPlace() && !event.getItemStack().isEmpty() && event.getFace() != null && event.getUseItem() != DENY)
			{
				EntityPlayer player = event.getEntityPlayer();
				ItemStack heldStack = event.getItemStack();
				EnumFacing offset = event.getFace();
				IBlockState blockState = world.getBlockState(event.getPos());
				EnumHand hand = event.getHand();

				if (!player.isSneaking() && !activationMap.containsKey(blockState.getBlock().getUnlocalizedName()))
				{
					Class<? extends Block> blockClass = blockState.getBlock().getClass();
					Method methods[] = blockClass.getDeclaredMethods();
					
					activationMap.put(blockState.getBlock().getUnlocalizedName(), true);
					
					for (int x = 0; x < methods.length; x++)
					{
						if (methods[x].getName().contains("onBlockActivated") || methods[x].getName().equals("a"))
						{
							activationMap.replace(blockState.getBlock().getUnlocalizedName(), false);
							break;
						}
					}
				}
				
				if(player.isSneaking() || activationMap.get(blockState.getBlock().getUnlocalizedName()).booleanValue() == true)
					this.placeBlocks(world, player, blockState, blockState.getBlock(), heldStack, event.getPos(), offset, hand);
				
			}
		}

	}

	public void placeBlocks(World world, EntityPlayer player, IBlockState blockState, Block targetBlock, ItemStack heldStack, BlockPos pos, EnumFacing offset, EnumHand activeHand)
	{
		InventoryPlayer inventory = player.inventory;
		int heldItemIndex = inventory.currentItem;
		int hotbarLength = InventoryPlayer.getHotbarSize();
		ItemStack toPlace = inventory.getStackInSlot((heldItemIndex + 1) % hotbarLength);
		Item heldItem = heldStack.getItem();

		if (player.getHeldItemOffhand().getItem() instanceof ItemBlock)
			return;

		if (toPlace == null || !(toPlace.getItem() instanceof ItemBlock))
		{
			if (heldItemIndex - 1 >= 0)
			{
				toPlace = player.inventory.getStackInSlot(heldItemIndex - 1 % hotbarLength);
			}
			else
			{
				toPlace = player.inventory.getStackInSlot(hotbarLength - 1); //Stops a ArrayOutOfBoundsException... % don't like negative
			}
		}

		if (toPlace != null && toPlace.getItem() instanceof ItemBlock)
		{
			boolean isPlayerCreative = player.capabilities.isCreativeMode;
			boolean canHarvest = heldItem.canHarvestBlock(targetBlock.getBlockState().getBaseState(), heldStack) || canItemHarvest(heldStack, targetBlock, blockState) || (toPlace.getHasSubtypes() && targetBlock.getHarvestTool(blockState) == null);

			@SuppressWarnings("deprecation")
			IBlockState heldBlock = Block.getBlockFromItem(toPlace.getItem()).getStateFromMeta(toPlace.getMetadata());

			SoundEvent sound = heldBlock.getBlock().getSoundType(heldBlock, world, pos, player).getPlaceSound();

			if (player.isSneaking() && canHarvest)
			{
				if (targetBlock == Blocks.BEDROCK)
					return;

				SurvivalTweaks.playSound(sound, SoundCategory.BLOCKS, world, player, true);

				if (!world.isRemote)
				{
					targetBlock.harvestBlock(world, player, pos, blockState, blockState.getBlock().createTileEntity(world, blockState), heldStack);
					world.setBlockState(pos, heldBlock);
				}
				if (!isPlayerCreative)
				{
					heldStack.damageItem(1, player);
					toPlace.setCount(toPlace.getCount() - 1);
				}
			}
			else
			{
				pos = pos.add(offset.getFrontOffsetX(), offset.getFrontOffsetY(), offset.getFrontOffsetZ());

				if (world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)).size() == 0 && heldBlock.getBlock().canPlaceBlockAt(world, pos))
				{
					SurvivalTweaks.playSound(sound, SoundCategory.BLOCKS, world, player, true);

					if (!world.isRemote)
					{
						world.setBlockState(pos, heldBlock);
					}
					if (!isPlayerCreative)
						toPlace.setCount(toPlace.getCount() - 1);
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

	private void throwArrow(World world, EntityPlayer player, ItemStack heldItem, EnumHand hand)
	{
		float damage = (float) calculateDamage(4.0, player);

		SurvivalTweaks.playSound(SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, world, player, false);
		player.swingArm(hand);

		if (!world.isRemote)
		{
			ItemArrow itemArrow = (ItemArrow) heldItem.getItem();
			EntityArrow arrow = itemArrow.createArrow(world, heldItem, player);

			arrow.setAim(player, player.rotationPitch, player.rotationYaw, 0, damage / 5, 1);
			arrow.setDamage(damage);

			if (player.isPotionActive(MobEffects.STRENGTH))
				arrow.setIsCritical(true);

			if (!player.capabilities.isCreativeMode)
				heldItem.setCount(heldItem.getCount() - 1);
			else
				arrow.pickupStatus = EntityArrow.PickupStatus.DISALLOWED;

			world.spawnEntity(arrow);

		}
	}

	private void switchArmor(EntityPlayer player, World world, ItemStack heldItem)
	{
		InventoryPlayer inventory = player.inventory;
		int heldItemIndex = player.inventory.currentItem;
		int armorIndex = ((ItemArmor) heldItem.getItem()).armorType.getIndex();

		if (heldItem.getItem().getUnlocalizedName().equals("item.openblocks.sleepingbag")) //Bandage fix for now
			return;

		ItemStack equipedArmor = inventory.armorInventory.get(armorIndex);

		inventory.armorInventory.set(armorIndex, heldItem);

		if (!player.capabilities.isCreativeMode)
			inventory.mainInventory.set(heldItemIndex, equipedArmor);

		SurvivalTweaks.playSound(SoundEvents.ENTITY_IRONGOLEM_ATTACK, SoundCategory.PLAYERS, world, player, false);
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
