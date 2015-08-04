package sorazodia.survival.server.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandDimensionTeleport extends CommandBase
{
	private MinecraftServer mcServer = MinecraftServer.getServer();

	@Override
	public String getCommandName()
	{
		return "tpd";
	}
	
    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "Correct Usage: /tpd <player> <Dimension Id>";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		EntityPlayerMP player = CommandDimensionTeleport.getCommandSenderAsPlayer(sender);
		mcServer.getConfigurationManager().transferPlayerToDimension(player, Integer.parseInt(args[0]));
	}

}
