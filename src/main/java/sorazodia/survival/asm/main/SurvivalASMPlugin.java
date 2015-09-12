package sorazodia.survival.asm.main;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions({"sorazodia.survival"})
public class SurvivalASMPlugin implements IFMLLoadingPlugin
{

	@Override
	public String[] getASMTransformerClass()
	{
		return new String[]{"sorazodia.survival.asm.main.SurvivalTweaksTranformer"};
	}

	@Override
	public String getModContainerClass()
	{
		return "sorazodia.survival.asm.main.SurvivalTweaksCore";
	}

	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data)
	{
		
	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}

}
