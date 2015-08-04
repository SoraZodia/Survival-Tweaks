package sorazodia.survival.server.command;

import sorazodia.survival.teleport.InterDimTeleporter;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldServer;

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
		String message = "Correct Usage: /tpd <player> <Dimension Id>";
		sender.addChatMessage(new ChatComponentTranslation(message).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
		return message;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		EntityPlayerMP player = CommandDimensionTeleport.getCommandSenderAsPlayer(sender);
		int currentDimension = player.dimension;
		int targetDimension = Integer.parseInt(args[0]);
		WorldServer worldServer = mcServer.worldServerForDimension(targetDimension);
		
		tranferToDimension(player, worldServer, currentDimension, targetDimension);
	}
	
	private void tranferToDimension(EntityPlayerMP player, WorldServer worldServer, int currentDimensionID, int targetDimensionID, double x, double z)
	{
		if (targetDimensionID == currentDimensionID)
		{
			player.addChatMessage(new ChatComponentTranslation("You are already in that dimension... No whoosh for you"));
			return;
		}		
		
		mcServer.getConfigurationManager().transferPlayerToDimension(player, targetDimensionID, new InterDimTeleporter(worldServer, x, z));
	
		if (currentDimensionID == 1)
		{
			//player coods are not updated..... Rerunning the method so that the player won't be buried alive... Or flying downward
			int y = InterDimTeleporter.getY((int)x, (int)z, 0, worldServer.getActualHeight(), worldServer);
			
			player.setPositionAndUpdate(x, y, z);
			worldServer.spawnEntityInWorld(player);
			worldServer.updateEntityWithOptionalForce(player, false);
		}
	}
	
	private void tranferToDimension(EntityPlayerMP player, WorldServer worldServer, int currentDimensionID, int targetDimensionID)
	{
		tranferToDimension(player, worldServer, currentDimensionID, targetDimensionID, player.posX, player.posZ);
	}

}
