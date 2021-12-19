package me.theguyhere.grinchsimulator.listeners;

import me.theguyhere.grinchsimulator.GUI.Inventories;
import me.theguyhere.grinchsimulator.GUI.InventoryItems;
import me.theguyhere.grinchsimulator.Main;
import me.theguyhere.grinchsimulator.events.LeaveArenaEvent;
import me.theguyhere.grinchsimulator.exceptions.PlayerNotFoundException;
import me.theguyhere.grinchsimulator.game.models.arenas.Arena;
import me.theguyhere.grinchsimulator.game.models.arenas.ArenaManager;
import me.theguyhere.grinchsimulator.game.models.arenas.ArenaStatus;
import me.theguyhere.grinchsimulator.game.models.players.GPlayer;
import me.theguyhere.grinchsimulator.game.models.presents.PresentType;
import me.theguyhere.grinchsimulator.tools.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

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

	// Handle using pre game items, editor items, or clicking on present
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		// Check for right click
		if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		// Check for main hand
		if (e.getHand() == EquipmentSlot.OFF_HAND)
			return;

		Player player = e.getPlayer();
		Block block = e.getClickedBlock();
		ItemStack item = Objects.requireNonNull(player.getEquipment()).getItemInMainHand();

		// See if the player is in a game
		if (Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).anyMatch(a -> a.hasPlayer(player))) {
			Arena arena = Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull)
					.filter(a -> a.hasPlayer(player)).toList().get(0);

			// Make player leave
			if (InventoryItems.leave().equals(item))
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
						Bukkit.getPluginManager().callEvent(new LeaveArenaEvent(player)));

			// Handle present
			else if (block != null && arena.checkPresent(block.getLocation())) {
				Location blockLocation = block.getLocation();

				// Check for head
				if (block.getType() != Material.PLAYER_HEAD) {
					arena.removePresent(blockLocation);
					return;
				}

				// Check for arena in session
				if (arena.getStatus() != ArenaStatus.ACTIVE)
					return;

				// Check if already found
				if (arena.checkFound(blockLocation)) {
					// Todo
					player.sendMessage(Utils.notify("&cPresent already found!"));
					return;
				}

				// Find and update stats
				arena.findPresent(player, blockLocation);

				// Spawn particles
				Random r = new Random();
				player.spawnParticle(Particle.HEART, blockLocation.add(.5, .5 , .5), 5,
						r.nextDouble() / 2.5, r.nextDouble() / 2.5, r.nextDouble() / 2.5);
				player.spawnParticle(Particle.VILLAGER_HAPPY, blockLocation, 5, r.nextDouble() / 2.5,
						r.nextDouble() / 2.5, r.nextDouble() / 2.5);
				player.playSound(blockLocation, Sound.BLOCK_NOTE_BLOCK_BELL, 2F, (float) 1.82);
			}


			// Ignore
			else return;

			// Cancel interaction
			e.setCancelled(true);
		}

		// See if the player is an editor
		if (Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).anyMatch(a -> a.hasEditor(player))) {
			Arena arena = Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull)
					.filter(a -> a.hasEditor(player)).toList().get(0);
			int currentIndex;
			try {
				currentIndex = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(
						player.getInventory().getItem(1)).getItemMeta()).getLore()).get(0).substring(2));
			} catch (Exception err) {
				return;
			}

			// Previous set of present options
			if (Objects.requireNonNull(item.getItemMeta()).getDisplayName().contains("Previous"))
				Inventories.setEditorHotbar(player, (currentIndex + 18) % InventoryItems.presents().length);

			// Allow placing presents
			else if (Objects.requireNonNull(item.getItemMeta()).getDisplayName().contains("Present"))
				return;

			// Next set of present options
			else if (Objects.requireNonNull(item.getItemMeta()).getDisplayName().contains("Next"))
				Inventories.setEditorHotbar(player, (currentIndex + 1) % InventoryItems.presents().length);

			// Leave editor mode
			else if (InventoryItems.exit().equals(item))
				arena.removeEditor(player);

			// Ignore
			else return;

			// Cancel interaction
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Player player = e.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();
		String name = Objects.requireNonNull(item.getItemMeta()).getDisplayName();

		// Check for editor
		if (Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).noneMatch(a -> a.hasEditor(player)))
			return;

		Arena arena = Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull)
				.filter(a -> a.hasEditor(player)).toList().get(0);
		float yaw = player.getEyeLocation().getYaw() + 180;
		if (yaw > 180)
			yaw -= 360;
		Location location = e.getBlock().getLocation();
		location.setYaw(yaw);

		// Add present
		arena.addPresent(PresentType.valueOf(name.substring(4, name.indexOf(" ")).toUpperCase()), location);
	}

	// Handles breaking and placing of presents
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player player = e.getPlayer();
		Location blockLocation = e.getBlock().getLocation();

		// Check for present
		if (Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull)
				.noneMatch(a -> a.checkPresent(blockLocation)))
			return;

		// Check for head
		if (e.getBlock().getType() != Material.PLAYER_HEAD) {
			Arena arena = Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull)
					.filter(a -> a.hasEditor(player)).toList().get(0);
			arena.removePresent(blockLocation);
		}

		// Check for editor
		if (Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).anyMatch(a -> a.hasEditor(player))) {
			Arena arena = Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull)
					.filter(a -> a.hasEditor(player)).toList().get(0);

			// Remove present
			if (arena.removePresent(blockLocation))
				player.sendMessage(Utils.notify("&aPresent removed!"));
		} else {
			e.setCancelled(true);
			player.sendMessage(Utils.notify("&cPresent must be removed through Arena settings!"));
		}
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

	// Prevent players from dropping standard game items or editing items
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent e) {
		Player player = e.getPlayer();
		Arena arena;

		// Attempt to get arena and player
		try {
			arena = Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).filter(a -> a.hasPlayer(player))
					.toList().get(0);
			arena.getPlayer(player);

			// Cancel event if arena is in waiting mode
			if (arena.getStatus() == ArenaStatus.WAITING)
				e.setCancelled(true);

			return;
		} catch (Exception ignored) {
		}

		// Check for editor
		if (Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).anyMatch(a -> a.hasEditor(player)))
			e.setCancelled(true);
	}

	// Prevent moving items around while waiting for game to start or in editor mode
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		Arena arena;

		// Attempt to get arena and player
		try {
			arena = Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).filter(a -> a.hasPlayer(player))
					.toList().get(0);
			arena.getPlayer(player);

			// Cancel event if arena is in waiting mode
			if (arena.getStatus() == ArenaStatus.WAITING)
				e.setCancelled(true);

			return;
		} catch (Exception ignored) {
		}

		// Check for editor
		if (Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).anyMatch(a -> a.hasEditor(player)))
			e.setCancelled(true);
	}

	// Prevent swapping items while waiting for game to start or editing
	@EventHandler
	public void onSwap(PlayerSwapHandItemsEvent e) {
		Player player = e.getPlayer();
		Arena arena;

		// Attempt to get arena and player
		try {
			arena = Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).filter(a -> a.hasPlayer(player))
					.toList().get(0);
			arena.getPlayer(player);

			// Cancel event if arena is in waiting mode
			if (arena.getStatus() == ArenaStatus.WAITING)
				e.setCancelled(true);

			return;
		} catch (Exception ignored) {
		}

		// Check for editor
		if (Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).anyMatch(a -> a.hasEditor(player)))
			e.setCancelled(true);
	}
}
