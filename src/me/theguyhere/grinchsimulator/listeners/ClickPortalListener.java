package me.theguyhere.grinchsimulator.listeners;

import me.theguyhere.grinchsimulator.GUI.Inventories;
import me.theguyhere.grinchsimulator.Main;
import me.theguyhere.grinchsimulator.events.JoinArenaEvent;
import me.theguyhere.grinchsimulator.events.LeftClickNPCEvent;
import me.theguyhere.grinchsimulator.events.RightClickNPCEvent;
import me.theguyhere.grinchsimulator.game.models.arenas.Arena;
import me.theguyhere.grinchsimulator.game.models.arenas.ArenaManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class ClickPortalListener implements Listener {
	@EventHandler
	public void onRightClick(RightClickNPCEvent e) {
		Arena arena;

		// Try to get arena from npc
		try {
			arena = Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull)
					.filter(arena1 -> arena1.getPortal() != null)
					.filter(arena1 -> arena1.getPortal().getNpc().getCreeper().getEntityId() == e.getNpcId())
					.collect(Collectors.toList()).get(0);
		} catch (Exception err) {
			err.printStackTrace();
			return;
		}

		// Send out event of player joining
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(Main.class), () ->
				Bukkit.getPluginManager().callEvent(new JoinArenaEvent(e.getPlayer(), arena)));
	}

	@EventHandler
	public void onLeftClick(LeftClickNPCEvent e) {
		Arena arena;

		// Try to get arena from npc
		try {
			arena = Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull)
					.filter(arena1 -> arena1.getPortal() != null)
					.filter(arena1 -> arena1.getPortal().getNpc().getCreeper().getEntityId() == e.getNpcId())
					.collect(Collectors.toList()).get(0);
		} catch (Exception err) {
			err.printStackTrace();
			return;
		}

		// Open inventory
		e.getPlayer().openInventory(Inventories.createArenaInfoInventory(arena));
	}
}
