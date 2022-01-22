package me.theguyhere.grinchsimulator.listeners;

import me.theguyhere.grinchsimulator.GUI.Inventories;
import me.theguyhere.grinchsimulator.GUI.InventoryMeta;
import me.theguyhere.grinchsimulator.Main;
import me.theguyhere.grinchsimulator.events.LeaveArenaEvent;
import me.theguyhere.grinchsimulator.events.SignGUIEvent;
import me.theguyhere.grinchsimulator.game.models.Tasks;
import me.theguyhere.grinchsimulator.game.models.arenas.Arena;
import me.theguyhere.grinchsimulator.game.models.arenas.ArenaManager;
import me.theguyhere.grinchsimulator.game.models.arenas.ArenaRecordType;
import me.theguyhere.grinchsimulator.game.models.presents.PresentType;
import me.theguyhere.grinchsimulator.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;

public class InventoryListener implements Listener {
	private final Main plugin;
	private boolean close;

	// Constants for armor types
	public InventoryListener(Main plugin) {
		this.plugin = plugin;
	}

	// Prevent losing items by drag clicking in custom inventory
	@EventHandler
	public void onDrag(InventoryDragEvent e) {
		String title = e.getView().getTitle();

		// Ignore non-plugin inventories
		if (!title.contains(Utils.format("&k")))
			return;

		// Ignore clicks in player inventory
		if (e.getInventory().getType() == InventoryType.PLAYER)
			return;

		e.setCancelled(true);
	}

	// Prevent losing items by shift clicking in custom inventory
	@EventHandler
	public void onShiftClick(InventoryClickEvent e) {
		String title = e.getView().getTitle();

		// Ignore non-plugin inventories
		if (!title.contains(Utils.format("&k")))
			return;

		// Check for shift click
		if (e.getClick() != ClickType.SHIFT_LEFT && e.getClick() != ClickType.SHIFT_RIGHT)
			return;

		e.setCancelled(true);
	}

	// All click events in the inventories
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		// Get inventory title
		String title = e.getView().getTitle();

		// Ignore non-plugin inventories
		if (!title.contains(Utils.format("&k")))
			return;

		// Ignore null inventories
		if (e.getClickedInventory() == null)
			return;

		// Debugging info
		Utils.debugInfo("Inventory Item: " + e.getCurrentItem(), 2);
		Utils.debugInfo("Cursor Item: " + e.getCursor(), 2);
		Utils.debugInfo("Clicked Inventory: " + e.getClickedInventory(), 2);
		Utils.debugInfo("Inventory Name: " + title, 2);

		// Cancel the event to prevent changing the GUI
		e.setCancelled(true);

		// Ignore clicks in player inventory
		if (e.getClickedInventory().getType() == InventoryType.PLAYER)
			return;

		// Get button information
		ItemStack button = e.getCurrentItem();
		Material buttonType;
		String buttonName;

		Player player = (Player) e.getWhoClicked();
		int slot = e.getSlot();
		FileConfiguration config = plugin.getArenaData();
		FileConfiguration language = plugin.getLanguageData();

		// Ignore null items
		if (button == null)
			return;

		// Get button type and name
		buttonType = button.getType();
		buttonName = Objects.requireNonNull(button.getItemMeta()).getDisplayName();

		// Arena inventory
		if (title.contains("Grinch Simulator Arenas")) {
			// Create new arena with naming inventory
			if (buttonType == Material.RED_CONCRETE) {
				// Set a new arena
				ArenaManager.setArena(slot + 1, new Arena(plugin, slot + 1, new Tasks(plugin, slot)));

				Inventories.nameArena(player, ArenaManager.getArena(slot + 1));
			}

			// Edit existing arena
			else if (buttonType == Material.LIME_CONCRETE)
				player.openInventory(Inventories.createArenaInventory(slot + 1));

			// Open lobby menu
			else if (buttonName.contains("Lobby"))
				player.openInventory(Inventories.createLobbyInventory(plugin));

			// Open info boards menu
			else if (buttonName.contains("Info Boards"))
				player.openInventory(Inventories.createInfoBoardInventory(plugin));

			// Open leaderboards menu
			else if (buttonName.contains("Leaderboards"))
				player.openInventory(Inventories.createLeaderboardInventory());

			// Close inventory
			else if (buttonName.contains("EXIT"))
				player.closeInventory();
		}

