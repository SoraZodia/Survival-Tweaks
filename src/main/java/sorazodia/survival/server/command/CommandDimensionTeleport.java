package sorazodia.survival.server.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import sorazodia.survival.teleport.InterDimTeleporter;

import com.mojang.authlib.GameProfile;

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
		String message = "Invalid Arguments";
		printUsage(sender);
		return message;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{

		EntityPlayerMP player;
		int targetDimension;
		WorldServer worldServer;

		switch (args.length)
		{
		case 1:
			if (args[0].equals("list"))
			{
				String print = listIDs(CommandDimensionTeleport.getCommandSenderAsPlayer(sender));
				sender.addChatMessage(new ChatComponentText(print));
				break;
			}
			
			if(!isInteger(args[0]) || !DimensionManager.isDimensionRegistered(Integer.parseInt(args[0])))
			{
				sender.addChatMessage(new ChatComponentTranslation("Invaild ID or Dimension don't exist"));
				break;
			}
			
			targetDimension = Integer.parseInt(args[0]);
			worldServer = mcServer.worldServerForDimension(targetDimension);
			player = CommandDimensionTeleport.getCommandSenderAsPlayer(sender);

			tranferToDimension(player, worldServer, player.dimension, targetDimension);
			break;
		case 2:
			player = CommandDimensionTeleport.getPlayer(sender, args[0]);

			if (player == null)
				sender.addChatMessage(new ChatComponentTranslation("Player Not Found"));

			if (isInteger(args[1]))
			{
				targetDimension = Integer.parseInt(args[1]);
				worldServer = mcServer.worldServerForDimension(targetDimension);

				tranferToDimension(player, worldServer, player.dimension, targetDimension);
			} else
			{
				EntityPlayerMP targetPlayer = CommandDimensionTeleport.getPlayer(sender, args[1]);

				if (targetPlayer == null)
				{
					sender.addChatMessage(new ChatComponentTranslation("Player Not Found"));
					break;
				}

				targetDimension = targetPlayer.dimension;
				worldServer = mcServer.worldServerForDimension(targetDimension);

				tranferToDimension(player, targetPlayer, worldServer, player.dimension, targetDimension, targetPlayer.posX, targetPlayer.posZ);
			}
			break;
		default:
			printUsage(sender);
			break;
		}

	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        ServerConfigurationManager manager = MinecraftServer.getServer().getConfigurationManager();
        
        if (args.length >= 1)
        {
        	String lastLetter = args[args.length - 1];
        	ArrayList<String> playerList = new ArrayList<>();
        	GameProfile[] profiles = MinecraftServer.getServer().func_152357_F();
        	
        	for (int x = 0; x < profiles.length; x++)
        	{
        		if (manager.func_152596_g(profiles[x]) && doesStringStartWith(lastLetter, profiles[x].getName()))
                {
        			playerList.add(profiles[x].getName());
                }
        	}
        	
        	return playerList;
        }
        
        return null;
    }

	private String listIDs(EntityPlayerMP player)
	{
	   StringBuilder str = new StringBuilder();
	   Integer[] ids = DimensionManager.getIDs();
	   int currectDimension = player.dimension;
	   String name;
	   String message;
	   
	   for(int x = 0; x < ids.length; x++)
	   {
		if (ids[x] != currectDimension)
	       message = (" [%i: %s] ");
		else
			message = (" [%i: %s (You Are Here)] ");
		
		name = DimensionManager.getProvider(ids[x]).getDimensionName();
		message = message.replace("%i", ids[x].toString());
		message = message.replace("%s", name);
		
		str.append(message);
	   }
	   
	   return str.toString().trim();
	}

	private void printUsage(ICommandSender sender)
	{
		sender.addChatMessage(new ChatComponentTranslation("Usage:"));
		sender.addChatMessage(new ChatComponentTranslation("/tpd list - List the dimension ids, name, and the one you are currectly in"));
		sender.addChatMessage(new ChatComponentTranslation("/tpd <Dimension Id> - Teleports you to that dimension"));
		sender.addChatMessage(new ChatComponentTranslation("/tpd <Player Name> <Dimension Id> - Teleport that player to that dimension"));
		sender.addChatMessage(new ChatComponentTranslation("/tpd <First Player Name> <Second Player Name> - Teleport the first player to the second player, regardless of thier current dimension"));
	}

	// Thank you StackOverflow
	private boolean isInteger(String arg)
	{
		if (arg == null)
			return false;

		int length = arg.length();

		if (length == 0)
			return false;

		int x = 0;

		if (arg.charAt(0) == '-')
		{
			if (length == 1)
				return false;
			x = 1;
		}

		for (; x < length; x++)
		{
			char c = arg.charAt(x);
			if (c <= '/' || c >= ':')
				return false;
		}

		return true;
	}

	private void tranferToDimension(EntityPlayerMP player, EntityPlayerMP targetPlayer, WorldServer worldServer, int currentDimensionID, int targetDimensionID, double x, double z)
	{
		if (targetDimensionID == currentDimensionID && player.equals(targetPlayer))
		{
			player.addChatMessage(new ChatComponentTranslation("You are already in that dimension... No whoosh for you"));
			return;
		}

		mcServer.getConfigurationManager().transferPlayerToDimension(player, targetDimensionID, new InterDimTeleporter(worldServer, x, z));

		if (currentDimensionID == 1)
		{
			// player coods are not updated..... Rerunning the method so that the player won't be buried alive... Or flying downward
			int y = InterDimTeleporter.getY((int) x, (int) z, 0, worldServer.getActualHeight(), worldServer);

			player.setPositionAndUpdate(x, y, z);
			worldServer.spawnEntityInWorld(player);
			worldServer.updateEntityWithOptionalForce(player, false);
		} else if (targetPlayer != null)
		{
			player.setPositionAndUpdate(x, player.posY, z);
			worldServer.updateEntityWithOptionalForce(player, false); // just to be safe
		}
	}

	private void tranferToDimension(EntityPlayerMP player, WorldServer worldServer, int currentDimensionID, int targetDimensionID)
	{
		tranferToDimension(player, null, worldServer, currentDimensionID, targetDimensionID, player.posX, player.posZ);
	}

}