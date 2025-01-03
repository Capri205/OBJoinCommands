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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class OBJoinCommands extends JavaPlugin {

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
    private static String pluginPrefix = "[" + plugin + "]";
    private static String logMsgPrefix = pluginPrefix + " » ";
    private static TextComponent chatMsgPrefix = Component.text(plugin, NamedTextColor.AQUA, TextDecoration.BOLD)
        .append(Component.text(" » ", NamedTextColor.DARK_GRAY, TextDecoration.BOLD));
    
    public OBJoinCommands() {
        instance = this;
    }

    public static OBJoinCommands getInstance() {
        return instance;
    }

    public void onEnable() {

        refreshWorldList();

        initialize();

        register();

        log.log(Level.INFO, logMsgPrefix + "Plugin Version " + this.getPluginMeta().getVersion() + " activated!");
    }

    public void onDisable() {
        log.log(Level.INFO, logMsgPrefix + "Plugin deactivated!");
    }

    public void initialize() {
        this.saveDefaultConfig();
        
        try {
            this.delay = Long.parseLong(config.getString("delay"));
            if (this.delay < 0) {
                this.delay = 20L;
                log.log(Level.INFO, logMsgPrefix + "Negative delay value read. Setting to default 20 ticks (1 second)");
            }
            log.log(Level.INFO, logMsgPrefix + "Delay is " + this.delay);
        } catch (NumberFormatException e) {
            log.log(Level.INFO, logMsgPrefix + "Error reading delay from config. Setting to default 20 ticks (1 second)");
            delay = 20L;
        }
        
        // load up commands from config
        if (config.contains("server")) {
            ConfigurationSection serversection = config.getConfigurationSection("server"); 
            if (serversection.contains("commandlist")) {
                svrcmdlist = (ArrayList<String>) serversection.getStringList("commandlist");
                if (svrcmdlist != null && svrcmdlist.size() > 0) {
                    log.log(Level.INFO, logMsgPrefix + "Read " + svrcmdlist.size() + " server command" + (svrcmdlist.size() != 1 ? "s" : "") + " from config");
                } else {
                    log.log(Level.INFO, logMsgPrefix + "No server commands registered");
                }
            } else {
                log.log(Level.INFO, logMsgPrefix + "No commandlist in server section");
            }
        } else {
            log.log(Level.INFO, logMsgPrefix + "No server section in config");
        }
        
        // load up world commands
        if (config.contains("worlds")) {
            ConfigurationSection serversection = config.getConfigurationSection("worlds");
            Set<String> worlds = serversection.getKeys(false);
            if (worlds.size() > 0) {
                log.log(Level.INFO, getLogMsgPrefix() + "Found " + worlds.size() + " worlds");
                for (String world : worlds) {
                    if (serversection.getConfigurationSection(world).contains("commandlist")) {
                        ArrayList<String> worldcmdlist = (ArrayList<String>) serversection.getConfigurationSection(world).getStringList("commandlist");
                        wrldcmds.put(world,  worldcmdlist);
                        log.log(Level.INFO, logMsgPrefix + "Read " + worldcmdlist.size() + " command" + (worldcmdlist.size() != 1 ? "s" : "") + " for world '" + world + "'");
                    } else {
                        log.log(Level.INFO, logMsgPrefix + "No commandlist for world '" + world + "'");
                    }
                }
            } else {
                log.log(Level.INFO, logMsgPrefix + "No worlds registered in worlds section");
            }
            
        } else {
            log.log(Level.INFO, logMsgPrefix + "No worlds section in config");
        }
        
    }

    public void register() {
        // event listener
        this.listener = new EventListener();
        this.getServer().getPluginManager().registerEvents((Listener)this.listener, (Plugin)this);
        // command listener
        this.getCommand("objc").setExecutor(new CommandListener());
    }
    
    // return a single command by type
    public String getCommand(String type, int seqnum) {
        if (type.equals("server")) {
            if (svrcmdlist.size() > 0 && seqnum <= svrcmdlist.size()) {
                return svrcmdlist.get(seqnum - 1);
            }
        } else {
            log.log(Level.INFO, "debug - wrldcmds contains type: " + type);
            if (wrldcmds.keySet().contains(type)) {
                log.log(Level.INFO, "debug - seqnum: " + seqnum);
                log.log(Level.INFO, "debug - wrldcmds.type.size: " + wrldcmds.get(type).size());
                if (wrldcmds.get(type).size() > 0 && seqnum <= wrldcmds.get(type).size() ) {
                    return wrldcmds.get(type).get(seqnum - 1);
                }
            }
        }
        log.log(Level.INFO, "debug - returning null");
        return null;
    }

    // return server commands
    public ArrayList<String> getServerCommands() {
        return svrcmdlist;
    }

    // return worlds that have commands
    public Collection<String> getWorlds() {
        if (wrldcmds.keySet().size() > 0) {
            return wrldcmds.keySet();
        }
        return null;
    }

    // return commands for a world
    public ArrayList<String> getWorldCommands(String world) {
        if (wrldcmds.containsKey(world)) {
            return wrldcmds.get(world);
        }
        return new ArrayList<String>();
    }
    // return counts of server and world commands
    public Boolean hasServerCommands() {
        if (svrcmdlist.size() > 0) {
            return true;
        }
        return false;
    }

    public Boolean hasWorldCommands() {
        int cmdcnt = 0;
        if (wrldcmds.keySet().size() > 0) {
            for (String world : wrldcmds.keySet()) {
                cmdcnt += wrldcmds.get(world).size();
            }
            if (cmdcnt > 0) {
                return true;
            }
        }
        return false;
    }

    // checks if a world exists in the world commands
    public boolean isACommandWorld(String type) {
        log.log(Level.INFO, "debug - wrldcmds.keySet:");
        for (String world : wrldcmds.keySet()) {
            log.log(Level.INFO, "debug -    " + world);
        }
        if (wrldcmds.keySet().size() > 0 && wrldcmds.containsKey(type)) {
            return true;
        }
        return false;
    }    

    // check a command is valid - work in progress
    private Boolean validateCommand(String command) {
        List<String> firstArgs = new ArrayList<String>();
        for(HelpTopic t : this.getServer().getHelpMap().getHelpTopics()){
            firstArgs.add(t.getName().split(" ")[0].toLowerCase());
       }
        if(firstArgs.contains(command.split(" ")[0].toLowerCase())){
            return true;
       }
        return false;
    }
    
    public Boolean runCommand(Player player, String command, long delay) {
        if (delay > 0) {
            new BukkitRunnable() {
                public void run() {
                    cmdstate = player.performCommand(command);
                }
            }.runTaskLater(OBJoinCommands.getInstance(), delay);
            return cmdstate;
        } else {
            return player.performCommand(command);
        }
    }

    // build a list of worlds on the server
    public void refreshWorldList() {
        worldlist.clear();
        for (World world : Bukkit.getWorlds()) {
            worldlist.add(world.getName());
        }
    }

    // insert a command into a command list at the desired position, taking into account
    // things like position numbers being outside of current list sequence number range
    public void insertCommand(String type, int seqnum, String cmd) {
        int insertidx = 0;
        if (type.equals("server")) {
            if (svrcmdlist.size() == 0) {
                insertidx = 0;
            } else if (seqnum > svrcmdlist.size()) {
                insertidx = svrcmdlist.size();
            } else {
                insertidx = seqnum-1;
            }
            svrcmdlist.add(insertidx, cmd);
            config.getConfigurationSection("server").set("commandlist", svrcmdlist);
            this.saveConfig();
        } else {
            String world = type;
            log.log(Level.INFO, "debug - world: " + world + ", seqnum: " + seqnum + ", cmd: " + cmd);
            if (wrldcmds.keySet().contains(world)) {
                if (wrldcmds.get(world).size() == 0) {
                    insertidx = 0;
                } else if (seqnum > svrcmdlist.size()) {
                    insertidx = svrcmdlist.size();
                } else {
                    insertidx = seqnum-1;
                }
                wrldcmds.get(world).add(insertidx, cmd);
                config.getConfigurationSection("worlds").createSection(world);
                config.getConfigurationSection("worlds").getConfigurationSection(world).set("commandlist", wrldcmds.get(world));
                saveConfig();
            } else {
                // new array
                ArrayList<String> worldcmdlist = new ArrayList<String>();
                worldcmdlist.add(cmd);
                wrldcmds.put(world, worldcmdlist);
                config.getConfigurationSection("worlds").createSection(world);
                config.getConfigurationSection("worlds").getConfigurationSection(world).set("commandlist", worldcmdlist);
                saveConfig();
            }
        }
    }
    
    // remove a command from a list, ignoring any invalid sequences
    public String deleteCommand(String type, int seqnum) {
        String rtnstate = null;
        if (type.equals("server")) {
            if (svrcmdlist.size() > 0 && seqnum <= svrcmdlist.size()) {
                rtnstate = svrcmdlist.remove(seqnum - 1);
                config.getConfigurationSection("server").set("commandlist", svrcmdlist);
                saveConfig();
            } else {
                rtnstate = "No server command for that sequence number";
            }
        } else {
            if (wrldcmds.keySet().contains(type)) {
                if (wrldcmds.get(type).size() > 0 && seqnum <= wrldcmds.get(type).size() ) {
                    rtnstate = wrldcmds.get(type).remove(seqnum - 1);
                    if (wrldcmds.get(type).size() == 0) {
                        wrldcmds.remove(type);
                    }
                    config.getConfigurationSection("worlds").createSection(type);
                    config.getConfigurationSection("worlds").getConfigurationSection(type).set("commandlist", wrldcmds.get(type));
                    saveConfig();
                } else {
                    rtnstate = "No command in '" + type + "' for that sequence number.";
                }
            } else {
                rtnstate = "No commands registed for '" + type + "'.";
            }
        }
        return rtnstate;
    }

    public long getDelay() {
        return delay;
    }
    public void setDelay(long delay) {
        this.delay = delay;
        config.set("delay", delay);
        saveConfig();
    }
    
    // Consistent messaging
    public String getPluginName() {
        return plugin;
    }
    public String getPluginPrefix() {
        return pluginPrefix;
    }
    public Component getChatMsgPrefix() {
        return chatMsgPrefix;
    }
    public String getLogMsgPrefix() {
        return logMsgPrefix;
    }
}