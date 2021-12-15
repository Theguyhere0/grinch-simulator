package me.theguyhere.grinchsimulator.listeners;

import me.theguyhere.grinchsimulator.GUI.InventoryItems;
import me.theguyhere.grinchsimulator.Main;
import me.theguyhere.grinchsimulator.events.LeaveArenaEvent;
import me.theguyhere.grinchsimulator.game.models.arenas.Arena;
import me.theguyhere.grinchsimulator.game.models.arenas.ArenaManager;
import me.theguyhere.grinchsimulator.game.models.arenas.ArenaStatus;
import me.theguyhere.grinchsimulator.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;

public class GameListener implements Listener {
	private final Main plugin;

	public GameListener(Main plugin) {
		this.plugin = plugin;
	}
	
	// Stop automatic game mode switching between worlds
	@EventHandler
	public void onGameModeSwitch(PlayerGameModeChangeEvent e) {
		if (Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).anyMatch(a -> a.hasPlayer(e.getPlayer())) &&
				e.getNewGameMode() == GameMode.SURVIVAL) e.setCancelled(true);
	}

	// Prevent players from going hungry while waiting for an arena to start
	@EventHandler
	public void onHunger(FoodLevelChangeEvent e) {
		Player player = (Player) e.getEntity();

		// See if the player is in a game
		if (Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).noneMatch(a -> a.hasPlayer(player)))
			return;

		e.setCancelled(true);
	}

	// Open shop, kit selecting menu, or leave
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		// Check for right click
		if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		Player player = e.getPlayer();

		// See if the player is in a game
		if (Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).noneMatch(a -> a.hasPlayer(player)))
			return;

		// Get item in hand
		ItemStack item;
		if (e.getHand() == EquipmentSlot.OFF_HAND)
			item = Objects.requireNonNull(player.getEquipment()).getItemInOffHand();
		else item = Objects.requireNonNull(player.getEquipment()).getItemInMainHand();

		// Make player leave
		if (InventoryItems.leave().equals(item))
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
					Bukkit.getPluginManager().callEvent(new LeaveArenaEvent(player)));

		// Ignore
		else return;

		// Cancel interaction
		e.setCancelled(true);
	}

	// Handles players falling into the void
	@EventHandler
	public void onVoidDamage(EntityDamageEvent e) {
		// Check for player taking damage
		if (!(e.getEntity() instanceof Player player)) return;

		Arena arena;

		// Check for void damage
		if (!e.getCause().equals(EntityDamageEvent.DamageCause.VOID)) return;

		// Attempt to get arena and player
		try {
			arena = Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).filter(a -> a.hasPlayer(player))
					.toList().get(0);
		} catch (Exception err) {
			return;
		}

		// Cancel void damage
		e.setCancelled(true);

		// Teleport player back to player spawn or waiting room
		if (arena.getWaitingRoom() == null)
			try {
				player.teleport(arena.getPlayerSpawn());
			} catch (NullPointerException err) {
				Utils.debugError(err.getMessage(), 0);
			}
		else player.teleport(arena.getWaitingRoom());
	}

	// Prevent players from teleporting when in a game
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		Player player = e.getPlayer();

		// Check if player is playing in an arena
		if (Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).noneMatch(a -> a.hasPlayer(player)))
			return;

		Arena arena = Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).filter(a -> a.hasPlayer(player))
				.toList().get(0);

		// Check if the arena has started
		if (arena.getStatus() == ArenaStatus.WAITING)
			return;

		// Cancel teleport and notify
		e.setCancelled(true);
		player.sendMessage(Utils.notify(plugin.getLanguageData().getString("teleportError")));
	}

	// Prevent players from dropping standard game items
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent e) {
		Player player = e.getPlayer();
		ItemStack item = e.getItemDrop().getItemStack();

		// Check if player is in an arena
		if (Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).noneMatch(arena -> arena.hasPlayer(player)))
			return;

		// Check for GUI items
		if (item.equals(InventoryItems.leave()))
			e.setCancelled(true);
	}

	// Prevent moving items around while waiting for game to start
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		Arena arena;

		// Attempt to get arena and player
		try {
			arena = Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).filter(a -> a.hasPlayer(player))
					.toList().get(0);
			arena.getPlayer(player);
		} catch (Exception err) {
			return;
		}

		// Cancel event if arena is in waiting mode
		if (arena.getStatus() == ArenaStatus.WAITING)
			e.setCancelled(true);
	}

	// Prevent swapping items while waiting for game to start
	@EventHandler
	public void onSwap(PlayerSwapHandItemsEvent e) {
		Player player = e.getPlayer();
		Arena arena;

		// Attempt to get arena and player
		try {
			arena = Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).filter(a -> a.hasPlayer(player))
					.toList().get(0);
			arena.getPlayer(player);
		} catch (Exception err) {
			return;
		}

		// Cancel event if arena is in waiting mode
		if (arena.getStatus() == ArenaStatus.WAITING)
			e.setCancelled(true);
	}
}
