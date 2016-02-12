package sorazodia.survival.server.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import sorazodia.survival.main.SurvivalTweaks;
import sorazodia.survival.teleport.InterDimTeleporter;

import com.mojang.authlib.GameProfile;

public class CommandDimensionTeleport implements ICommand
{
	private MinecraftServer mcServer = MinecraftServer.getServer();
	private static final String NAME = "tpd";

	@Override
	public String getCommandName()
	{
		return NAME;
	}

	public static String getName()
	{
		return NAME;
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		printUsage(sender);
		return StatCollector.translateToLocal("survivaltweaks.command.tpd");
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
				String print = listIDs(CommandDimensionTeleport.getPlayer(sender));
				sender.addChatMessage(new ChatComponentText(print));
				break;
			}

			if (!checkDimension(sender, args[0]))
				break;

			targetDimension = Integer.parseInt(args[0]);
			worldServer = mcServer.worldServerForDimension(targetDimension);
			player = CommandDimensionTeleport.getPlayer(sender);

			tranferToDimension(player, worldServer, player.dimension, targetDimension);
			break;
		case 2:
			player = CommandDimensionTeleport.getPlayer(args[0]);

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
				EntityPlayerMP targetPlayer = CommandDimensionTeleport.getPlayer(args[1]);

				if (targetPlayer == null)
				{
					sender.addChatMessage(new ChatComponentTranslation("survivaltweaks.invalid.player"));
					break;
				}

				targetDimension = targetPlayer.dimension;
				worldServer = mcServer.worldServerForDimension(targetDimension);

				tranferToDimension(player, targetPlayer, worldServer, player.dimension, targetDimension, targetPlayer.posX, targetPlayer.posY + 1,
						targetPlayer.posZ);
			}
			break;

		case 3:
			player = CommandDimensionTeleport.getPlayer(sender);

			if (checkPlayer(sender, player) == false || !checkDimension(sender, args[0]) || checkInts(args, 1, sender) == false)
				break;

			targetDimension = Integer.parseInt(args[0]);
			worldServer = mcServer.worldServerForDimension(targetDimension);

			tranferToDimension(player, worldServer, player.dimension, targetDimension, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			break;
		case 4:
			boolean solo = false;

			if (!SurvivalTweaks.isInteger(args[0]))
				player = CommandDimensionTeleport.getPlayer(args[0]);
			else
			{
				player = getPlayer(sender);
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
				tranferToDimension(player, null, worldServer, player.dimension, targetDimension, Double.parseDouble(args[1]),
						Double.valueOf(args[2]), Double.parseDouble(args[3]));

			break;
		case 5:
			player = CommandDimensionTeleport.getPlayer(args[0]);

			if (checkPlayer(sender, player) == false || !checkDimension(sender, args[1]) || checkInts(args, 2, sender) == false)
				break;

			targetDimension = Integer.parseInt(args[1]);
			worldServer = mcServer.worldServerForDimension(targetDimension);

			tranferToDimension(player, null, worldServer, player.dimension, targetDimension, Double.parseDouble(args[2]), Double.valueOf(args[3]),
					Double.parseDouble(args[4]));

			break;
		default:
			sender.addChatMessage(new ChatComponentTranslation("survivaltweaks.command.useage"));
			printUsage(sender);
			break;
		}

	}

	private static EntityPlayerMP getPlayer(ICommandSender sender)
	{
		if (sender instanceof EntityPlayerMP)
			return (EntityPlayerMP) sender;
		
		return null;
	}
	
	private static EntityPlayerMP getPlayer(String username)
	{
		return MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(username);
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos blockpos)
	{
		ArrayList<String> argsList = new ArrayList<>();
		
		if (args.length >= 1)
		{
			String lastLetter = args[args.length - 1];
			GameProfile[] profiles = MinecraftServer.getServer().getGameProfiles();

			if (args.length == 1 && lastLetter.regionMatches(true, 0, "list", 0, 4))
				argsList.add("list");

			for (int x = 0; x < profiles.length; x++)
			{
				if (lastLetter.regionMatches(true, 0, profiles[x].getName(), 0, profiles[x].getName().length())) 
				{
					argsList.add(profiles[x].getName());
				}
			}

			return argsList;
		}

		return argsList;
	}

	private String listIDs(EntityPlayerMP player)
	{
		StringBuilder str = new StringBuilder();
		Integer[] ids = DimensionManager.getIDs();
		int currectDimension = player.dimension;
		String name;
		String message;

		for (int x : ids)
		{
			DimensionChecker.add(x, DimensionManager.getProvider(x).getDimensionName());
		}

		for (int x : DimensionChecker.getIDList())
		{
			int id = x;
			if (id != currectDimension)
				message = (" [%i: %s] ");
			else
				message = (" [%i: %s" + "(" + StatCollector.translateToLocal("survivaltweaks.command.tpd.list.here") + ")] ");

			name = DimensionChecker.getName(x);

			message = message.replace("%i", String.valueOf(id));
			message = message.replace("%s", name);

			str.append(message);
		}

		return str.toString().trim();
	}

	private void printUsage(ICommandSender sender)
	{
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
		InterDimTeleporter teleporter;
		
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
		} else if (targetPlayer != null)
		{
			player.setPositionAndUpdate(x, y, z);
			worldServer.updateEntityWithOptionalForce(player, false); // just to be safe
		}
	}

	private void tranferToDimension(EntityPlayerMP player, WorldServer worldServer, int currentDimensionID, int targetDimensionID)
	{
		BlockPos coord = player.getBedLocation(targetDimensionID);
		
		if (coord == null)
			coord = worldServer.getSpawnPoint();
			
	    tranferToDimension(player, null, worldServer, currentDimensionID, targetDimensionID, coord.getX(), null, coord.getZ());
	}

	private void tranferToDimension(EntityPlayerMP player, WorldServer worldServer, int currentDimensionID, int targetDimensionID, int x, int z)
	{
		tranferToDimension(player, null, worldServer, currentDimensionID, targetDimensionID, x, null, z);
	}

	@Override
	public int compareTo(ICommand command)
	{
		return NAME.compareTo(command.getCommandName());
	}

	@Override
	public List<String> getCommandAliases()
	{
		ArrayList<String> aliases = new ArrayList<>();
		aliases.add(NAME);
		
		return aliases;
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender)
	{
		return sender.canCommandSenderUseCommand(2, NAME);
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index)
	{
		if (args.length >= 2)
			return SurvivalTweaks.isInteger(args[0]);
		return false;
	}

}
