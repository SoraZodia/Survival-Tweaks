package sorazodia.survival.mechanics;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.world.World;
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

			player.curePotionEffects(new ItemStack(Items.milk_bucket));

			for (int id : ConfigHandler.getPotionIDs())
			{
				if (player.isPotionActive(id))
					player.removePotionEffect(id);
			}

			if (player.getHealth() != player.getMaxHealth())
			{
				player.heal(20F);

				if (hunger.getFoodLevel() > 0)
					hunger.addStats(-10, 0);
			}

			if (tickEvent.side == Side.CLIENT)
			{
				Minecraft.getMinecraft().getNetHandler().handleUpdateHealth(new S06PacketUpdateHealth(player.getHealth(), hunger.getFoodLevel(), hunger.getSaturationLevel()));
			}

		}

	}

	@SubscribeEvent
	public void itemRightClick(PlayerInteractEvent interactEvent)
	{
		EntityPlayer player = interactEvent.entityPlayer;

		if (player.getCurrentEquippedItem() != null && interactEvent.action != Action.LEFT_CLICK_BLOCK)
		{
			ItemStack heldStack = player.getCurrentEquippedItem();
			Item heldItem = heldStack.getItem();
			World world = interactEvent.world;

			if (heldItem instanceof ItemArmor)
				switchArmor(player, world, heldStack);

			if (heldItem == Items.arrow)
				throwArrow(world, player, heldStack);

//			if ((heldItem instanceof ItemTool || heldItem.isDamageable()) && interactEvent.action == Action.RIGHT_CLICK_BLOCK)
//			{
//				int x = interactEvent.x;
//				int y = interactEvent.y;
//				int z = interactEvent.z;
//
//				placeBlocks(world, player, heldStack, x, y, z, interactEvent.face);
//			}
		}

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
