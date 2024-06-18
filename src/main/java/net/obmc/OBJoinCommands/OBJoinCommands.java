package net.obmc.OBJoinCommands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.help.HelpTopic;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public class OBJoinCommands extends JavaPlugin
{

	static Logger log = Logger.getLogger("Minecraft");

	public static OBJoinCommands instance;
	private EventListener listener;

	private Configuration config = this.getConfig();

	private static ArrayList<String> worldlist = new ArrayList<String>();
	private static ArrayList<String> svrcmdlist = new ArrayList<String>();
	private HashMap<String, ArrayList<String>> wrldcmds = new HashMap<String, ArrayList<String>>();
	private long delay = 20L;
	private Boolean cmdstate = null;


	private static String plugin = "OBJoinCommands";
	private static String pluginprefix = "[" + plugin + "] ";
	private static String chatmsgprefix = ChatColor.AQUA + "" + ChatColor.BOLD + plugin + ChatColor.DARK_GRAY + ChatColor.BOLD + " » " + ChatColor.LIGHT_PURPLE + "";
	private static String logmsgprefix = pluginprefix + "- ";
	
	public OBJoinCommands() {
		instance = this;
	}

	// Make our (public) main class methods and variables available to other classes
	public static OBJoinCommands getInstance() {
    	return instance;
    }

	// plugin enable
	public void onEnable() {

		refreshWorldList();

		initializeStuff();

		registerStuff();

		log.log(Level.INFO, getLogMsgPrefix() + "Plugin Version " + this.getDescription().getVersion() + " activated!");
	}

	// plugin disable
	public void onDisable() {
		log.log(Level.INFO, getLogMsgPrefix() + "Plugin deactivated!");
	}

	// initialize plugin
	public void initializeStuff() {
		this.saveDefaultConfig();
		//Configuration config = this.getConfig();
		
		try {
			this.delay = Long.parseLong( config.getString( "delay" ) );
			if ( this.delay < 0 ) {
				this.delay = 20L;
				log.log( Level.INFO, getLogMsgPrefix() + "Negative delay value read. Setting to default 20 ticks (1 second)");
			}
			log.log( Level.INFO, getLogMsgPrefix() + "Delay is " + this.delay);
		} catch ( NumberFormatException e ) {
			log.log( Level.INFO, getLogMsgPrefix() + "Error reading delay from config. Setting to default 20 ticks (1 second)");
			delay = 20L;
		}
		
		// load up commands from config
		if ( config.contains( "server" ) ) {
			ConfigurationSection serversection = config.getConfigurationSection( "server" ); 
			if ( serversection.contains( "commandlist" ) ) {
				svrcmdlist = (ArrayList<String>) serversection.getStringList( "commandlist" );
				if ( svrcmdlist != null && svrcmdlist.size() > 0 ) {
					log.log( Level.INFO, getLogMsgPrefix() + "Read " + svrcmdlist.size() + " server command" + ( svrcmdlist.size() != 1 ? "s" : "" ) + " from config" );
// disable for now as it only works for server commands and not custom plugin commands
//					int idx = 0;
//					for ( String command : svrcmdlist ) {
//						if ( !validateCommand( command ) ) {
//							log.log(Level.INFO, getLogMsgPrefix() + "Invalid server command at index " + idx + " : " + command );
//							svrcmdlist.remove(idx);
//						}
//						idx++;
//					}
				} else {
					log.log( Level.INFO, getLogMsgPrefix() + "No server commands registered");
				}
			} else {
				log.log( Level.INFO, getLogMsgPrefix() + "No commandlist in server section");
			}
		} else {
			log.log( Level.INFO, getLogMsgPrefix() + "No server section in config");
		}
		
		// load up world commands
		if ( config.contains( "worlds" ) ) {
			ConfigurationSection serversection = config.getConfigurationSection( "worlds" );
			Set<String> worlds = serversection.getKeys(false);
			if ( worlds.size() > 0 ) {
				log.log( Level.INFO, getLogMsgPrefix() + "Found " + worlds.size() + " worlds");
				for ( String world : worlds ) {
					if ( serversection.getConfigurationSection( world ).contains( "commandlist" ) ) {
						ArrayList<String> worldcmdlist = (ArrayList<String>) serversection.getConfigurationSection( world ).getStringList( "commandlist" );
						wrldcmds.put( world,  worldcmdlist );
						log.log( Level.INFO, getLogMsgPrefix() + "Read " + worldcmdlist.size() + " command" + ( worldcmdlist.size() != 1 ? "s" : "" ) + " for world '" + world + "'" );
					} else {
						log.log( Level.INFO, getLogMsgPrefix() + "No commandlist for world '" + world + "'" );
					}
				}
			} else {
				log.log( Level.INFO, getLogMsgPrefix() + "No worlds registered in worlds section");
			}
			
		} else {
			log.log( Level.INFO, getLogMsgPrefix() + "No worlds section in config");
		}
		
	}

	// register our listeners
	public void registerStuff() {
		// event listener
        this.listener = new EventListener();
        this.getServer().getPluginManager().registerEvents((Listener)this.listener, (Plugin)this);
        // command listener
        this.getCommand("objc").setExecutor(new CommandListener());
	}
	
	// return a single command
	public String getCommand( String type, String world, int seqnum ) {
		if ( type.equals( "server" ) ) {
			if ( svrcmdlist.size() > 0 && seqnum <= svrcmdlist.size() ) {
				return svrcmdlist.get( seqnum - 1 );
			}
		} else {
			if ( wrldcmds.keySet().contains( world ) ) {
				if ( wrldcmds.get( world ).size() > 0 && seqnum <= wrldcmds.get( world ).size()  ) {
					return wrldcmds.get( world ).get( seqnum - 1 );
				}
			}
		}
		return null;
	}
	// return server commands
	public ArrayList<String> getServerCommands() {
		return svrcmdlist;
	}
	// return worlds that have commands
	public Collection<String> getWorlds() {
		if ( wrldcmds.keySet().size() > 0 ) {
			return wrldcmds.keySet();
		}
		return null;
	}
	// return commands for a world
	public ArrayList<String> getWorldCommands( String world ) {
		if ( wrldcmds.containsKey( world ) ) {
			return wrldcmds.get( world );
		}
		return new ArrayList<String>();
	}
	// return counts of server and world commands
	public Boolean hasServerCommands() {
		if ( svrcmdlist.size() > 0 ) {
			return true;
		}
		return false;
	}
	public Boolean hasWorldCommands() {
		int cmdcnt = 0;
		if ( wrldcmds.keySet().size() > 0 ) {
			for ( String world : wrldcmds.keySet() ) {
				cmdcnt += wrldcmds.get( world ).size();
			}
			if ( cmdcnt > 0 ) {
				return true;
			}
		}
		return false;
	}
	
	// check a command is valid - not working for non-system/custom plugin commands
	private Boolean validateCommand( String command ) {
		List<String> firstArgs = new ArrayList<String>();
		for(HelpTopic t : this.getServer().getHelpMap().getHelpTopics()){
            firstArgs.add(t.getName().split(" ")[0].toLowerCase());
        }
        if(firstArgs.contains(command.split(" ")[0].toLowerCase())){
            return true;
        }
        return false;
	}
	
	public Boolean runCommand( Player player, String command, long delay ) {
		if ( delay > 0 ) {
			new BukkitRunnable() {
				public void run() {
					cmdstate = player.performCommand( command );
				}
			}.runTaskLater(OBJoinCommands.getInstance(), delay);
			return cmdstate;
		} else {
			return player.performCommand( command );
		}
	}

	// Consistent messaging
	public String getPluginName() {
		return plugin;
	}
	public String getPluginPrefix() {
		return pluginprefix;
	}
	public String getChatMsgPrefix() {
		return chatmsgprefix;
	}
	public String getLogMsgPrefix() {
		return logmsgprefix;
	}
	
	// build a list of worlds on the server
	public void refreshWorldList() {
		worldlist.clear();
		for ( World world : Bukkit.getWorlds() ) {
			worldlist.add( world.getName() );
		}
	}

	// insert a command into a command list at the desired position, taking into account
	// things like position numbers being outside of current list sequence number range
	public void insertCommand(String type, String world, int seqnum, String cmd) {
		int insertidx = 0;
		if ( type.equals( "server" ) ) {
			if ( svrcmdlist.size() == 0 ) {
				insertidx = 0;
			} else if ( seqnum > svrcmdlist.size() ) {
				insertidx = svrcmdlist.size();
			} else {
				insertidx = seqnum-1;
			}
			svrcmdlist.add( insertidx, cmd );
			config.getConfigurationSection( "server" ).set( "commandlist", svrcmdlist );
			saveConfig();
		} else {
			if ( wrldcmds.keySet().contains( world ) ) {
				if ( wrldcmds.get( world ).size() == 0 ) {
					insertidx = 0;
				} else if ( seqnum > svrcmdlist.size() ) {
					insertidx = svrcmdlist.size();
				} else {
					insertidx = seqnum-1;
				}
				wrldcmds.get( world ).add( insertidx, cmd );
				config.getConfigurationSection( "worlds" ).createSection( world );
				config.getConfigurationSection( "worlds" ).getConfigurationSection( world ).set( "commandlist", wrldcmds.get( world ) );
				saveConfig();
			} else {
				// new array
				ArrayList<String> worldcmdlist = new ArrayList<String>();
				worldcmdlist.add( cmd );
				wrldcmds.put( world, worldcmdlist );
				config.getConfigurationSection( "worlds" ).createSection( world );
				config.getConfigurationSection( "worlds" ).getConfigurationSection( world ).set( "commandlist", worldcmdlist );
				saveConfig();
			}
		}
	}
	
	// remove a command from a list, ignoring any invalid sequences
	public String deleteCommand( String type, String world, int seqnum ) {
		String rtnstate = null;
		if ( type.equals( "server" ) ) {
			if ( svrcmdlist.size() > 0 && seqnum <= svrcmdlist.size() ) {
				rtnstate = svrcmdlist.remove( seqnum - 1 );
				config.getConfigurationSection( "server" ).set( "commandlist", svrcmdlist );
				saveConfig();
			} else {
				rtnstate = "No server command for that sequence number.";
			}
		} else {
			if ( wrldcmds.keySet().contains( world ) ) {
				if ( wrldcmds.get( world ).size() > 0 && seqnum <= wrldcmds.get( world ).size()  ) {
					rtnstate = wrldcmds.get( world ).remove( seqnum - 1 );
					if ( wrldcmds.get( world ).size() == 0 ) {
						wrldcmds.remove( world );
					}
					config.getConfigurationSection( "worlds" ).createSection( world );
					config.getConfigurationSection( "worlds" ).getConfigurationSection( world ).set( "commandlist", wrldcmds.get( world ) );
					saveConfig();
				} else {
					rtnstate = "No command in '" + world + "' for that sequence number.";
				}
			} else {
				rtnstate = "No commands registed for '" + world + "'.";
			}
		}
		return rtnstate;
	}

	public long getDelay() {
		return delay;
	}
	public void setDelay( long delay ) {
		this.delay = delay;
		config.set( "delay", delay );
		saveConfig();
	}
}
