package sorazodia.survival.mechanics;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.potion.Potion;
import net.minecraft.util.FoodStats;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import sorazodia.survival.config.ConfigHandler;
import sorazodia.survival.main.SurvivalTweaks;

public class PlayerSleepEvent
{

	public PlayerSleepEvent()
	{
		SurvivalTweaks.getLogger().info("MEOW MEOW MEOW MEOW");
	}

	@SubscribeEvent
	public void onSleep(PlayerTickEvent tickEvent)
	{

		boolean update = false;

		if (!ConfigHandler.doSleepHeal())
			return;

		EntityPlayer player = tickEvent.player;
		FoodStats hunger = player.getFoodStats();

		if (player.isPlayerFullyAsleep() && !player.worldObj.isRemote)
		{
			player.curePotionEffects(new ItemStack(Items.milk_bucket));

			for (String potion : ConfigHandler.getPotionIDs())
			{
				int id = -1;
				
				if (SurvivalTweaks.isInteger(potion))
					id = Integer.parseInt(potion);
				else
					id = Potion.getPotionFromResourceLocation(potion).id;
				
				if (player.isPotionActive(id))
					player.removePotionEffect(id);
			}

			if (player.getHealth() < player.getMaxHealth())
			{
				float pervHealth = player.getHealth();
				player.heal(20F);
				int hungerReduction = (int) (((player.getHealth() - pervHealth) / 18) * 10);

				if (hunger.getFoodLevel() - hungerReduction < 0)
					hunger.addStats(-hunger.getFoodLevel(), 0);
				else if (hunger.getFoodLevel() > 0)
					hunger.addStats(-hungerReduction, 0);

				update = true;
			}
		}

		if (player.worldObj.isRemote && update)
		{
			Minecraft.getMinecraft().getNetHandler().handleUpdateHealth(new S06PacketUpdateHealth(player.getHealth(), hunger.getFoodLevel(), hunger.getSaturationLevel()));
		}

	}

}
