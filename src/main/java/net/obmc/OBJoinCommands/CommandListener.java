package net.obmc.OBJoinCommands;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandListener implements CommandExecutor {

	static Logger log = Logger.getLogger("Minecraft");
	
	private Component chatMsgPrefix = null;
	
	public CommandListener() {
		chatMsgPrefix = OBJoinCommands.getInstance().getChatMsgPrefix();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		// for now only op can use the command
		if (!sender.isOp()) {
			sender.sendMessage(Component.text("Sorry, command is reserved for server operators.", NamedTextColor.RED));
			return true;
		}

		// usage if no arguments passed
		if (args.length == 0) {
			Usage(sender);
			return true;
		}

		// process the command and any arguments
		if (command.getName().equalsIgnoreCase("objc")) {
			
			Player player = (Player) sender;
			String type = null;
			String world = null;
			int seqnum = -1;
			String cmd = null;
			
			switch (args[0].toLowerCase()) {

				case "help":
					Usage(player);
					break;

				case "list":
				case "show":
					if (args.length > 1) {
						if (args[1].equals("server")) {
							sender.sendMessage(chatMsgPrefix.append(Component.text("Server on join commands:", NamedTextColor.LIGHT_PURPLE)));
							Iterator<String> scit =  OBJoinCommands.getInstance().getServerCommands().iterator();
							int idx = 1;
							while (scit.hasNext()) {
								sender.sendMessage(chatMsgPrefix.append(Component.text(idx + ". ", NamedTextColor.GOLD))
								    .append(Component.text(scit.next(), NamedTextColor.GRAY)));
								idx++;
							}
							
						} else if (args[1].equals("world")) {
							if (args.length > 2) {
								world = args[2];
								Collection<String> worlds = OBJoinCommands.getInstance().getWorlds();
								if (worlds.contains(world)) {
									sender.sendMessage(chatMsgPrefix.append(Component.text("Commands for " + world + ":", NamedTextColor.LIGHT_PURPLE)));
									ArrayList<String> commands = OBJoinCommands.getInstance().getWorldCommands(world);
									int idx = 1;
									for (String worldcmd : commands) {
										sender.sendMessage(chatMsgPrefix.append(Component.text(idx + ". ", NamedTextColor.GOLD))
										      .append(Component.text(worldcmd, NamedTextColor.GRAY)));
										idx++;
									}								
								} else {
									sender.sendMessage(chatMsgPrefix.append(Component.text("No commands for world '" + world + "'", NamedTextColor.LIGHT_PURPLE)));
								}
							} else {
								sender.sendMessage(chatMsgPrefix.append(Component.text("Please provide a world as part of the command.", NamedTextColor.RED)));
							}
						} else {
							sender.sendMessage(chatMsgPrefix.append(Component.text("Add command needs 'server' or 'world'.", NamedTextColor.RED)));
							return false;							
						}
					} else {
						if (OBJoinCommands.getInstance().hasServerCommands()) {
							sender.sendMessage(chatMsgPrefix.append(Component.text("Server on join commands:", NamedTextColor.LIGHT_PURPLE)));
							Iterator<String> scit =  OBJoinCommands.getInstance().getServerCommands().iterator();
							int idx = 1;
							while (scit.hasNext()) {
								sender.sendMessage(chatMsgPrefix.append(Component.text(idx + ". ", NamedTextColor.GOLD))
								        .append(Component.text(scit.next(), NamedTextColor.GRAY)));
								idx++;
							}
						} else {
							sender.sendMessage(chatMsgPrefix.append(Component.text("No server commands set", NamedTextColor.LIGHT_PURPLE)));
						}
						if (OBJoinCommands.getInstance().hasWorldCommands()) {
							sender.sendMessage(chatMsgPrefix.append(Component.text("World commands:", NamedTextColor.LIGHT_PURPLE)));
							Collection<String> worlds = OBJoinCommands.getInstance().getWorlds();
							for (String listworld : worlds) {
								sender.sendMessage(chatMsgPrefix.append(Component.text("  " + listworld + ":", NamedTextColor.LIGHT_PURPLE)));
								ArrayList<String> commands = OBJoinCommands.getInstance().getWorldCommands(listworld);
								int idx = 1;
								for (String worldcmd : commands) {
									sender.sendMessage(chatMsgPrefix.append(Component.text("    " + idx + ". ", NamedTextColor.GOLD))
									        .append(Component.text(worldcmd, NamedTextColor.GRAY)));
									idx++;
								}
							}
						} else {
							sender.sendMessage(chatMsgPrefix.append(Component.text("No worlds have commands set", NamedTextColor.LIGHT_PURPLE)));
						}
					}
					break;

				//objc add [ server | <worldname> ] <sequence> "<command>"
				case "add":
				    log.log(Level.INFO, "debug - args.len: " + args.length);
				    for(int i = 0; i < args.length; i++) {
				        log.log(Level.INFO, "debug -   " + i + " : " + args[i]);
				    }
				    // validate we have enough arguments 
					if (args.length < 4) {
						sender.sendMessage(chatMsgPrefix.append(Component.text("You haven't provided enough paramters for /obrep add", NamedTextColor.RED)));
						return false;
					}
					// validate server or world
					type = args[1];
					if (!type.equals("server")) {
					    if (!checkWorld(args[1])) {
                            sender.sendMessage(chatMsgPrefix.append(Component.text("Invalid world name provided", NamedTextColor.RED)));
                            return false;
                        }
					}
					// validate sequence number is a number
					try {
						seqnum = Integer.parseInt(args[2]);
					} catch (NumberFormatException e) {
						sender.sendMessage(chatMsgPrefix.append(Component.text("Invalid sequence number provided", NamedTextColor.RED)));
						return false;
					}
					if (seqnum < 1) {
						sender.sendMessage(chatMsgPrefix.append(Component.text("Negative or zero sequence numbers aren't allowed", NamedTextColor.RED)));
						return false;						
					}
					// build out the command from the remaining arguments
					cmd = "";
					for (int i = 3; i < args.length; i++) {
						cmd += args[i] + " ";
					}
					cmd = cmd.trim();
					log.log(Level.INFO, "debug - type: " + type + ", seqnum: " + seqnum + ", cmd: '" + cmd + "'");
					OBJoinCommands.getInstance().insertCommand(type, seqnum, cmd);
					sender.sendMessage(chatMsgPrefix.append(Component.text("Command registered for sequence number " + seqnum, NamedTextColor.GREEN)));
					break;

				//objc del [ server | <worldname> ] <sequence>
				case "delete":
				case "del":
					if (args.length < 3) {
						sender.sendMessage(chatMsgPrefix.append(Component.text("You haven't provided enough paramters for this command", NamedTextColor.RED)));
						return false;
					}
					type = args[1];
					if (!type.equals("server") && !OBJoinCommands.getInstance().isACommandWorld(type)) {
					    sender.sendMessage(chatMsgPrefix.append(Component.text("World '" + type + "' has no commands or doesn't exist", NamedTextColor.RED)));
                        return true;
					}
					try {
						seqnum = Integer.parseInt(args[2]);
					} catch (NumberFormatException e) {
						sender.sendMessage(chatMsgPrefix.append(Component.text("Invalid sequence number provided", NamedTextColor.RED)));
						return false;
					}
					if (seqnum < 1) {
						sender.sendMessage(chatMsgPrefix.append(Component.text("Negative or zero sequence numbers aren't allowed", NamedTextColor.RED)));
						return false;						
					}
					
					String cmpstate = null;
					try {
						cmpstate = OBJoinCommands.getInstance().getCommand(type, seqnum);
					} catch (NullPointerException e) {
						sender.sendMessage(chatMsgPrefix.append(Component.text("No command found for that sequence number", NamedTextColor.RED)));
						break;
					}
					String rtnstate = OBJoinCommands.getInstance().deleteCommand(type, seqnum);
					if (!rtnstate.equals(cmpstate)) {
						sender.sendMessage(chatMsgPrefix.append(Component.text(rtnstate, NamedTextColor.RED)));
					} else {
						sender.sendMessage(chatMsgPrefix.append(Component.text("Command at sequence number " + seqnum + " removed", NamedTextColor.GREEN)));
						
					}
					break;

				case "set":
					if (args.length < 2) {
						sender.sendMessage(chatMsgPrefix.append(Component.text("You haven't provided enough paramters for this command", NamedTextColor.RED)));
						return false;
					}
					long delay = -1;
					if (args[1].equals("delay")) {
						try {
							delay = Long.parseLong(args[2]);
							if (delay < 0) {
								sender.sendMessage(chatMsgPrefix.append(Component.text("A negative delay isn't allowed", NamedTextColor.RED)));
								return false;
							}
						} catch (NumberFormatException e) {
							sender.sendMessage(chatMsgPrefix.append(Component.text("Invalid delay provided", NamedTextColor.RED)));
							return false;
						}
					} else {
						sender.sendMessage(chatMsgPrefix.append(Component.text("Unknown parameter for set command", NamedTextColor.RED)));
						return false;
					}
					OBJoinCommands.getInstance().setDelay(delay);
					double delayseconds = delay / 20;
					BigDecimal ds = new BigDecimal(delayseconds).setScale(0, RoundingMode.HALF_DOWN);
					sender.sendMessage(chatMsgPrefix.append(Component.text("Delay set to " + delay + " ticks (" + ds + " second" + (ds.intValue() != 1 ? "s" : "") + ")", NamedTextColor.GREEN)));
					break;

				default:
					sender.sendMessage(chatMsgPrefix.append(Component.text("Unknown command!", NamedTextColor.RED)));
					Usage(sender);
					break;
			}
		}
		return true;
	}

	// show player the command help
    void Usage(CommandSender sender) {
        sender.sendMessage(chatMsgPrefix.append(Component.text("/obs show [server|<worldname>]", NamedTextColor.LIGHT_PURPLE))
            .append(Component.text(" - Show all or some commands", NamedTextColor.GOLD)));
        sender.sendMessage(chatMsgPrefix.append(Component.text("/obs add  [server|<worldname>] <sequence> command", NamedTextColor.LIGHT_PURPLE))
            .append(Component.text(" - Add a new command at sequence number", NamedTextColor.GOLD)));
        sender.sendMessage(chatMsgPrefix.append(Component.text("/obs del  [server|<worldname>] <sequence>", NamedTextColor.LIGHT_PURPLE))
            .append(Component.text(" - Remove command at sequence number", NamedTextColor.GOLD)));
        sender.sendMessage(chatMsgPrefix.append(Component.text("/obs set delay <delayinticks>", NamedTextColor.LIGHT_PURPLE))
            .append(Component.text(" - Set the global command delay", NamedTextColor.GOLD)));
   }
    
    // check a world exists
    boolean checkWorld(String checkworld) {
        return Bukkit.getWorlds().stream() .anyMatch(world -> world.getName().equals(checkworld));
   }
    
    // validates a sequence number and adjusts accordingly
    int validateSeqNum(String type, int seqnum) {
    	return seqnum;
   }
}
