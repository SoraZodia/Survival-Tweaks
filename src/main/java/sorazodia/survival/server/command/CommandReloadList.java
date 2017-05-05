package sorazodia.survival.server.command;

import java.util.ArrayList;
import java.util.List;

import sorazodia.survival.main.SurvivalTweaks;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;

public class CommandReloadList implements ICommand
{
	
	private final String name = "reload";
	
	@Override
	public int compareTo(ICommand command)
	{
		return SurvivalTweaks.MODID.compareTo(command.getName());
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender player)
	{
		return player.canUseCommand(2, SurvivalTweaks.MODID);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender player, String[] args) throws CommandException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getAliases()
	{
		ArrayList<String> aliases = new ArrayList<>();
		aliases.add(name);
		
		return aliases;
	}

	@Override
	public String getName()
	{
		return SurvivalTweaks.MODID;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender player, String[] args, BlockPos pos)
	{
		ArrayList<String> options = new ArrayList<>();
		
		if (args.length == 0)
			options.add(this.getName());
		if (args.length > 0)
			options.add(name);
		
		return options;
	}

	@Override
	public String getUsage(ICommandSender player)
	{
		return I18n.translateToLocalFormatted("survivaltweaks.command.reload");
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index)
	{
		return false;
	}
	
}
