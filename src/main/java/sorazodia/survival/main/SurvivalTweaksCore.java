package sorazodia.survival.main;

import java.util.Arrays;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import com.google.common.eventbus.EventBus;

public class SurvivalTweaksCore extends DummyModContainer
{
	public SurvivalTweaksCore()
    {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = "survivalTweaksCore";
        meta.name = "SurvivalTweaksCore";
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
    
}
