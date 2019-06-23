package sorazodia.survival.main;

import java.util.function.BooleanSupplier;

import com.google.gson.JsonObject;

import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import sorazodia.survival.config.ConfigHandler;

public class RecipeManager implements IConditionFactory
{
	private final String ID_KEY = "id";
	@Override
	public BooleanSupplier parse(JsonContext context, JsonObject json)
	{	
		if (json.has(ID_KEY) && json.get(ID_KEY).getAsString().toLowerCase().equals("chainmail")) {
			System.out.println(ConfigHandler.enableChainmail());
			return () -> ConfigHandler.enableChainmail();
		}
		
		return () -> true;
	}

}
