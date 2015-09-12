package sorazodia.survival.asm.main;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.EventBus;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;

public class SurvivalTweaksCore extends DummyModContainer
{
	private static Logger log = LogManager.getLogger();
	
	public SurvivalTweaksCore()
    {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = "survivalTweaksCore";
        meta.name = "SurvivalTweaks Core";
        meta.description = "ASM edits for Survival Tweaks";
        meta.version = "1.7.10-1.0.0";
        meta.credits = "VikeStep, for his ASM video";
        meta.authorList = Arrays.asList("sorazodia");       
    }
	
	@Override
    public boolean registerBus(EventBus bus, LoadController controller){
        bus.register(this);
        return true;
    }

	public static Logger getLogger()
	{
		return log;
	}
    
}
