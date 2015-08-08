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
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import sorazodia.survival.main.SurvivalTweaks;
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
		String message = StatCollector.translateToLocal("survivaltweaks.invalid.argument");
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

			if (!checkDimension(sender, args[0]))
				break;

			targetDimension = Integer.parseInt(args[0]);
			worldServer = mcServer.worldServerForDimension(targetDimension);
			player = CommandDimensionTeleport.getCommandSenderAsPlayer(sender);

			tranferToDimension(player, worldServer, player.dimension, targetDimension);
			break;
		case 2:
			player = CommandDimensionTeleport.getPlayer(sender, args[0]);

			if (checkPlayer(sender, player) == false)
				break;

			if (SurvivalTweaks.isInteger(args[1]))
			{
				if (!checkDimension(sender, args[1]))
					break;

				targetDimension = Integer.parseInt(args[1]);
				worldServer = mcServer.worldServerForDimension(targetDimension);

				tranferToDimension(player, worldServer, player.dimension, targetDimension);
			} else
			{
				EntityPlayerMP targetPlayer = CommandDimensionTeleport.getPlayer(sender, args[1]);

				if (targetPlayer == null)
				{
					sender.addChatMessage(new ChatComponentTranslation("survivaltweaks.invalid.player"));
					break;
				}

				targetDimension = targetPlayer.dimension;
				worldServer = mcServer.worldServerForDimension(targetDimension);

				tranferToDimension(player, targetPlayer, worldServer, player.dimension, targetDimension, targetPlayer.posX, targetPlayer.posY + 1, targetPlayer.posZ);
			}
			break;

		case 3:
			player = CommandDimensionTeleport.getCommandSenderAsPlayer(sender);

			if (checkPlayer(sender, player) == false || !checkDimension(sender, args[0]) || checkInts(args, 1, sender) == false)
				break;

			targetDimension = Integer.parseInt(args[0]);
			worldServer = mcServer.worldServerForDimension(targetDimension);

			tranferToDimension(player, worldServer, player.dimension, targetDimension, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			break;
		case 4:
			boolean solo = false;

			if (!SurvivalTweaks.isInteger(args[0]))
				player = CommandDimensionTeleport.getPlayer(sender, args[0]);
			else
			{
				player = getCommandSenderAsPlayer(sender);
				solo = true;
			}

			if (!solo && (checkPlayer(sender, player) == false || !checkDimension(sender, args[1]) || checkInts(args, 2, sender) == false))
				break;

			if (solo && (checkPlayer(sender, player) == false || !checkDimension(sender, args[0]) || checkInts(args, 1, sender) == false))
				break;

			if (!solo)
				targetDimension = Integer.parseInt(args[1]);
			else
				targetDimension = Integer.parseInt(args[0]);

			worldServer = mcServer.worldServerForDimension(targetDimension);

			if (!solo)
				tranferToDimension(player, worldServer, player.dimension, targetDimension, Integer.parseInt(args[2]), Integer.parseInt(args[3]));
			else
				tranferToDimension(player, null, worldServer, player.dimension, targetDimension, Double.parseDouble(args[1]), Double.valueOf(args[2]), Double.parseDouble(args[3]));

			break;
		case 5:
			player = CommandDimensionTeleport.getPlayer(sender, args[0]);

			if (checkPlayer(sender, player) == false || !checkDimension(sender, args[1]) || checkInts(args, 2, sender) == false)
				break;

			targetDimension = Integer.parseInt(args[1]);
			worldServer = mcServer.worldServerForDimension(targetDimension);

			tranferToDimension(player, null, worldServer, player.dimension, targetDimension, Double.parseDouble(args[2]), Double.valueOf(args[3]), Double.parseDouble(args[4]));

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
			ArrayList<String> argsList = new ArrayList<>();
			GameProfile[] profiles = MinecraftServer.getServer().func_152357_F();

			if (args.length == 1)
				argsList.add("list");

			for (int x = 0; x < profiles.length; x++)
			{
				if (manager.func_152596_g(profiles[x]) && doesStringStartWith(lastLetter, profiles[x].getName()))
				{
					argsList.add(profiles[x].getName());
				}
			}

			return argsList;
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

		for (int x = 0; x < ids.length; x++)
		{
			if (ids[x] != currectDimension)
				message = (" [%i: %s] ");
			else
				message = (" [%i: %s" + "(" + StatCollector.translateToLocal("survivaltweaks.command.tpd.list.here") + ")] ");

			name = DimensionManager.getProvider(ids[x]).getDimensionName();
			message = message.replace("%i", ids[x].toString());
			message = message.replace("%s", name);

			str.append(message);
		}

		return str.toString().trim();
	}

	private void printUsage(ICommandSender sender)
	{
		sender.addChatMessage(new ChatComponentTranslation("survivaltweaks.command.useage"));
		sender.addChatMessage(new ChatComponentTranslation("survivaltweaks.command.tpd.list"));
		sender.addChatMessage(new ChatComponentTranslation("survivaltweaks.command.tpd.id"));
		sender.addChatMessage(new ChatComponentTranslation("survivaltweaks.command.tpd.id.xz"));
		sender.addChatMessage(new ChatComponentTranslation("survivaltweaks.command.tpd.id.xyz"));
		sender.addChatMessage(new ChatComponentTranslation("survivaltweaks.command.tpd.p2id"));
		sender.addChatMessage(new ChatComponentTranslation("survivaltweaks.command.tpd.p2id.xz"));
		sender.addChatMessage(new ChatComponentTranslation("survivaltweaks.command.tpd.p2id.xyz"));
		sender.addChatMessage(new ChatComponentTranslation("survivaltweaks.command.tpd.p2p"));
	}

	private boolean checkInts(String[] args, int i, ICommandSender sender)
	{
		boolean isInt = true;

		for (; i < args.length; i++)
		{
			if (!SurvivalTweaks.isInteger(args[i]))
			{
				sender.addChatMessage(new ChatComponentTranslation("survivaltweaks.invalid.number", args[i]));
				isInt = false;
			}
		}

		return isInt;
	}

	private boolean checkPlayer(ICommandSender sender, EntityPlayerMP player)
	{
		if (player == null)
		{
			sender.addChatMessage(new ChatComponentTranslation("survivaltweaks.invalid.player"));
			return false;
		}

		return true;
	}

	private boolean checkDimension(ICommandSender sender, String arg)
	{
		if (!SurvivalTweaks.isInteger(arg) || !DimensionManager.isDimensionRegistered(Integer.parseInt(arg)))
		{
			sender.addChatMessage(new ChatComponentTranslation("survivaltweaks.invalid.dimension"));
			return false;
		}

		return true;
	}

	private void tranferToDimension(EntityPlayerMP player, EntityPlayerMP targetPlayer, WorldServer worldServer, int currentDimensionID, int targetDimensionID, double x, Double y, double z)
	{
		if (targetDimensionID == currentDimensionID)
		{
			player.addChatMessage(new ChatComponentTranslation("survivaltweaks.invalid.nowhoosh"));
			return;
		}

		if (player.equals(targetPlayer))
		{
			player.addChatMessage(new ChatComponentTranslation("survivaltweaks.command.tpd.sameplayer"));
			return;
		}

		InterDimTeleporter teleporter;

		if (y != null)
			teleporter = new InterDimTeleporter(worldServer, x, y, z);
		else
		{
			teleporter = new InterDimTeleporter(worldServer, x, z);
			y = teleporter.getY();
		}

		mcServer.getConfigurationManager().transferPlayerToDimension(player, targetDimensionID, teleporter);

		if (currentDimensionID == 1)
		{
			player.setPositionAndUpdate(x, y, z);
			worldServer.spawnEntityInWorld(player);
			worldServer.updateEntityWithOptionalForce(player, false);
		} 
		else if (targetPlayer != null)
		{
			player.setPositionAndUpdate(x, y, z);
			worldServer.updateEntityWithOptionalForce(player, false); // just to be safe
		}
	}

	private void tranferToDimension(EntityPlayerMP player, WorldServer worldServer, int currentDimensionID, int targetDimensionID)
	{
		ChunkCoordinates coord = worldServer.getSpawnPoint();
		tranferToDimension(player, null, worldServer, currentDimensionID, targetDimensionID, coord.posX, null, coord.posZ);
	}

	private void tranferToDimension(EntityPlayerMP player, WorldServer worldServer, int currentDimensionID, int targetDimensionID, int x, int z)
	{
		tranferToDimension(player, null, worldServer, currentDimensionID, targetDimensionID, x, null, z);
	}

}
