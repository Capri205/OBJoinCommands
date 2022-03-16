package net.obmc.OBJoinCommands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class CommandListener implements CommandExecutor {

	static Logger log = Logger.getLogger( "Minecraft" );
	
	private String chatmsgprefix = null;
	private String logmsgprefix = null;
	
	public CommandListener() {
		chatmsgprefix = OBJoinCommands.getInstance().getChatMsgPrefix();
		logmsgprefix = OBJoinCommands.getInstance().getLogMsgPrefix();
	}
	
	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {

		// for now only op can use the command
		if ( !sender.isOp() ) {
			sender.sendMessage( ChatColor.RED + "Sorry, command is reserved for server operators." );
			return true;
		}

		// usage if no arguments passed
		if ( args.length == 0 ) {
			Usage( sender );
			return true;
		}

		// process the command and any arguments
		if ( command.getName().equalsIgnoreCase( "objc" ) ) {
			
			Player player = (Player) sender;
			int seqidx = 0;
			String type = null;
			String world = null;
			int seqnum = -1;
			String cmd = null;
			
			switch (args[0].toLowerCase()) {

				case "list":
				case "show":
					if ( args.length > 1 ) {
						if ( args[1].equals( "server" ) ) {
							sender.sendMessage( chatmsgprefix + "Server on join commands:" );
							Iterator<String> scit =  OBJoinCommands.getInstance().getServerCommands().iterator();
							int idx = 1;
							while ( scit.hasNext() ) {
								sender.sendMessage( chatmsgprefix + "  " + ChatColor.GOLD + idx + ". " + ChatColor.GRAY + scit.next() );
								idx++;
							}
							
						} else if ( args[1].equals( "world" ) ) {
							if ( args.length > 2 ) {
								world = args[2];
								Collection<String> worlds = OBJoinCommands.getInstance().getWorlds();
								if ( worlds.contains( world ) ) {
									sender.sendMessage( chatmsgprefix + "Commands for " + world + ":" );
									ArrayList<String> commands = OBJoinCommands.getInstance().getWorldCommands( world );
									int idx = 1;
									for ( String worldcmd : commands ) {
										sender.sendMessage( chatmsgprefix + "    " + ChatColor.GOLD + idx + ". " + ChatColor.GRAY + worldcmd );
										idx++;
									}								
								} else {
									sender.sendMessage( chatmsgprefix + "No commands for world '" + world + "'" );
								}
							} else {
								sender.sendMessage( chatmsgprefix + ChatColor.RED + "Please provide a world as part of the command.");
							}
						} else {
							sender.sendMessage( chatmsgprefix + ChatColor.RED + "Add command needs 'server' or 'world'." );
							return false;							
						}
					} else {
						if ( OBJoinCommands.getInstance().hasServerCommands() ) {
							sender.sendMessage( chatmsgprefix + "Server on join commands:" );
							Iterator<String> scit =  OBJoinCommands.getInstance().getServerCommands().iterator();
							int idx = 1;
							while ( scit.hasNext() ) {
								sender.sendMessage( chatmsgprefix + "  " + ChatColor.GOLD + idx + ". " + ChatColor.GRAY + scit.next() );
								idx++;
							}
						} else {
							sender.sendMessage( chatmsgprefix + "No server commands set" );
						}
						if ( OBJoinCommands.getInstance().hasWorldCommands() ) {
							sender.sendMessage( chatmsgprefix + "World commands:" );
							Collection<String> worlds = OBJoinCommands.getInstance().getWorlds();
							for ( String listworld : worlds ) {
								sender.sendMessage( chatmsgprefix + "  " + listworld + ":" );
								ArrayList<String> commands = OBJoinCommands.getInstance().getWorldCommands( listworld );
								int idx = 1;
								for ( String worldcmd : commands ) {
									sender.sendMessage( chatmsgprefix + "    " + ChatColor.GOLD + idx + ". " + ChatColor.GRAY + worldcmd );
									idx++;
								}
							}
						} else {
							sender.sendMessage( chatmsgprefix + "No worlds have commands set" );
						}
					}
					break;

				//objc add [ server | world <worldname> ] <sequence> "<command>"
				case "add":
					if ( args.length < 4 ) {
						sender.sendMessage( chatmsgprefix + ChatColor.RED + "You haven't provided enough paramters for this command." );
						return false;
					}
					if ( !args[1].equals( "server" ) && !args[1].equals( "world" ) ) {
						sender.sendMessage( chatmsgprefix + ChatColor.RED + "Add command needs 'server' or 'world'." );
						return false;
					}
					type = args[1];
					seqidx = 2;
					if ( args[1].equals( "world" ) ) {
						if ( !checkWorld( args[2] ) ) {
							sender.sendMessage( chatmsgprefix + ChatColor.RED + "Invalid world provided." );
							return false;
						}
						world = args[2];
						seqidx = 3;
					}
					try {
						seqnum = Integer.parseInt( args[seqidx] );
					} catch ( NumberFormatException e ) {
						sender.sendMessage( chatmsgprefix + ChatColor.RED + "Invalid sequence number provided." );
						return false;
					}
					if ( seqnum < 1 ) {
						sender.sendMessage( chatmsgprefix + ChatColor.RED + "Negative or zero sequence numbers aren't allowed." );
						return false;						
					}
					cmd = "";
					for ( int i = seqidx + 1; i < args.length; i++ ) {
						cmd += args[i] + " ";
					}
					OBJoinCommands.getInstance().insertCommand( type, world, seqnum, cmd );
					sender.sendMessage( chatmsgprefix + ChatColor.GREEN + "Command registered for sequence number " + seqnum );
					break;

				//objc del [ server | world <worldname> ] <sequence>
				case "delete":
				case "del":
					if ( args.length < 3 ) {
						sender.sendMessage( chatmsgprefix + ChatColor.RED + "You haven't provided enough paramters for this command." );
						return false;
					}
					if ( !args[1].equals( "server" ) && !args[1].equals( "world" ) ) {
						sender.sendMessage( chatmsgprefix + ChatColor.RED + "Remove command needs 'server' or 'world'." );
						return false;
					}
					type = args[1];
					seqidx = 2;
					if ( args[1].equals( "world" ) ) {
						if ( !checkWorld( args[2] ) ) {
							sender.sendMessage( chatmsgprefix + ChatColor.RED + "Invalid world provided." );
							return false;
						}
						world = args[2];
						seqidx = 3;
					}
					try {
						seqnum = Integer.parseInt( args[seqidx] );
					} catch ( NumberFormatException e ) {
						sender.sendMessage( chatmsgprefix + ChatColor.RED + "Invalid sequence number provided." );
						return false;
					}
					if ( seqnum < 1 ) {
						sender.sendMessage( chatmsgprefix + ChatColor.RED + "Negative or zero sequence numbers aren't allowed." );
						return false;						
					}
					
					String cmpstate = null;
					try {
						cmpstate = OBJoinCommands.getInstance().getCommand( type, world, seqnum );
					} catch ( NullPointerException e ) {
						sender.sendMessage( chatmsgprefix + ChatColor.RED + "No command found for that sequence number." );
						break;
					}
					String rtnstate = OBJoinCommands.getInstance().deleteCommand( type, world, seqnum );
					if ( !rtnstate.equals( cmpstate ) ) {
						sender.sendMessage( chatmsgprefix + ChatColor.RED + rtnstate );
					} else {
						sender.sendMessage( chatmsgprefix + ChatColor.GREEN + "Command at sequence number " + seqnum + " removed." );
						
					}
					break;

				default:
					sender.sendMessage( chatmsgprefix + ChatColor.RED + "Unknown command!");
					Usage(sender);
					break;
			}
		}
		return true;
	}

	// show player the command help
    void Usage(CommandSender sender) {
        sender.sendMessage(chatmsgprefix + "/obs show [server|world <worldname>]" + ChatColor.GOLD + " - Show available damage types");
        sender.sendMessage(chatmsgprefix + "/obs add  [server|world <worldname>] <sequence> command" + ChatColor.GOLD + " - Add a new command at sequence number");
        sender.sendMessage(chatmsgprefix + "/obs del  [server|world <worldname>] <sequence>" + ChatColor.GOLD + " - Remove command at sequence number");
    }
    
    
    // check a world exists
    Boolean checkWorld( String checkworld ) {
    	for ( World world : Bukkit.getWorlds() ) {
    		if ( world.getName().equals( checkworld ) ) {
    			return true;
    		}
    	}
    	return false;
    }
    
    // validates a sequence number and adjusts accordingly
    int validateSeqNum( String type, int seqnum ) {
    	return seqnum;
    }
}
