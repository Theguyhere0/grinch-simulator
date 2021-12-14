package me.theguyhere.grinchsimulator.listeners;

import me.theguyhere.grinchsimulator.Main;
import me.theguyhere.grinchsimulator.game.models.arenas.ArenaManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class DeathListener implements Listener {
	private final Main plugin;

	public DeathListener(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		plugin.getReader().uninject(e.getEntity());
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		ArenaManager.displayEverything(e.getPlayer());
		plugin.getReader().inject(e.getPlayer());
	}
}
