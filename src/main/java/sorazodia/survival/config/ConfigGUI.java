package sorazodia.survival.config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import sorazodia.survival.main.SurvivalTweaks;

public class ConfigGUI extends GuiConfig
{
	private static final String TITLE = "SurvivalTweaks Config";
	
	public ConfigGUI(GuiScreen parent)
	{
		super(parent, new ConfigElement(ConfigHandler.configFile.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(), SurvivalTweaks.MODID, false, false, TITLE);
	}

}
