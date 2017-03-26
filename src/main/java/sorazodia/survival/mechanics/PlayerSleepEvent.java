package sorazodia.survival.mechanics;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
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
		if (!ConfigHandler.doSleepHeal())
			return;

		EntityPlayer player = tickEvent.player;
		FoodStats hunger = player.getFoodStats();

		if (player.isPlayerFullyAsleep() && !player.world.isRemote)
		{
			player.curePotionEffects(new ItemStack(Items.MILK_BUCKET));

			for (String potion : ConfigHandler.getPotionIDs())
			{
				Potion effect = null;
				
				if (SurvivalTweaks.isInteger(potion))
					effect = Potion.getPotionById(Integer.parseInt(potion));
				else
					effect = Potion.getPotionFromResourceLocation(potion);
				
				if (player.isPotionActive(effect))
					player.removeActivePotionEffect(effect);
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
			}
		}

	}

}
