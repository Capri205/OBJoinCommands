package net.obmc.OBJoinCommands;

import java.util.Iterator;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class EventListener implements Listener
{
	static Logger log = Logger.getLogger( "Minecraft" );
	static ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

  
	@EventHandler
	public void onPlayerJoin( PlayerJoinEvent event ) {
		// perform any server commands
		doServerCommands( event.getPlayer() );
		// perform any world commands
		doWorldCommands( event.getPlayer() );
	}

	@EventHandler
	public void onWorldChange( PlayerChangedWorldEvent event ) {
		Player player = event.getPlayer();
		doWorldCommands( player );
	}
	
	@EventHandler
	public void onWorldInit( WorldInitEvent event ) {
		OBJoinCommands.getInstance().refreshWorldList();
	}

	@EventHandler
	public void onWorldUnload( WorldUnloadEvent event ) {
		new BukkitRunnable() {
			public void run() {
				OBJoinCommands.getInstance().refreshWorldList();
			}
		}.runTaskLater(OBJoinCommands.getInstance(), 20L);
	}

	// perform any server commands
	private void doServerCommands( Player player ) {
		Iterator<String> scit =  OBJoinCommands.getInstance().getServerCommands().iterator();
		while ( scit.hasNext() ) {
			String command = scit.next();
			command = command.replaceAll( "@p", player.getName() ).replaceAll( "@s", player.getName() );
			doCommand( command, OBJoinCommands.getInstance().getDelay() );
		}
	}
	// perform any world commands
	private void doWorldCommands( Player player ) {
		String worldname = player.getWorld().getName();
		Iterator<String> wcit =  OBJoinCommands.getInstance().getWorldCommands( worldname ).iterator();
		while ( wcit.hasNext() ) {
			String command = wcit.next();
			command = command.replaceAll( "@p", player.getName() ).replaceAll( "@s", player.getName() );
			doCommand( command, OBJoinCommands.getInstance().getDelay() );
		}
	}
	
	// execute a single command
	private void doCommand( String command, long delay ) {
		new BukkitRunnable() {
			public void run() {
				Bukkit.dispatchCommand( console, command );
			}
		}.runTaskLater(OBJoinCommands.getInstance(), delay);
	}
}