		// Lobby menu
		else if (title.contains(Utils.format("&2&lLobby"))) {
			String path = "lobby";

			// Create lobby
			if (buttonName.contains("Create Lobby")) {
				Utils.setConfigurationLocation(plugin, path, player.getLocation());
				plugin.getArenaManager().reloadLobby();
				player.sendMessage(Utils.notify("&aLobby set!"));
				player.openInventory(Inventories.createLobbyInventory(plugin));
			}

			// Relocate lobby
			else if (buttonName.contains("Relocate Lobby")) {
				Utils.setConfigurationLocation(plugin, path, player.getLocation());
				plugin.getArenaManager().reloadLobby();
				player.sendMessage(Utils.notify("&aLobby relocated!"));
			}

			// Teleport player to lobby
			else if (buttonName.contains("Teleport")) {
				Location location = Utils.getConfigLocationNoPitch(plugin, path);
				if (location == null) {
					player.sendMessage(Utils.notify("&cNo lobby to teleport to!"));
					return;
				}
				player.teleport(location);
				player.closeInventory();
			}

			// Center lobby
			else if (buttonName.contains("Center")) {
				Location location = Utils.getConfigLocationNoRotation(plugin, path);
				if (location == null) {
					player.sendMessage(Utils.notify("&cNo lobby to center!"));
					return;
				}
				Utils.centerConfigLocation(plugin, path);
				player.sendMessage(Utils.notify("&aLobby centered!"));
			}

			// Remove lobby
			else if (buttonName.contains("REMOVE"))
				if (config.contains("lobby"))
					player.openInventory(Inventories.createLobbyConfirmInventory());
				else player.sendMessage(Utils.notify("&cNo lobby to remove!"));

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createArenasInventory());
		}

		// Info board menu
		else if (title.contains("Info Boards")) {

			// Edit board
			if (Arrays.asList(Inventories.INFO_BOARD_MATS).contains(buttonType))
				player.openInventory(Inventories.createInfoBoardMenu(plugin, slot + 1));

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createArenasInventory());
		}

		// Info board menu for a specific board
		else if (title.contains("Info Board ")) {
			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
			assert meta != null;
			int num = meta.getInteger1();
			String path = "infoBoard." + num;

			// Create board
			if (buttonName.contains("Create")) {
				plugin.getArenaManager().setInfoBoard(player.getLocation(), num);
				player.sendMessage(Utils.notify("&aInfo board set!"));
				player.openInventory(Inventories.createInfoBoardMenu(plugin, num));
			}

			// Relocate board
			else if (buttonName.contains("Relocate")) {
				Utils.setConfigurationLocation(plugin, path, player.getLocation());
				plugin.getArenaManager().refreshInfoBoard(num);
				player.sendMessage(Utils.notify("&aInfo board relocated!"));
			}

			// Teleport player to info board
			else if (buttonName.contains("Teleport")) {
				Location location = Utils.getConfigLocationNoRotation(plugin, path);
				if (location == null) {
					player.sendMessage(Utils.notify("&cNo info board to teleport to!"));
					return;
				}
				player.teleport(location);
				player.closeInventory();
			}

			// Center info board
			else if (buttonName.contains("Center")) {
				if (Utils.getConfigLocationNoRotation(plugin, path) == null) {
					player.sendMessage(Utils.notify("&cNo info board to center!"));
					return;
				}
				plugin.getArenaManager().centerInfoBoard(num);
				player.sendMessage(Utils.notify("&aInfo board centered!"));
			}

			// Remove info board
			else if (buttonName.contains("REMOVE"))
				if (config.contains(path))
					player.openInventory(Inventories.createInfoBoardConfirmInventory(num));
				else player.sendMessage(Utils.notify("&cNo info board to remove!"));

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createInfoBoardInventory(plugin));
		}

		// Leaderboard menu
		else if (title.contains("Leaderboards")) {
			if (buttonName.contains("Total Presents Leaderboard"))
				player.openInventory(Inventories.createTotalPresentsLeaderboardInventory(plugin));

			if (buttonName.contains("Top Presents Leaderboard"))
				player.openInventory(Inventories.createTopPresentsLeaderboardInventory(plugin));

			if (buttonName.contains("Total Happiness Leaderboard"))
				player.openInventory(Inventories.createTotalHappinessLeaderboardInventory(plugin));

			if (buttonName.contains("Top Happiness Leaderboard"))
				player.openInventory(Inventories.createTopHappinessLeaderboardInventory(plugin));

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createArenasInventory());
		}

		// Total presents leaderboard menu
		else if (title.contains(Utils.format("&4&lTotal Presents Leaderboard"))) {
			String path = "leaderboard.totalPresents";

			// Create leaderboard
			if (buttonName.contains("Create")) {
				plugin.getArenaManager().setLeaderboard(player.getLocation(), "totalPresents");
				player.sendMessage(Utils.notify("&aLeaderboard set!"));
				player.openInventory(Inventories.createTotalPresentsLeaderboardInventory(plugin));
			}

			// Relocate leaderboard
			else if (buttonName.contains("Relocate")) {
				plugin.getArenaManager().setLeaderboard(player.getLocation(), "totalPresents");
				player.sendMessage(Utils.notify("&aLeaderboard relocated!"));
			}

			// Teleport player to leaderboard
			else if (buttonName.contains("Teleport")) {
				Location location = Utils.getConfigLocationNoRotation(plugin, path);
				if (location == null) {
					player.sendMessage(Utils.notify("&cNo leaderboard to teleport to!"));
					return;
				}
				player.teleport(location);
				player.closeInventory();
			}

			// Center leaderboard
			else if (buttonName.contains("Center")) {
				Location location = Utils.getConfigLocationNoRotation(plugin, path);
				if (location == null) {
					player.sendMessage(Utils.notify("&cNo leaderboard to center!"));
					return;
				}
				plugin.getArenaManager().centerLeaderboard("totalPresents");
				player.sendMessage(Utils.notify("&aLeaderboard centered!"));
			}

			// Remove leaderboard
			else if (buttonName.contains("REMOVE"))
				if (config.contains(path))
					player.openInventory(Inventories.createTotalPresentsConfirmInventory());
				else player.sendMessage(Utils.notify("&cNo leaderboard to remove!"));

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createLeaderboardInventory());
		}

		// Top presents leaderboard menu
		else if (title.contains(Utils.format("&c&lTop Presents Leaderboard"))) {
			String path = "leaderboard.topPresents";

			// Create leaderboard
			if (buttonName.contains("Create")) {
				plugin.getArenaManager().setLeaderboard(player.getLocation(), "topPresents");
				player.sendMessage(Utils.notify("&aLeaderboard set!"));
				player.openInventory(Inventories.createTopPresentsLeaderboardInventory(plugin));
			}

			// Relocate leaderboard
			else if (buttonName.contains("Relocate")) {
				plugin.getArenaManager().setLeaderboard(player.getLocation(), "topPresents");
				player.sendMessage(Utils.notify("&aLeaderboard relocated!"));
			}

			// Teleport player to leaderboard
			else if (buttonName.contains("Teleport")) {
				Location location = Utils.getConfigLocationNoRotation(plugin, path);
				if (location == null) {
					player.sendMessage(Utils.notify("&cNo leaderboard to teleport to!"));
					return;
				}
				player.teleport(location);
				player.closeInventory();
			}

			// Center leaderboard
			else if (buttonName.contains("Center")) {
				Location location = Utils.getConfigLocationNoRotation(plugin, path);
				if (location == null) {
					player.sendMessage(Utils.notify("&cNo leaderboard to center!"));
					return;
				}
				plugin.getArenaManager().refreshLeaderboard("topPresents");
				player.sendMessage(Utils.notify("&aLeaderboard centered!"));
			}

			// Remove leaderboard
			else if (buttonName.contains("REMOVE"))
				if (config.contains(path))
					player.openInventory(Inventories.createTopPresentsConfirmInventory());
				else player.sendMessage(Utils.notify("&cNo leaderboard to remove!"));

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createLeaderboardInventory());
		}

		// Total happiness leaderboard menu
		else if (title.contains(Utils.format("&5&lTotal Happiness Leaderboard"))) {
			String path = "leaderboard.totalHappiness";

			// Create leaderboard
			if (buttonName.contains("Create")) {
				plugin.getArenaManager().setLeaderboard(player.getLocation(), "totalHappiness");
				player.sendMessage(Utils.notify("&aLeaderboard set!"));
				player.openInventory(Inventories.createTotalHappinessLeaderboardInventory(plugin));
			}

			// Relocate leaderboard
			else if (buttonName.contains("Relocate")) {
				plugin.getArenaManager().setLeaderboard(player.getLocation(), "totalHappiness");
				player.sendMessage(Utils.notify("&aLeaderboard relocated!"));
			}

			// Teleport player to leaderboard
			else if (buttonName.contains("Teleport")) {
				Location location = Utils.getConfigLocationNoRotation(plugin, path);
				if (location == null) {
					player.sendMessage(Utils.notify("&cNo leaderboard to teleport to!"));
					return;
				}
				player.teleport(location);
				player.closeInventory();
			}

			// Center leaderboard
			else if (buttonName.contains("Center")) {
				Location location = Utils.getConfigLocationNoRotation(plugin, path);
				if (location == null) {
					player.sendMessage(Utils.notify("&cNo leaderboard to center!"));
					return;
				}
				plugin.getArenaManager().refreshLeaderboard("totalHappiness");
				player.sendMessage(Utils.notify("&aLeaderboard centered!"));
			}

			// Remove leaderboard
			else if (buttonName.contains("REMOVE"))
				if (config.contains(path))
					player.openInventory(Inventories.createTotalHappinessConfirmInventory());
				else player.sendMessage(Utils.notify("&cNo leaderboard to remove!"));

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createLeaderboardInventory());
		}

		// Top happiness leaderboard menu
		else if (title.contains(Utils.format("&d&lTop Happiness Leaderboard"))) {
			String path = "leaderboard.topHappiness";

			// Create leaderboard
			if (buttonName.contains("Create")) {
				plugin.getArenaManager().setLeaderboard(player.getLocation(), "topHappiness");
				player.sendMessage(Utils.notify("&aLeaderboard set!"));
				player.openInventory(Inventories.createTopHappinessLeaderboardInventory(plugin));
			}

			// Relocate leaderboard
			else if (buttonName.contains("Relocate")) {
				plugin.getArenaManager().setLeaderboard(player.getLocation(), "topHappiness");
				player.sendMessage(Utils.notify("&aLeaderboard relocated!"));
			}

			// Teleport player to leaderboard
			else if (buttonName.contains("Teleport")) {
				Location location = Utils.getConfigLocationNoRotation(plugin, path);
				if (location == null) {
					player.sendMessage(Utils.notify("&cNo leaderboard to teleport to!"));
					return;
				}
				player.teleport(location);
				player.closeInventory();
			}

			// Center leaderboard
			else if (buttonName.contains("Center")) {
				Location location = Utils.getConfigLocationNoRotation(plugin, path);
				if (location == null) {
					player.sendMessage(Utils.notify("&cNo leaderboard to center!"));
					return;
				}
				plugin.getArenaManager().refreshLeaderboard("topHappiness");
				player.sendMessage(Utils.notify("&aLeaderboard centered!"));
			}

			// Remove leaderboard
			else if (buttonName.contains("REMOVE"))
				if (config.contains(path))
					player.openInventory(Inventories.createTopHappinessConfirmInventory());
				else player.sendMessage(Utils.notify("&cNo leaderboard to remove!"));

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createLeaderboardInventory());
		}

		// Menu for an arena
		else if (title.contains(Utils.format("&2&lEdit "))) {
			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
			assert meta != null;
			Arena arenaInstance = ArenaManager.getArena(meta.getInteger1());

			// Open name editor
			if (buttonName.contains("Edit Name"))
				if (arenaInstance.isClosed())
					Inventories.nameArena(player, arenaInstance);
				else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));

			// Open portal menu
			else if (buttonName.contains("Portal and Leaderboard"))
				player.openInventory(Inventories.createPortalInventory(meta.getInteger1()));

			// Open player menu
			else if (buttonName.contains("Player Settings"))
				player.openInventory(Inventories.createPlayersInventory(meta.getInteger1()));

			// Open presents menu
			else if (buttonName.contains("Present Settings"))
				player.openInventory(Inventories.createPresentSettingsInventory(meta.getInteger1()));

			// Open game settings menu
			else if (buttonName.contains("Game Settings"))
				player.openInventory(Inventories.createGameSettingsInventory(meta.getInteger1()));

			// Toggle arena close
			else if (buttonName.contains("Close")) {
				// Arena currently closed
				if (arenaInstance.isClosed()) {
					// No lobby
					if (!config.contains("lobby")) {
						player.sendMessage(Utils.notify("&cArena cannot open without a lobby!"));
						return;
					}

					// No arena portal
					if (arenaInstance.getPortalLocation() == null) {
						player.sendMessage(Utils.notify("&cArena cannot open without a portal!"));
						return;
					}

					// No player spawn
					if (arenaInstance.getPlayerSpawn() == null) {
						player.sendMessage(Utils.notify("&cArena cannot open without a player spawn!"));
						return;
					}

					// Open arena
					arenaInstance.setClosed(false);
				}

				// Arena currently open
				else {
					// Set to closed and update portal
					arenaInstance.setClosed(true);

					// Kick players
					arenaInstance.getPlayers().forEach(gPlayer ->
							Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
									Bukkit.getPluginManager().callEvent(new LeaveArenaEvent(gPlayer.getPlayer()))));
				}

				// Save perm data
				player.openInventory(Inventories.createArenaInventory(meta.getInteger1()));
			}

			// Open arena remove confirmation menu
			else if (buttonName.contains("REMOVE"))
				if (arenaInstance.isClosed())
					player.openInventory(Inventories.createArenaConfirmInventory(meta.getInteger1()));
				else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));

			// Return to arenas menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createArenasInventory());
		}

		// Confirmation menus
		else if (title.contains("Remove") || title.contains("Reset")) {
			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
			assert meta != null;

			// Confirm to remove portal
			if (title.contains("Remove Portal?")) {
				// Return to previous menu
				if (buttonName.contains("NO"))
					player.openInventory(Inventories.createPortalInventory(meta.getInteger1()));

				// Remove the portal, then return to previous menu
				else if (buttonName.contains("YES")) {
					// Remove portal, close arena
					ArenaManager.getArena(meta.getInteger1()).removePortal();

					// Confirm and return
					player.sendMessage(Utils.notify("&aPortal removed!"));
					player.openInventory(Inventories.createPortalInventory(meta.getInteger1()));
				}
			}

			// Confirm to remove present arena leaderboard
			else if (title.contains("Remove Present LBoard?")) {
				// Return to previous menu
				if (buttonName.contains("NO"))
					player.openInventory(Inventories.createPresentLeaderInventory(meta.getInteger1()));

				// Remove the arena board, then return to previous menu
				else if (buttonName.contains("YES")) {
					// Delete arena leaderboard
					ArenaManager.getArena(meta.getInteger1()).removeArenaBoard(ArenaRecordType.PRESENTS);

					// Confirm and return
					player.sendMessage(Utils.notify("&aLeaderboard removed!"));
					player.openInventory(Inventories.createPresentLeaderInventory(meta.getInteger1()));
				}
			}

			// Confirm to remove happiness arena leaderboard
			else if (title.contains("Remove Happiness LBoard?")) {
				// Return to previous menu
				if (buttonName.contains("NO"))
					player.openInventory(Inventories.createHappyLeaderInventory(meta.getInteger1()));

				// Remove the arena board, then return to previous menu
				else if (buttonName.contains("YES")) {
					// Delete arena leaderboard
					ArenaManager.getArena(meta.getInteger1()).removeArenaBoard(ArenaRecordType.HAPPINESS);

					// Confirm and return
					player.sendMessage(Utils.notify("&aLeaderboard removed!"));
					player.openInventory(Inventories.createHappyLeaderInventory(meta.getInteger1()));
				}
			}

			// Confirm to remove player spawn
			else if (title.contains("Remove Spawn?")) {
				// Return to previous menu
				if (buttonName.contains("NO"))
					player.openInventory(Inventories.createPlayerSpawnInventory(meta.getInteger1()));

				// Remove spawn, then return to previous menu
				else if (buttonName.contains("YES")) {
					Arena arenaInstance = ArenaManager.getArena(meta.getInteger1());

					arenaInstance.setPlayerSpawn(null);
					arenaInstance.setClosed(true);
					player.sendMessage(Utils.notify("&aSpawn removed!"));
					player.openInventory(Inventories.createPlayerSpawnInventory(meta.getInteger1()));
				}
			}

			// Confirm to remove waiting room
			else if (title.contains("Remove Waiting Room?")) {
				// Return to previous menu
				if (buttonName.contains("NO"))
					player.openInventory(Inventories.createWaitingRoomInventory(meta.getInteger1()));

				// Remove spawn, then return to previous menu
				else if (buttonName.contains("YES")) {
					Arena arenaInstance = ArenaManager.getArena(meta.getInteger1());

					arenaInstance.setWaitingRoom(null);
					player.sendMessage(Utils.notify("&aWaiting room removed!"));
					player.openInventory(Inventories.createWaitingRoomInventory(meta.getInteger1()));
				}
			}

			// Confirm to remove lobby
			else if (title.contains("Remove Lobby?")) {
				// Return to previous menu
				if (buttonName.contains("NO"))
					player.openInventory(Inventories.createLobbyInventory(plugin));

				// Remove the lobby, then return to previous menu
				else if (buttonName.contains("YES")) {
					config.set("lobby", null);
					plugin.saveArenaData();
					plugin.getArenaManager().reloadLobby();
					player.sendMessage(Utils.notify("&aLobby removed!"));
					player.openInventory(Inventories.createLobbyInventory(plugin));
				}
			}

			// Confirm to remove info board
			else if (title.contains("Remove Info Board?")) {
				String path = "infoBoard." + meta.getInteger1();

				// Return to previous menu
				if (buttonName.contains("NO"))
					player.openInventory(Inventories.createInfoBoardMenu(plugin, meta.getInteger1()));

					// Remove the info board, then return to previous menu
				else if (buttonName.contains("YES")) {
					// Remove info board data
					config.set(path, null);
					plugin.saveArenaData();

					// Remove info board
					plugin.getArenaManager().removeInfoBoard(meta.getInteger1());

					// Confirm and return
					player.sendMessage(Utils.notify("&aInfo board removed!"));
					player.openInventory(Inventories.createInfoBoardMenu(plugin, meta.getInteger1()));
				}
			}

			// Confirm to remove total presents leaderboard
			else if (title.contains("Remove Total Presents Leaderboard?")) {
				String path = "leaderboard.totalPresents";

				// Return to previous menu
				if (buttonName.contains("NO"))
					player.openInventory(Inventories.createTotalPresentsLeaderboardInventory(plugin));

				// Remove the leaderboard, then return to previous menu
				else if (buttonName.contains("YES")) {
					// Remove leaderboard data
					config.set(path, null);
					plugin.saveArenaData();

					// Remove leaderboard
					plugin.getArenaManager().removeLeaderboard("totalPresents");

					// Confirm and return
					player.sendMessage(Utils.notify("&aLeaderboard removed!"));
					player.openInventory(Inventories.createTotalPresentsLeaderboardInventory(plugin));
				}
			}

			// Confirm to remove top presents leaderboard
			else if (title.contains("Remove Top Presents Leaderboard?")) {
				String path = "leaderboard.topPresents";

				// Return to previous menu
				if (buttonName.contains("NO"))
					player.openInventory(Inventories.createTopPresentsLeaderboardInventory(plugin));

				// Remove the leaderboard, then return to previous menu
				else if (buttonName.contains("YES")) {
					// Remove leaderboard data
					config.set(path, null);
					plugin.saveArenaData();

					// Remove leaderboard
					plugin.getArenaManager().removeLeaderboard("topPresents");

					// Confirm and return
					player.sendMessage(Utils.notify("&aLeaderboard removed!"));
					player.openInventory(Inventories.createTopPresentsLeaderboardInventory(plugin));
				}
			}

			// Confirm to remove total happiness leaderboard
			else if (title.contains("Remove Total Happiness Leaderboard?")) {
				String path = "leaderboard.totalHappiness";

				// Return to previous menu
				if (buttonName.contains("NO"))
					player.openInventory(Inventories.createTotalHappinessLeaderboardInventory(plugin));

				// Remove the leaderboard, then return to previous menu
				else if (buttonName.contains("YES")) {
					// Remove leaderboard data
					config.set(path, null);
					plugin.saveArenaData();

					// Remove leaderboard
					plugin.getArenaManager().removeLeaderboard("totalHappiness");

					// Confirm and return
					player.sendMessage(Utils.notify("&aLeaderboard removed!"));
					player.openInventory(Inventories.createTotalHappinessLeaderboardInventory(plugin));
				}
			}

			// Confirm to remove top happiness leaderboard
			else if (title.contains("Remove Top Happiness Leaderboard?")) {
				String path = "leaderboard.topHappiness";

				// Return to previous menu
				if (buttonName.contains("NO"))
					player.openInventory(Inventories.createTopHappinessLeaderboardInventory(plugin));

				// Remove the leaderboard, then return to previous menu
				else if (buttonName.contains("YES")) {
					// Remove leaderboard data
					config.set(path, null);
					plugin.saveArenaData();

					// Remove leaderboard
					plugin.getArenaManager().removeLeaderboard("topHappiness");

					// Confirm and return
					player.sendMessage(Utils.notify("&aLeaderboard removed!"));
					player.openInventory(Inventories.createTopHappinessLeaderboardInventory(plugin));
				}
			}

			// Confirm to delete all presents
			else if (title.contains("Remove All Presents?")) {
				// Return to previous menu
				if (buttonName.contains("NO"))
					player.openInventory(Inventories.createPresentDeleteInventory(meta.getInteger1()));

				// Delete all presents, then return to previous menu
				else if (buttonName.contains("YES")) {
					// Remove all presents
					ArenaManager.getArena(meta.getInteger1()).removeAllPresents();

					// Confirm and return
					player.sendMessage(Utils.notify("&aAll presents removed!"));
					player.openInventory(Inventories.createPresentDeleteInventory(meta.getInteger1()));
				}
			}

			// Confirm to delete all of a specific type of present
			else if (title.contains("Remove ") && title.contains(" Presents?")) {
				// Return to previous menu
				if (buttonName.contains("NO"))
					player.openInventory(Inventories.createPresentDeleteInventory(meta.getInteger1()));

				// Delete the presents, then return to previous menu
				else if (buttonName.contains("YES")) {
					// Delete the presents
					ArenaManager.getArena(meta.getInteger1()).removePresents(PresentType.valueOf(meta.getString()));

					// Confirm and return
					player.sendMessage(Utils.notify("&a" + meta.getString().charAt(0) +
							meta.getString().substring(1).toLowerCase() + " presents removed!"));
					player.openInventory(Inventories.createPresentDeleteInventory(meta.getInteger1()));
				}
			}

			// Confirm to reset all present values
			else if (title.contains("Reset All Presents?")) {
				// Return to previous menu
				if (buttonName.contains("NO"))
					player.openInventory(Inventories.createPresentValueInventory(meta.getInteger1()));

				// Delete all presents, then return to previous menu
				else if (buttonName.contains("YES")) {
					// Reset all present values
					ArenaManager.getArena(meta.getInteger1()).resetPresentValues();

					// Confirm and return
					player.sendMessage(Utils.notify("&aAll presents values reset!"));
					player.openInventory(Inventories.createPresentValueInventory(meta.getInteger1()));
				}
			}

			// Confirm to remove arena
			else {
				// Return to previous menu
				if (buttonName.contains("NO"))
					player.openInventory(Inventories.createArenaInventory(meta.getInteger1()));

				// Remove arena data, then return to previous menu
				else if (buttonName.contains("YES")) {
					Arena arenaInstance = ArenaManager.getArena(meta.getInteger1());

					// Remove data
					arenaInstance.remove();
					ArenaManager.setArena(meta.getInteger1(), null);

					// Confirm and return
					player.sendMessage(Utils.notify("&aArena removed!"));
					player.openInventory(Inventories.createArenasInventory());
				}
			}
		}

		// Portal and leaderboard menu for an arena
		else if (title.contains("Portal/LBoard:")) {
			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
			assert meta != null;
			Arena arenaInstance = ArenaManager.getArena(meta.getInteger1());

			// Create portal
			if (buttonName.contains("Create Portal"))
				if (arenaInstance.isClosed()) {
					arenaInstance.setPortal(player.getLocation());
					player.sendMessage(Utils.notify("&aPortal set!"));
					player.openInventory(Inventories.createPortalInventory(meta.getInteger1()));
				} else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));

			// Relocate portal
			else if (buttonName.contains("Relocate Portal"))
				if (arenaInstance.isClosed()) {
					arenaInstance.setPortal(player.getLocation());
					player.sendMessage(Utils.notify("&aPortal relocated!"));
				} else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));

			// Teleport player to portal
			else if (buttonName.contains("Teleport to Portal")) {
				Location location = arenaInstance.getPortalLocation();
				if (location == null) {
					player.sendMessage(Utils.notify("&cNo portal to teleport to!"));
					return;
				}
				player.teleport(location);
				player.closeInventory();
			}

			// Center portal
			else if (buttonName.contains("Center Portal")) {
				if (arenaInstance.isClosed()) {
					if (arenaInstance.getPortal() == null) {
						player.sendMessage(Utils.notify("&cNo portal to center!"));
						return;
					}
					arenaInstance.centerPortal();
					player.sendMessage(Utils.notify("&aPortal centered!"));
				} else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
			}

			// Remove portal
			else if (buttonName.contains("REMOVE PORTAL"))
				if (arenaInstance.getPortal() != null)
					if (arenaInstance.isClosed())
						player.openInventory(Inventories.createPortalConfirmInventory(meta.getInteger1()));
					else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
				else player.sendMessage(Utils.notify("&cNo portal to remove!"));

			// Edit present leaderboard
			else if (buttonName.contains("Present Leaderboard"))
				player.openInventory(Inventories.createPresentLeaderInventory(meta.getInteger1()));

			// Edit happiness leaderboard
			else if (buttonName.contains("Happiness Leaderboard"))
				player.openInventory(Inventories.createHappyLeaderInventory(meta.getInteger1()));

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createArenaInventory(meta.getInteger1()));
		}

		// Present leaderboard menu for an arena
		else if (title.contains("Present LBoard:")) {
			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
			assert meta != null;
			Arena arenaInstance = ArenaManager.getArena(meta.getInteger1());

			// Create portal
			if (buttonName.contains("Create Leaderboard")) {
					arenaInstance.setArenaBoard(player.getLocation(), ArenaRecordType.PRESENTS);
					player.sendMessage(Utils.notify("&aLeaderboard set!"));
					player.openInventory(Inventories.createPresentLeaderInventory(meta.getInteger1()));
			}

			// Relocate portal
			else if (buttonName.contains("Relocate Leaderboard")) {
					arenaInstance.setArenaBoard(player.getLocation(), ArenaRecordType.PRESENTS);
					player.sendMessage(Utils.notify("&aLeaderboard relocated!"));
			}

			// Teleport player to portal
			else if (buttonName.contains("Teleport to Leaderboard")) {
				Location location = arenaInstance.getPresentLeadersLocation();
				if (location == null) {
					player.sendMessage(Utils.notify("&cNo leaderboard to teleport to!"));
					return;
				}
				player.teleport(location);
				player.closeInventory();
			}

			// Center portal
			else if (buttonName.contains("Center Leaderboard")) {
				if (arenaInstance.getPresentLeaders() == null) {
					player.sendMessage(Utils.notify("&cNo leaderboard to center!"));
					return;
				}
				arenaInstance.centerArenaBoard(ArenaRecordType.PRESENTS);
				player.sendMessage(Utils.notify("&aLeaderboard centered!"));
			}

			// Remove portal
			else if (buttonName.contains("REMOVE LEADERBOARD"))
				if (arenaInstance.getPresentLeaders() != null)
					player.openInventory(Inventories.createPresentLeadersConfirmInventory(meta.getInteger1()));
				else player.sendMessage(Utils.notify("&cNo leaderboard to remove!"));

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createPortalInventory(meta.getInteger1()));
		}

		// Happiness leaderboard menu for an arena
		else if (title.contains("Happiness LBoard:")) {
			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
			assert meta != null;
			Arena arenaInstance = ArenaManager.getArena(meta.getInteger1());

			// Create portal
			if (buttonName.contains("Create Leaderboard")) {
					arenaInstance.setArenaBoard(player.getLocation(), ArenaRecordType.HAPPINESS);
					player.sendMessage(Utils.notify("&aLeaderboard set!"));
					player.openInventory(Inventories.createHappyLeaderInventory(meta.getInteger1()));
			}

			// Relocate portal
			else if (buttonName.contains("Relocate Leaderboard")) {
					arenaInstance.setArenaBoard(player.getLocation(), ArenaRecordType.HAPPINESS);
					player.sendMessage(Utils.notify("&aLeaderboard relocated!"));
			}

			// Teleport player to portal
			else if (buttonName.contains("Teleport to Leaderboard")) {
				Location location = arenaInstance.getHappyLeadersLocation();
				if (location == null) {
					player.sendMessage(Utils.notify("&cNo leaderboard to teleport to!"));
					return;
				}
				player.teleport(location);
				player.closeInventory();
			}

			// Center portal
			else if (buttonName.contains("Center Leaderboard")) {
				if (arenaInstance.getHappyLeaders() == null) {
					player.sendMessage(Utils.notify("&cNo leaderboard to center!"));
					return;
				}
				arenaInstance.centerArenaBoard(ArenaRecordType.HAPPINESS);
				player.sendMessage(Utils.notify("&aLeaderboard centered!"));
			}

			// Remove portal
			else if (buttonName.contains("REMOVE LEADERBOARD"))
				if (arenaInstance.getHappyLeaders() != null)
					player.openInventory(Inventories.createHappyLeadersConfirmInventory(meta.getInteger1()));
				else player.sendMessage(Utils.notify("&cNo leaderboard to remove!"));

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createPortalInventory(meta.getInteger1()));
		}

		// Player settings menu for an arena
		else if (title.contains("Player Settings:")) {
			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
			assert meta != null;
			Arena arenaInstance = ArenaManager.getArena(meta.getInteger1());

			// Open player spawn editor
			if (buttonName.contains("Player Spawn"))
				player.openInventory(Inventories.createPlayerSpawnInventory(meta.getInteger1()));

			// Open waiting room editor
			else if (buttonName.contains("Waiting"))
				player.openInventory(Inventories.createWaitingRoomInventory(meta.getInteger1()));

			// Edit max players
			else if (buttonName.contains("Maximum"))
				if (arenaInstance.isClosed())
					player.openInventory(Inventories.createMaxPlayerInventory(meta.getInteger1()));
				else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));

			// Edit min players
			else if (buttonName.contains("Minimum"))
				if (arenaInstance.isClosed())
					player.openInventory(Inventories.createMinPlayerInventory(meta.getInteger1()));
				else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createArenaInventory(meta.getInteger1()));
		}

		// Player spawn menu for an arena
		else if (title.contains("Player Spawn:")) {
			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
			assert meta != null;
			Arena arenaInstance = ArenaManager.getArena(meta.getInteger1());

			// Create spawn
			if (buttonName.contains("Create"))
				if (arenaInstance.isClosed()) {
					arenaInstance.setPlayerSpawn(player.getLocation());
					player.sendMessage(Utils.notify("&aSpawn set!"));
					player.openInventory(Inventories.createPlayerSpawnInventory(meta.getInteger1()));
				} else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));

			// Create spawn
			if (buttonName.contains("Relocate"))
				if (arenaInstance.isClosed()) {
					arenaInstance.setPlayerSpawn(player.getLocation());
					player.sendMessage(Utils.notify("&aSpawn relocated!"));
				} else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));

			// Teleport player to spawn
			else if (buttonName.contains("Teleport")) {
				try {
					player.teleport(arenaInstance.getPlayerSpawn());
					player.closeInventory();
				} catch (NullPointerException err) {
					player.sendMessage(Utils.notify("&cNo player spawn to teleport to!"));
				}
			}

			// Center player spawn
			else if (buttonName.contains("Center"))
				if (arenaInstance.isClosed()) {
					if (arenaInstance.getPlayerSpawn() != null) {
						arenaInstance.centerPlayerSpawn();
						player.sendMessage(Utils.notify("&aSpawn centered!"));
					} else player.sendMessage(Utils.notify("&cNo player spawn to center!"));
				} else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));

			// Remove spawn
			else if (buttonName.contains("REMOVE"))
				if (arenaInstance.getPlayerSpawn() != null) {
					if (arenaInstance.isClosed())
						player.openInventory(Inventories.createSpawnConfirmInventory(meta.getInteger1()));
					else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
				} else player.sendMessage(Utils.notify("&cNo player spawn to remove!"));

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createPlayersInventory(meta.getInteger1()));
		}

		// Waiting room menu for an arena
		else if (title.contains("Waiting Room:")) {
			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
			assert meta != null;
			Arena arenaInstance = ArenaManager.getArena(meta.getInteger1());

			// Create waiting room
			if (buttonName.contains("Create")) {
				if (arenaInstance.isClosed()) {
					arenaInstance.setWaitingRoom(player.getLocation());
					player.sendMessage(Utils.notify("&aWaiting room set!"));
					player.openInventory(Inventories.createWaitingRoomInventory(meta.getInteger1()));
				} else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
			}

			// Relocate waiting room
			if (buttonName.contains("Relocate")) {
				if (arenaInstance.isClosed()) {
					arenaInstance.setWaitingRoom(player.getLocation());
					player.sendMessage(Utils.notify("&aWaiting room relocated!"));
				} else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
			}

			// Teleport player to waiting room
			else if (buttonName.contains("Teleport")) {
				Location location = arenaInstance.getWaitingRoom();
				if (location == null) {
					player.sendMessage(Utils.notify("&cNo waiting room to teleport to!"));
					return;
				}
				player.teleport(location);
				player.closeInventory();
			}

			// Center waiting room
			else if (buttonName.contains("Center"))
				if (arenaInstance.isClosed()) {
					if (arenaInstance.getWaitingRoom() == null) {
						player.sendMessage(Utils.notify("&cNo waiting room to center!"));
						return;
					}
					arenaInstance.centerWaitingRoom();
					player.sendMessage(Utils.notify("&aWaiting room centered!"));
				} else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));

			// Remove waiting room
			else if (buttonName.contains("REMOVE"))
				if (arenaInstance.getWaitingRoom() != null)
					if (arenaInstance.isClosed())
						player.openInventory(Inventories.createWaitingConfirmInventory(meta.getInteger1()));
					else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
				else player.sendMessage(Utils.notify("&cNo waiting room to remove!"));

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createPlayersInventory(meta.getInteger1()));
		}

		// Max player menu for an arena
		else if (title.contains("Maximum Players:")) {
			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
			assert meta != null;
			Arena arenaInstance = ArenaManager.getArena(meta.getInteger1());
			int current = arenaInstance.getMaxPlayers();
			
			// Decrease max players
			if (buttonName.contains("Decrease")) {
				// Check for arena closure
				if (!arenaInstance.isClosed()) {
					player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
					return;
				}

				// Check if max players is greater than 1
				if (current <= 1) {
					player.sendMessage(Utils.notify("&cMax players cannot be less than 1!"));
					return;
				}

				// Check if max players is greater than min players
				if (current <= arenaInstance.getMinPlayers()) {
					player.sendMessage(Utils.notify("&cMax players cannot be less than min player!"));
					return;
				}

				arenaInstance.setMaxPlayers(--current);
				player.openInventory(Inventories.createMaxPlayerInventory(meta.getInteger1()));
			}

			// Increase max players
			else if (buttonName.contains("Increase")) {
				// Check for arena closure
				if (!arenaInstance.isClosed()) {
					player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
					return;
				}

				arenaInstance.setMaxPlayers(++current);
				player.openInventory(Inventories.createMaxPlayerInventory(meta.getInteger1()));
			}

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createPlayersInventory(meta.getInteger1()));
		}

		// Min player menu for an arena
		else if (title.contains("Minimum Players:")) {
			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
			assert meta != null;
			Arena arenaInstance = ArenaManager.getArena(meta.getInteger1());
			int current = arenaInstance.getMinPlayers();

			// Decrease min players
			if (buttonName.contains("Decrease")) {
				// Check for arena closure
				if (!arenaInstance.isClosed()) {
					player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
					return;
				}

				// Check if min players is greater than 1
				if (current <= 1) {
					player.sendMessage(Utils.notify("&cMin players cannot be less than 1!"));
					return;
				}

				arenaInstance.setMinPlayers(--current);
				player.openInventory(Inventories.createMinPlayerInventory(meta.getInteger1()));
			}

			// Increase min players
			else if (buttonName.contains("Increase")) {
				// Check for arena closure
				if (!arenaInstance.isClosed()) {
					player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
					return;
				}

				// Check if min players is less than max players
				if (current >= arenaInstance.getMaxPlayers()) {
					player.sendMessage(Utils.notify("&cMin players cannot be greater than max player!"));
					return;
				}

				arenaInstance.setMinPlayers(++current);
				player.openInventory(Inventories.createMinPlayerInventory(meta.getInteger1()));
			}

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createPlayersInventory(meta.getInteger1()));
		}

		// Present settings menu for an arena
		else if (title.contains("Present Settings:")) {
			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
			assert meta != null;
			Arena arenaInstance = ArenaManager.getArena(meta.getInteger1());

			// Edit presents
			if (buttonName.contains("Add/Remove Presents")) {
				// Check for arena closure
				if (!arenaInstance.isClosed()) {
					player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
					return;
				}

				arenaInstance.addEditor(player);
				player.closeInventory();
			}

			// Delete presents by type
			else if (buttonName.contains("Delete Presents by Type")) {
				// Check for arena closure
				if (!arenaInstance.isClosed()) {
					player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
					return;
				}

				player.openInventory(Inventories.createPresentDeleteInventory(meta.getInteger1()));
			}

			// Edit present values
			else if (buttonName.contains("Present Values"))
				player.openInventory(Inventories.createPresentValueInventory(meta.getInteger1()));

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createArenaInventory(meta.getInteger1()));
		}

		// Delete presents for an arena
		else if (title.contains("Delete Presents:")) {
			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
			assert meta != null;

			// Delete all instances of a specific present type
			if (buttonName.contains("Presents") && !buttonName.contains("All"))
				player.openInventory(Inventories.createPresentDeleteConfirmInventory(meta.getInteger1(),
						PresentType.valueOf(buttonName.substring(4, buttonName.indexOf(" ")).toUpperCase())));

			// Delete all presents
			else if (buttonName.contains("All Presents"))
				player.openInventory(Inventories.createPresentsDeleteConfirmInventory(meta.getInteger1()));

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createPresentSettingsInventory(meta.getInteger1()));
		}

		// Present value setting menu for an arena
		else if (title.contains("Present Values:")) {
			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
			assert meta != null;
			Arena arenaInstance = ArenaManager.getArena(meta.getInteger1());

			// Edit value of a specific present type
			if (buttonName.contains("Presents"))
				player.openInventory(Inventories.createPresentValueMenu(meta.getInteger1(),
						PresentType.valueOf(buttonName.substring(4, buttonName.indexOf(" ")).toUpperCase())));

			// Reset all values
			else if (buttonName.contains("Reset Values")) {
				// Check for arena closure
				if (!arenaInstance.isClosed()) {
					player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
					return;
				}

				player.openInventory(Inventories.createPresentsResetConfirmInventory(meta.getInteger1()));
			}

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createPresentSettingsInventory(meta.getInteger1()));
		}

		// Present value menu for an arena
		else if (title.contains("Present Value: ")) {
			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
			assert meta != null;
			Arena arenaInstance = ArenaManager.getArena(meta.getInteger1());
			PresentType type = PresentType.valueOf(meta.getString());
			int current = arenaInstance.getPresentValue(type);

			// Decrease value
			if (buttonName.contains("Decrease")) {
				// Check for arena closure
				if (!arenaInstance.isClosed()) {
					player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
					return;
				}

				// Check if present value is greater than 1
				if (current <= 1) {
					player.sendMessage(Utils.notify("&cPresent value cannot be less than 1!"));
					return;
				}

				arenaInstance.setPresentValue(type, --current);
				player.openInventory(Inventories.createPresentValueMenu(meta.getInteger1(), type));
			}

			// Increase value
			else if (buttonName.contains("Increase")) {
				// Check for arena closure
				if (!arenaInstance.isClosed()) {
					player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
					return;
				}

				arenaInstance.setPresentValue(type, ++current);
				player.openInventory(Inventories.createPresentValueMenu(meta.getInteger1(), type));
			}

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createPresentValueInventory(meta.getInteger1()));
		}

		// Game settings menu for an arena
		else if (title.contains("Game Settings:")) {
			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
			assert meta != null;
			Arena arenaInstance = ArenaManager.getArena(meta.getInteger1());

			// Change wave time limit
			if (buttonName.contains("Wave Time Limit"))
				if (arenaInstance.isClosed())
					player.openInventory(Inventories.createWaveTimeLimitInventory(meta.getInteger1()));
				else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));

			// Edit sounds
			else if (buttonName.contains("Sounds"))
				player.openInventory(Inventories.createSoundsInventory(meta.getInteger1()));

			// Copy game settings from another arena or a preset
			else if (buttonName.contains("Copy Game Settings"))
				if (arenaInstance.isClosed())
					player.openInventory(Inventories.createCopySettingsInventory(meta.getInteger1()));
				else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createArenaInventory(meta.getInteger1()));
		}

		// Wave time limit menu for an arena
		else if (title.contains("Wave Time Limit:")) {
			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
			assert meta != null;
			Arena arenaInstance = ArenaManager.getArena(meta.getInteger1());
			int current = arenaInstance.getTimeLimit();

			// Decrease wave time limit
			if (buttonName.contains("Decrease")) {
				// Check for arena closure
				if (!arenaInstance.isClosed()) {
					player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
					return;
				}

				// Check if wave time limit is unlimited
				if (current == -1)
					arenaInstance.setTimeLimit(1);

				// Check if wave time limit is greater than 1
				else if (current <= 1) {
					player.sendMessage(Utils.notify("&cWave time limit cannot be less than 1!"));
					return;
				} else arenaInstance.setTimeLimit(--current);

				player.openInventory(Inventories.createWaveTimeLimitInventory(meta.getInteger1()));
			}

			// Set wave time limit to unlimited
			if (buttonName.contains("Unlimited")) {
				// Check for arena closure
				if (!arenaInstance.isClosed()) {
					player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
					return;
				}

				arenaInstance.setTimeLimit(-1);
				player.openInventory(Inventories.createWaveTimeLimitInventory(meta.getInteger1()));
			}

			// Reset wave time limit to 1
			if (buttonName.contains("Reset")) {
				// Check for arena closure
				if (!arenaInstance.isClosed()) {
					player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
					return;
				}

				arenaInstance.setTimeLimit(1);
				player.openInventory(Inventories.createWaveTimeLimitInventory(meta.getInteger1()));
			}

			// Increase wave time limit
			else if (buttonName.contains("Increase")) {
				// Check for arena closure
				if (!arenaInstance.isClosed()) {
					player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
					return;
				}

				// Check if wave time limit is unlimited
				if (current == -1)
					arenaInstance.setTimeLimit(1);
				else arenaInstance.setTimeLimit(++current);

				player.openInventory(Inventories.createWaveTimeLimitInventory(meta.getInteger1()));
			}

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createGameSettingsInventory(meta.getInteger1()));
		}

		// Sound settings menu for an arena
		else if (title.contains("Sounds:")) {
			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
			assert meta != null;
			Arena arenaInstance = ArenaManager.getArena(meta.getInteger1());

//			// Toggle win sound
//			if (buttonName.contains("Win")) {
//				if (arenaInstance.isClosed()) {
//					arenaInstance.setWinSound(!arenaInstance.hasWinSound());
//					player.openInventory(Inventories.createSoundsInventory(meta.getInteger1()));
//				} else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
//			}
//
//			// Toggle lose sound
//			else if (buttonName.contains("Lose")) {
//				if (arenaInstance.isClosed()) {
//					arenaInstance.setLoseSound(!arenaInstance.hasLoseSound());
//					player.openInventory(Inventories.createSoundsInventory(meta.getInteger1()));
//				} else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
//			}
//
//			// Toggle wave start sound
//			else if (buttonName.contains("Start")) {
//				if (arenaInstance.isClosed()) {
//					arenaInstance.setWaveStartSound(!arenaInstance.hasWaveStartSound());
//					player.openInventory(Inventories.createSoundsInventory(meta.getInteger1()));
//				} else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
//			}
//
//			// Toggle wave finish sound
//			else if (buttonName.contains("Finish")) {
//				if (arenaInstance.isClosed()) {
//					arenaInstance.setWaveFinishSound(!arenaInstance.hasWaveFinishSound());
//					player.openInventory(Inventories.createSoundsInventory(meta.getInteger1()));
//				} else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
//			}
//
			// Edit waiting sound
			if (buttonName.contains("Waiting"))
				if (arenaInstance.isClosed())
					player.openInventory(Inventories.createWaitSoundInventory(meta.getInteger1()));
				else player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));

			// Exit menu
			else if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createGameSettingsInventory(meta.getInteger1()));
		}

		// Waiting sound menu for an arena
		else if (title.contains("Waiting Sound:")) {
			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
			assert meta != null;
			Arena arenaInstance = ArenaManager.getArena(meta.getInteger1());

			// Exit menu
			if (buttonName.contains("EXIT"))
				player.openInventory(Inventories.createSoundsInventory(meta.getInteger1()));

			// Set sound
			else {
				// Check for arena closure
				if (!arenaInstance.isClosed()) {
					player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
					return;
				}

				arenaInstance.setWaitingSound(slot);
				player.openInventory(Inventories.createWaitSoundInventory(meta.getInteger1()));
			}
		}

		// Menu to copy game settings
		else if (title.contains("Copy Game Settings")) {
			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
			assert meta != null;
			Arena arena1 = ArenaManager.getArena(meta.getInteger1());

			if (slot < 45) {
				// Check for arena closure
				if (!arena1.isClosed()) {
					player.sendMessage(Utils.notify("&cArena must be closed to modify this!"));
					return;
				}

				Arena arena2 = ArenaManager.getArena(slot + 1);

				// Copy settings from another arena
				if (buttonType == Material.WHITE_CONCRETE)
					arena1.copy(arena2);
				else return;
			}

			// Exit menu
			else if (buttonName.contains("EXIT")) {
				player.openInventory(Inventories.createGameSettingsInventory(meta.getInteger1()));
				return;
			}

			// Save updates
			player.sendMessage(Utils.notify("&aGame settings copied!"));
		}

		// Stats menu for a player
//		else if (title.contains("'s Stats")) {
//			String name = title.substring(6, title.length() - 8);
//			if (buttonName.contains("Kits"))
//				player.openInventory(Inventories.createPlayerKitsInventory(plugin, name, player.getName()));
//		}

		// Stats menu for an arena
//		else if (title.contains("Info")) {
//			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
//			assert meta != null;
//
//			if (buttonName.contains("Custom Shop Inventory"))
//				player.openInventory(ArenaManager.getArena(meta.getInteger1()).getMockCustomShop());
//
//			else if (buttonName.contains("Allowed Kits"))
//				player.openInventory(Inventories.createAllowedKitsInventory(meta.getInteger1(), true));
//		}
	}

	// Ensures closing inventory doesn't mess up data
	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		String title = e.getView().getTitle();

		// Ignore non-plugin inventories
		if (!title.contains(Utils.format("&k")))
			return;

		// Ignore if safe close toggle is on
		if (close)
			return;

		// Close safely for the inventory of concern
		if (title.contains("Arena ")) {
			InventoryMeta meta = (InventoryMeta) e.getInventory().getHolder();
			assert meta != null;
			Arena arena = ArenaManager.getArena(meta.getInteger1());
			if (arena.getName() == null)
				ArenaManager.setArena(meta.getInteger1(), null);
		}
	}

	// Handles arena naming
	@EventHandler
	public void onRename(SignGUIEvent e) {
		Arena arena = e.getArena();
		Player player = e.getPlayer();

		// Try updating name
		try {
			arena.setName(e.getLines()[2]);
			Utils.debugInfo(String.format("Name changed for arena %d!", arena.getArena()), 2);
		} catch (Exception ignored) {
			player.sendMessage(Utils.notify(Utils.format("&cInvalid arena name!")));
			if (arena.getName() == null)
				ArenaManager.setArena(arena.getArena(), null);
			return;
		}

		player.openInventory(Inventories.createArenaInventory(arena.getArena()));
	}

	// Ensures safely opening inventories
	private void openInv(Player player, Inventory inventory) {
		// Set safe close toggle on
		close = true;

		// Open the desired inventory
		player.openInventory(inventory);

		// Set safe close toggle to off
		close = false;
	}
}
