package me.theguyhere.grinchsimulator.GUI;

import me.theguyhere.grinchsimulator.Main;
import me.theguyhere.grinchsimulator.game.models.presents.PresentType;
import me.theguyhere.grinchsimulator.game.models.presents.Presents;
import me.theguyhere.grinchsimulator.game.models.arenas.Arena;
import me.theguyhere.grinchsimulator.game.models.arenas.ArenaManager;
import me.theguyhere.grinchsimulator.tools.Utils;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.PacketPlayOutOpenSignEditor;
import net.minecraft.server.v1_16_R3.TileEntitySign;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftSign;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Inventories {
	// Easily alternate between different materials
	public static final Material[] INFO_BOARD_MATS = {Material.DARK_OAK_SIGN, Material.BIRCH_SIGN};
	private static final String CONSTRUCTION = Utils.format("&6Under Construction!");

	// Menu of all the arenas
	public static Inventory createArenasInventory() {
		// Create inventory
		Inventory inv = Bukkit.createInventory(null, 54,  Utils.format("&k") +
				Utils.format("&2&lGrinch Simulator Arenas"));

		// Options to interact with all 45 possible arenas
		for (int i = 1; i < 46; i++) {
			// Check if arena exists, set button accordingly
			if (ArenaManager.getArena(i) == null)
				inv.setItem(i - 1, Utils.createItem(Material.RED_CONCRETE,
						Utils.format("&c&lCreate Arena " + i)));
			else
				inv.setItem(i - 1, Utils.createItem(Material.LIME_CONCRETE,
						Utils.format("&a&lEdit " + ArenaManager.getArena(i).getName())));
		}

		// Option to set lobby location
		inv.setItem(45, Utils.createItem(Material.BELL, Utils.format("&2&lLobby"),
				Utils.format("&7Manage minigame lobby")));

		// Option to set info hologram
		inv.setItem(46, Utils.createItem(Material.OAK_SIGN, Utils.format("&6&lInfo Boards")));

		// Option to set leaderboard hologram
		inv.setItem(47, Utils.createItem(Material.GOLDEN_HELMET, Utils.format("&e&lLeaderboards"),
				Utils.BUTTON_FLAGS, null, CONSTRUCTION));

		// Option to exit
		inv.setItem(53, InventoryItems.exit());

		return inv;
	}

	// Menu for lobby
	public static Inventory createLobbyInventory(Main plugin) {
		// Create inventory
		Inventory inv = Bukkit.createInventory(null, 9, Utils.format("&k") +
				Utils.format("&2&lLobby"));

		// Option to create or relocate the lobby
		if (Utils.getConfigLocation(plugin, "lobby") == null)
			inv.setItem(0, InventoryItems.create("Lobby"));
		else inv.setItem(0, InventoryItems.relocate("Lobby"));

		// Option to teleport to the lobby
		inv.setItem(2, InventoryItems.teleport("Lobby"));

		// Option to center the lobby
		inv.setItem(4, InventoryItems.center("Lobby"));

		// Option to remove the lobby
		inv.setItem(6, InventoryItems.remove("LOBBY"));

		// Option to exit
		inv.setItem(8, InventoryItems.exit());

		return inv;
	}

	// Confirmation menu for removing lobby
	public static Inventory createLobbyConfirmInventory() {
		Inventory inv = Bukkit.createInventory(null, 9, Utils.format("&k") +
				Utils.format("&4&lRemove Lobby?"));

		// "No" option
		inv.setItem(0, InventoryItems.no());

		// "Yes" option
		inv.setItem(8, InventoryItems.yes());

		return inv;
	}

	// Menu for editing info boards
	public static Inventory createInfoBoardInventory(Main plugin) {
		// Create inventory
		Inventory inv = Bukkit.createInventory(null, 9, Utils.format("&k") +
				Utils.format("&6&lInfo Boards"));

		// Prepare for material indexing
		int index;

		// Options to interact with all 8 possible info boards
		for (int i = 1; i < 9; i++) {
			// Check if the info board exists
			if (!plugin.getArenaData().contains("infoBoard." + i))
				index = 0;
			else index = 1;

			// Create and set item
			inv.setItem(i - 1, Utils.createItem(INFO_BOARD_MATS[index], Utils.format("&6&lInfo Board " + i)));
		}

		// Option to exit
		inv.setItem(8, InventoryItems.exit());

		return inv;
	}

	// Menu for editing a specific info board
	public static Inventory createInfoBoardMenu(Main plugin, int arena) {
		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&6&lInfo Board " + arena));

		// Option to create or relocate info board
		if (Utils.getConfigLocation(plugin, "infoBoard." + arena) == null)
			inv.setItem(0, InventoryItems.create("Info Board"));
		else inv.setItem(0, InventoryItems.relocate("Info Board"));

		// Option to teleport to info board
		inv.setItem(2, InventoryItems.teleport("Info Board"));

		// Option to center the info board
		inv.setItem(4, InventoryItems.center("Info Board"));

		// Option to remove info board
		inv.setItem(6, InventoryItems.remove("INFO BOARD"));

		// Option to exit
		inv.setItem(8, InventoryItems.exit());

		return inv;
	}

	// Confirmation menu for removing info boards
	public static Inventory createInfoBoardConfirmInventory(int arena) {
		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&4&lRemove Info Board?"));

		// "No" option
		inv.setItem(0, InventoryItems.no());

		// "Yes" option
		inv.setItem(8, InventoryItems.yes());

		return inv;
	}

//	// Menu for leaderboards
//	public static Inventory createLeaderboardInventory() {
//		// Create inventory
//		Inventory inv = Bukkit.createInventory(null, 9, Utils.format("&k") +
//				Utils.format("&e&lLeaderboards"));
//
//		// Option to modify total kills leaderboard
//		inv.setItem(0, Utils.createItem(Material.DRAGON_HEAD, Utils.format("&4&lTotal Kills Leaderboard")));
//
//		// Option to modify top kills leaderboard
//		inv.setItem(1, Utils.createItem(Material.ZOMBIE_HEAD, Utils.format("&c&lTop Kills Leaderboard")));
//
//		// Option to modify total gems leaderboard
//		inv.setItem(2, Utils.createItem(Material.EMERALD_BLOCK, Utils.format("&2&lTotal Gems Leaderboard")));
//
//		// Option to modify top balance leaderboard
//		inv.setItem(3, Utils.createItem(Material.EMERALD, Utils.format("&a&lTop Balance Leaderboard")));
//
//		// Option to modify top wave leaderboard
//		inv.setItem(4, Utils.createItem(Material.GOLDEN_SWORD, Utils.format("&9&lTop Wave Leaderboard"),
//				Utils.BUTTON_FLAGS, null));
//
//		// Option to exit
//		inv.setItem(8, InventoryItems.exit());
//
//		return inv;
//	}
//
//	// Menu for editing the total kills leaderboard
//	public static Inventory createTotalKillsLeaderboardInventory(Main plugin) {
//		// Create inventory
//		Inventory inv = Bukkit.createInventory(null, 9, Utils.format("&k") +
//				Utils.format("&4&lTotal Kills Leaderboard"));
//
//		// Option to create or relocate the leaderboard
//		if (Utils.getConfigLocation(plugin, "leaderboard.totalKills") == null)
//			inv.setItem(0, InventoryItems.create("Leaderboard"));
//		else inv.setItem(0, InventoryItems.relocate("Leaderboard"));
//
//		// Option to teleport to the leaderboard
//		inv.setItem(2, InventoryItems.teleport("Leaderboard"));
//
//		// Option to center the leaderboard
//		inv.setItem(4, InventoryItems.center("Leaderboard"));
//
//		// Option to remove the leaderboard
//		inv.setItem(6, InventoryItems.remove("LEADERBOARD"));
//
//		// Option to exit
//		inv.setItem(8, InventoryItems.exit());
//
//		return inv;
//	}
//
//	// Menu for editing the top kills leaderboard
//	public static Inventory createTopKillsLeaderboardInventory(Main plugin) {
//		// Create inventory
//		Inventory inv = Bukkit.createInventory(null, 9, Utils.format("&k") +
//				Utils.format("&c&lTop Kills Leaderboard"));
//
//		// Option to create or relocate the leaderboard
//		if (Utils.getConfigLocation(plugin, "leaderboard.topKills") == null)
//			inv.setItem(0, InventoryItems.create("Leaderboard"));
//		else inv.setItem(0, InventoryItems.relocate("Leaderboard"));
//
//		// Option to teleport to the leaderboard
//		inv.setItem(2, InventoryItems.teleport("Leaderboard"));
//
//		// Option to center the leaderboard
//		inv.setItem(4, InventoryItems.center("Leaderboard"));
//
//		// Option to remove the leaderboard
//		inv.setItem(6, InventoryItems.remove("LEADERBOARD"));
//
//		// Option to exit
//		inv.setItem(8, InventoryItems.exit());
//
//		return inv;
//	}
//
//	// Menu for editing the total gems leaderboard
//	public static Inventory createTotalGemsLeaderboardInventory(Main plugin) {
//		// Create inventory
//		Inventory inv = Bukkit.createInventory(null, 9, Utils.format("&k") +
//				Utils.format("&2&lTotal Gems Leaderboard"));
//
//		// Option to create or relocate the leaderboard
//		if (Utils.getConfigLocation(plugin, "leaderboard.totalGems") == null)
//			inv.setItem(0, InventoryItems.create("Leaderboard"));
//		else inv.setItem(0, InventoryItems.relocate("Leaderboard"));
//
//		// Option to teleport to the leaderboard
//		inv.setItem(2, InventoryItems.teleport("Leaderboard"));
//
//		// Option to center the leaderboard
//		inv.setItem(4, InventoryItems.center("Leaderboard"));
//
//		// Option to remove the leaderboard
//		inv.setItem(6, InventoryItems.remove("LEADERBOARD"));
//
//		// Option to exit
//		inv.setItem(8, InventoryItems.exit());
//
//		return inv;
//	}
//
//	// Menu for editing the top balance leaderboard
//	public static Inventory createTopBalanceLeaderboardInventory(Main plugin) {
//		// Create inventory
//		Inventory inv = Bukkit.createInventory(null, 9, Utils.format("&k") +
//				Utils.format("&a&lTop Balance Leaderboard"));
//
//		// Option to create or relocate the leaderboard
//		if (Utils.getConfigLocation(plugin, "leaderboard.topBalance") == null)
//			inv.setItem(0, InventoryItems.create("Leaderboard"));
//		else inv.setItem(0, InventoryItems.relocate("Leaderboard"));
//
//		// Option to teleport to the leaderboard
//		inv.setItem(2, InventoryItems.teleport("Leaderboard"));
//
//		// Option to center the leaderboard
//		inv.setItem(4, InventoryItems.center("Leaderboard"));
//
//		// Option to remove the leaderboard
//		inv.setItem(6, InventoryItems.remove("LEADERBOARD"));
//
//		// Option to exit
//		inv.setItem(8, InventoryItems.exit());
//
//		return inv;
//	}
//
//	// Menu for editing the top wave leaderboard
//	public static Inventory createTopWaveLeaderboardInventory(Main plugin) {
//		// Create inventory
//		Inventory inv = Bukkit.createInventory(null, 9, Utils.format("&k") +
//				Utils.format("&9&lTop Wave Leaderboard"));
//
//		// Option to create or relocate the leaderboard
//		if (Utils.getConfigLocation(plugin, "leaderboard.topWave") == null)
//			inv.setItem(0, InventoryItems.create("Leaderboard"));
//		else inv.setItem(0, InventoryItems.relocate("Leaderboard"));
//
//		// Option to teleport to the leaderboard
//		inv.setItem(2, InventoryItems.teleport("Leaderboard"));
//
//		// Option to center the leaderboard
//		inv.setItem(4, InventoryItems.center("Leaderboard"));
//
//		// Option to remove the leaderboard
//		inv.setItem(6, InventoryItems.remove("LEADERBOARD"));
//
//		// Option to exit
//		inv.setItem(8, InventoryItems.exit());
//
//		return inv;
//	}
//
//	// Confirmation menu for total kills leaderboard
//	public static Inventory createTotalKillsConfirmInventory() {
//		Inventory inv = Bukkit.createInventory(null, 9, Utils.format("&k") +
//				Utils.format("&4&lRemove Total Kills Leaderboard?"));
//
//		// "No" option
//		inv.setItem(0, InventoryItems.no());
//
//		// "Yes" option
//		inv.setItem(8, InventoryItems.yes());
//
//		return inv;
//	}
//
//	// Confirmation menu for top kills leaderboard
//	public static Inventory createTopKillsConfirmInventory() {
//		Inventory inv = Bukkit.createInventory(null, 9, Utils.format("&k") +
//				Utils.format("&4&lRemove Top Kills Leaderboard?"));
//
//		// "No" option
//		inv.setItem(0, InventoryItems.no());
//
//		// "Yes" option
//		inv.setItem(8, InventoryItems.yes());
//
//		return inv;
//	}
//
//	// Confirmation menu for total gems leaderboard
//	public static Inventory createTotalGemsConfirmInventory() {
//		Inventory inv = Bukkit.createInventory(null, 9, Utils.format("&k") +
//				Utils.format("&4&lRemove Total Gems Leaderboard?"));
//
//		// "No" option
//		inv.setItem(0, InventoryItems.no());
//
//		// "Yes" option
//		inv.setItem(8, InventoryItems.yes());
//
//		return inv;
//	}
//
//	// Confirmation menu for top balance leaderboard
//	public static Inventory createTopBalanceConfirmInventory() {
//		Inventory inv = Bukkit.createInventory(null, 9, Utils.format("&k") +
//				Utils.format("&4&lRemove Top Balance Leaderboard?"));
//
//		// "No" option
//		inv.setItem(0, InventoryItems.no());
//
//		// "Yes" option
//		inv.setItem(8, InventoryItems.yes());
//
//		return inv;
//	}
//
//	// Confirmation menu for top wave leaderboard
//	public static Inventory createTopWaveConfirmInventory() {
//		Inventory inv = Bukkit.createInventory(null, 9, Utils.format("&k") +
//				Utils.format("&4&lRemove Top Wave Leaderboard?"));
//
//		// "No" option
//		inv.setItem(0, InventoryItems.no());
//
//		// "Yes" option
//		inv.setItem(8, InventoryItems.yes());
//
//		return inv;
//	}

	// Menu for editing an arena
	public static Inventory createArenaInventory(int arena) {
		Arena arenaInstance = ArenaManager.getArena(arena);
		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&2&lEdit " + arenaInstance.getName()));

		// Option to edit name
		inv.setItem(0, Utils.createItem(Material.NAME_TAG, Utils.format("&6&lEdit Name")));

		// Option to edit game portal and leaderboard
		inv.setItem(1, Utils.createItem(Material.END_PORTAL_FRAME,
				Utils.format("&5&lPortal and Leaderboard")));

		// Option to edit player settings
		inv.setItem(2, Utils.createItem(Material.PLAYER_HEAD, Utils.format("&d&lPlayer Settings")));

		// Option to edit present settings
		inv.setItem(3, Utils.rename(Presents.RED_PRESENT, Utils.format("&b&lPresent Settings")));

		// Option to edit miscellaneous game settings
		inv.setItem(4, Utils.createItem(Material.REDSTONE, Utils.format("&7&lGame Settings")));

		// Option to close the arena
		String closed;
		if (arenaInstance.isClosed())
			closed = "&c&lCLOSED";
		else closed = "&a&lOPEN";
		inv.setItem(5, Utils.createItem(Material.NETHER_BRICK_FENCE, Utils.format("&9&lClose Arena: " + closed)));

		// Option to remove arena
		inv.setItem(6, InventoryItems.remove("ARENA"));

		// Option to exit
		inv.setItem(8, InventoryItems.exit());

		return inv;
	}

	// Confirmation menu for removing an arena
	public static Inventory createArenaConfirmInventory(int arena) {
		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&4&lRemove " + ArenaManager.getArena(arena).getName() + '?'));

		// "No" option
		inv.setItem(0, InventoryItems.no());

		// "Yes" option
		inv.setItem(8, InventoryItems.yes());

		return inv;
	}

	// Menu for editing the portal and leaderboard of an arena
	public static Inventory createPortalInventory(int arena) {
		Arena arenaInstance = ArenaManager.getArena(arena);

		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&5&lPortal/LBoard: " + arenaInstance.getName()));

		// Option to create or relocate the portal
		if (arenaInstance.getPortal() == null)
			inv.setItem(0, InventoryItems.create("Portal"));
		else inv.setItem(0, InventoryItems.relocate("Portal"));

		// Option to teleport to the portal
		inv.setItem(1, InventoryItems.teleport("Portal"));

		// Option to center the portal
		inv.setItem(2, InventoryItems.center("Portal"));

		// Option to remove the portal
		inv.setItem(3, InventoryItems.remove("PORTAL"));

		// Option to edit the present leaderboard
		inv.setItem(5, Utils.createItem(Material.CRIMSON_SIGN, Utils.format("&5&lPresent Leaderboard")));

		// Option to edit the happiness leaderboard
		inv.setItem(6, Utils.createItem(Material.WARPED_SIGN, Utils.format("&5&lHappiness Leaderboard")));

		// Option to exit
		inv.setItem(8, InventoryItems.exit());

		return inv;
	}

	// Menu for editing the present leaderboard of an arena
	public static Inventory createPresentLeaderInventory(int arena) {
		Arena arenaInstance = ArenaManager.getArena(arena);

		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&5&lPresent LBoard: " + arenaInstance.getName()));

		// Option to create or relocate the leaderboard
		if (arenaInstance.getPresentLeaders() == null)
			inv.setItem(0, InventoryItems.create("Leaderboard"));
		else inv.setItem(0, InventoryItems.relocate("Leaderboard"));

		// Option to teleport to the leaderboard
		inv.setItem(2, InventoryItems.teleport("Leaderboard"));

		// Option to center the leaderboard
		inv.setItem(4, InventoryItems.center("Leaderboard"));

		// Option to remove the leaderboard
		inv.setItem(6, InventoryItems.remove("LEADERBOARD"));

		// Option to exit
		inv.setItem(8, InventoryItems.exit());

		return inv;
	}

	// Menu for editing the happiness leaderboard of an arena
	public static Inventory createHappyLeaderInventory(int arena) {
		Arena arenaInstance = ArenaManager.getArena(arena);

		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&5&lHappiness LBoard: " + arenaInstance.getName()));

		// Option to create or relocate the leaderboard
		if (arenaInstance.getHappyLeaders() == null)
			inv.setItem(0, InventoryItems.create("Leaderboard"));
		else inv.setItem(0, InventoryItems.relocate("Leaderboard"));

		// Option to teleport to the leaderboard
		inv.setItem(2, InventoryItems.teleport("Leaderboard"));

		// Option to center the leaderboard
		inv.setItem(4, InventoryItems.center("Leaderboard"));

		// Option to remove the leaderboard
		inv.setItem(6, InventoryItems.remove("LEADERBOARD"));

		// Option to exit
		inv.setItem(8, InventoryItems.exit());

		return inv;
	}

	// Confirmation menu for removing the arena portal
	public static Inventory createPortalConfirmInventory(int arena) {
		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&4&lRemove Portal?"));

		// "No" option
		inv.setItem(0, InventoryItems.no());

		// "Yes" option
		inv.setItem(8, InventoryItems.yes());

		return inv;
	}

	// Confirmation menu for removing the present leaderboard
	public static Inventory createPresentLeadersConfirmInventory(int arena) {
		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&4&lRemove Present LBoard?"));

		// "No" option
		inv.setItem(0, InventoryItems.no());

		// "Yes" option
		inv.setItem(8, InventoryItems.yes());

		return inv;
	}

	// Confirmation menu for removing the happiness leaderboard
	public static Inventory createHappyLeadersConfirmInventory(int arena) {
		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&4&lRemove Happiness LBoard?"));

		// "No" option
		inv.setItem(0, InventoryItems.no());

		// "Yes" option
		inv.setItem(8, InventoryItems.yes());

		return inv;
	}

	// Menu for editing the player settings of an arena
	public static Inventory createPlayersInventory(int arena) {
		Arena arenaInstance = ArenaManager.getArena(arena);

		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&d&lPlayer Settings: " + arenaInstance.getName()));

		// Option to edit player spawn
		inv.setItem(0, Utils.createItem(Material.END_PORTAL_FRAME, Utils.format("&5&lPlayer Spawn")));

		// Option to edit waiting room
		inv.setItem(2, Utils.createItem(Material.CLOCK, Utils.format("&b&lWaiting Room"),
				Utils.format("&7An optional room to wait in before"), Utils.format("&7the game starts")));

		// Option to edit max players
		inv.setItem(4, Utils.createItem(Material.NETHERITE_HELMET,
				Utils.format("&4&lMaximum Players"),
				Utils.BUTTON_FLAGS,
				null,
				Utils.format("&7Maximum players the game will have")));

		// Option to edit min players
		inv.setItem(6, Utils.createItem(Material.NETHERITE_BOOTS,
				Utils.format("&2&lMinimum Players"),
				Utils.BUTTON_FLAGS,
				null,
				Utils.format("&7Minimum players needed for game to start")));

		// Option to exit
		inv.setItem(8, InventoryItems.exit());

		return inv;
	}

	// Menu for editing the player spawn of an arena
	public static Inventory createPlayerSpawnInventory(int arena) {
		Arena arenaInstance = ArenaManager.getArena(arena);

		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&d&lPlayer Spawn: " + arenaInstance.getName()));

		// Option to create or relocate player spawn
		if (arenaInstance.getPlayerSpawn() != null)
			inv.setItem(0, InventoryItems.relocate("Spawn"));
		else inv.setItem(0, InventoryItems.create("Spawn"));

		// Option to teleport to player spawn
		inv.setItem(2, InventoryItems.teleport("Spawn"));

		// Option to center the player spawn
		inv.setItem(4, InventoryItems.center("Spawn"));

		// Option to remove player spawn
		inv.setItem(6, InventoryItems.remove("SPAWN"));

		// Option to exit
		inv.setItem(8, InventoryItems.exit());

		return inv;
	}

	// Confirmation menu for removing player spawn
	public static Inventory createSpawnConfirmInventory(int arena) {
		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&4&lRemove Spawn?"));

		// "No" option
		inv.setItem(0, InventoryItems.no());

		// "Yes" option
		inv.setItem(8, InventoryItems.yes());

		return inv;
	}

	// Menu for editing the waiting room of an arena
	public static Inventory createWaitingRoomInventory(int arena) {
		Arena arenaInstance = ArenaManager.getArena(arena);

		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&b&lWaiting Room: " + arenaInstance.getName()));

		// Option to create waiting room
		if (arenaInstance.getWaitingRoom() == null)
			inv.setItem(0, InventoryItems.create("Waiting Room"));
		else inv.setItem(0, InventoryItems.relocate("Waiting Room"));

		// Option to teleport to waiting room
		inv.setItem(2, InventoryItems.teleport("Waiting Room"));

		// Option to center the waiting room
		inv.setItem(4, InventoryItems.center("Waiting Room"));

		// Option to remove waiting room
		inv.setItem(6, InventoryItems.remove("WAITING ROOM"));

		// Option to exit
		inv.setItem(8, InventoryItems.exit());

		return inv;
	}

	// Confirmation menu for removing waiting room
	public static Inventory createWaitingConfirmInventory(int arena) {
		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&4&lRemove Waiting Room?"));

		// "No" option
		inv.setItem(0, InventoryItems.no());

		// "Yes" option
		inv.setItem(8, InventoryItems.yes());

		return inv;
	}

	// Menu for changing max players in an arena
	public static Inventory createMaxPlayerInventory(int arena) {
		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&4&lMaximum Players: " + ArenaManager.getArena(arena).getMaxPlayers()));

		// Option to decrease
		for (int i = 0; i < 4; i++)
			inv.setItem(i, Utils.createItem(Material.RED_CONCRETE, Utils.format("&4&lDecrease")));

		// Option to increase
		for (int i = 4; i < 8; i++)
			inv.setItem(i, Utils.createItem(Material.LIME_CONCRETE, Utils.format("&2&lIncrease")));

		// Option to exit
		inv.setItem(8, InventoryItems.exit());

		return inv;
	}

	// Menu for changing min players in an arena
	public static Inventory createMinPlayerInventory(int arena) {
		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&2&lMinimum Players: " + ArenaManager.getArena(arena).getMinPlayers()));

		// Option to decrease
		for (int i = 0; i < 4; i++)
			inv.setItem(i, Utils.createItem(Material.RED_CONCRETE, Utils.format("&4&lDecrease")));

		// Option to increase
		for (int i = 4; i < 8; i++)
			inv.setItem(i, Utils.createItem(Material.LIME_CONCRETE, Utils.format("&2&lIncrease")));

		// Option to exit
		inv.setItem(8, InventoryItems.exit());

		return inv;
	}

	// Menu for editing the present settings of an arena
	public static Inventory createPresentSettingsInventory(int arena) {
		Arena arenaInstance = ArenaManager.getArena(arena);

		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&b&lPresent Settings: " + arenaInstance.getName()));

		// Option to edit presents
		inv.setItem(1, Utils.createItem(Material.CRAFTING_TABLE, Utils.format("&a&lAdd/Remove Presents")));

		// Option to delete presents by type
		inv.setItem(3, Utils.createItem(Material.LAVA_BUCKET, Utils.format("&4&lDelete Presents by Type")));

		// Option to edit present values
		inv.setItem(5, Utils.rename(Presents.GOLD_PRESENT, Utils.format("&6&lPresent Values")));

		// Option to exit
		inv.setItem(8, InventoryItems.exit());

		return inv;
	}

	// Menu for deleting presents
	public static Inventory createPresentDeleteInventory(int arena) {
		Arena arenaInstance = ArenaManager.getArena(arena);

		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 27, Utils.format("&k") +
				Utils.format("&4&lDelete Presents: " + arenaInstance.getName()));

		// All present options
		inv.setItem(0, Utils.rename(Presents.WOOD_PRESENT, Utils.format("&5&lWood Presents")));
		inv.setItem(1, Utils.rename(Presents.STONE_PRESENT, Utils.format("&8&lStone Presents")));
		inv.setItem(2, Utils.rename(Presents.IRON_PRESENT, Utils.format("&7&lIron Presents")));
		inv.setItem(3, Utils.rename(Presents.COPPER_PRESENT, Utils.format("&3&lCopper Presents")));
		inv.setItem(4, Utils.rename(Presents.GOLD_PRESENT, Utils.format("&6&lGold Presents")));
		inv.setItem(5, Utils.rename(Presents.DIAMOND_PRESENT, Utils.format("&b&lDiamond Presents")));
		inv.setItem(6, Utils.rename(Presents.EMERALD_PRESENT, Utils.format("&a&lEmerald Presents")));
		inv.setItem(7, Utils.rename(Presents.NETHERITE_PRESENT, Utils.format("&7&lNetherite Presents")));
		inv.setItem(8, Utils.rename(Presents.BLACK_PRESENT, Utils.format("&8&lBlack Presents")));
		inv.setItem(9, Utils.rename(Presents.BROWN_PRESENT, Utils.format("&5&lBrown Presents")));
		inv.setItem(10, Utils.rename(Presents.RED_PRESENT, Utils.format("&c&lRed Presents")));
		inv.setItem(11, Utils.rename(Presents.ORANGE_PRESENT, Utils.format("&6&lOrange Presents")));
		inv.setItem(12, Utils.rename(Presents.YELLOW_PRESENT, Utils.format("&e&lYellow Presents")));
		inv.setItem(13, Utils.rename(Presents.GREEN_PRESENT, Utils.format("&2&lGreen Presents")));
		inv.setItem(14, Utils.rename(Presents.CYAN_PRESENT, Utils.format("&3&lCyan Presents")));
		inv.setItem(15, Utils.rename(Presents.BLUE_PRESENT, Utils.format("&9&lBlue Presents")));
		inv.setItem(16, Utils.rename(Presents.PURPLE_PRESENT, Utils.format("&5&lPurple Presents")));
		inv.setItem(17, Utils.rename(Presents.PINK_PRESENT, Utils.format("&d&lPink Presents")));
		inv.setItem(21, Utils.rename(Presents.WHITE_PRESENT, Utils.format("&f&lWhite Presents")));

		// All presents option
		inv.setItem(23, Utils.createItem(Material.CHEST, Utils.format("&4&lAll Presents")));

		// Option to exit
		inv.setItem(26, InventoryItems.exit());

		return inv;
	}

	// Confirmation menu for removing a specific type of present
	public static Inventory createPresentDeleteConfirmInventory(int arena, PresentType type) {
		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(type.label.toUpperCase(), arena), 9,
				Utils.format("&k") + Utils.format("&4&lRemove " +
						type.label.substring(0, 1).toUpperCase() + type.label.substring(1) + " Presents?"));

		// "No" option
		inv.setItem(0, InventoryItems.no());

		// "Yes" option
		inv.setItem(8, InventoryItems.yes());

		return inv;
	}

	// Confirmation menu for removing all presents
	public static Inventory createPresentsDeleteConfirmInventory(int arena) {
		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&4&lRemove All Presents?"));

		// "No" option
		inv.setItem(0, InventoryItems.no());

		// "Yes" option
		inv.setItem(8, InventoryItems.yes());

		return inv;
	}

	// Menu for editing the value of presents
	public static Inventory createPresentValueInventory(int arena) {
		Arena arenaInstance = ArenaManager.getArena(arena);

		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 27, Utils.format("&k") +
				Utils.format("&6&lPresent Values: " + arenaInstance.getName()));

		// All present options
		inv.setItem(0, Utils.rename(Presents.WOOD_PRESENT, Utils.format("&5&lWood Presents: ")
				+ arenaInstance.getPresentValue(PresentType.WOOD)));
		inv.setItem(1, Utils.rename(Presents.STONE_PRESENT, Utils.format("&8&lStone Presents: ")
				+ arenaInstance.getPresentValue(PresentType.STONE)));
		inv.setItem(2, Utils.rename(Presents.IRON_PRESENT, Utils.format("&7&lIron Presents: ")
				+ arenaInstance.getPresentValue(PresentType.IRON)));
		inv.setItem(3, Utils.rename(Presents.COPPER_PRESENT, Utils.format("&3&lCopper Presents: ")
				+ arenaInstance.getPresentValue(PresentType.COPPER)));
		inv.setItem(4, Utils.rename(Presents.GOLD_PRESENT, Utils.format("&6&lGold Presents: ")
				+ arenaInstance.getPresentValue(PresentType.GOLD)));
		inv.setItem(5, Utils.rename(Presents.DIAMOND_PRESENT, Utils.format("&b&lDiamond Presents: ")
				+ arenaInstance.getPresentValue(PresentType.DIAMOND)));
		inv.setItem(6, Utils.rename(Presents.EMERALD_PRESENT, Utils.format("&a&lEmerald Presents: ")
				+ arenaInstance.getPresentValue(PresentType.EMERALD)));
		inv.setItem(7, Utils.rename(Presents.NETHERITE_PRESENT, Utils.format("&7&lNetherite Presents: ")
				+ arenaInstance.getPresentValue(PresentType.NETHERITE)));
		inv.setItem(8, Utils.rename(Presents.BLACK_PRESENT, Utils.format("&8&lBlack Presents: ")
				+ arenaInstance.getPresentValue(PresentType.BLACK)));
		inv.setItem(9, Utils.rename(Presents.BROWN_PRESENT, Utils.format("&5&lBrown Presents: ")
				+ arenaInstance.getPresentValue(PresentType.BROWN)));
		inv.setItem(10, Utils.rename(Presents.RED_PRESENT, Utils.format("&c&lRed Presents: ")
				+ arenaInstance.getPresentValue(PresentType.RED)));
		inv.setItem(11, Utils.rename(Presents.ORANGE_PRESENT, Utils.format("&6&lOrange Presents: ")
				+ arenaInstance.getPresentValue(PresentType.ORANGE)));
		inv.setItem(12, Utils.rename(Presents.YELLOW_PRESENT, Utils.format("&e&lYellow Presents: ")
				+ arenaInstance.getPresentValue(PresentType.YELLOW)));
		inv.setItem(13, Utils.rename(Presents.GREEN_PRESENT, Utils.format("&2&lGreen Presents: ")
				+ arenaInstance.getPresentValue(PresentType.GREEN)));
		inv.setItem(14, Utils.rename(Presents.CYAN_PRESENT, Utils.format("&3&lCyan Presents: ")
				+ arenaInstance.getPresentValue(PresentType.CYAN)));
		inv.setItem(15, Utils.rename(Presents.BLUE_PRESENT, Utils.format("&9&lBlue Presents: ")
				+ arenaInstance.getPresentValue(PresentType.BLUE)));
		inv.setItem(16, Utils.rename(Presents.PURPLE_PRESENT, Utils.format("&5&lPurple Presents: ")
				+ arenaInstance.getPresentValue(PresentType.PURPLE)));
		inv.setItem(17, Utils.rename(Presents.PINK_PRESENT, Utils.format("&d&lPink Presents: ")
				+ arenaInstance.getPresentValue(PresentType.PINK)));
		inv.setItem(21, Utils.rename(Presents.WHITE_PRESENT, Utils.format("&f&lWhite Presents: ")
				+ arenaInstance.getPresentValue(PresentType.WHITE)));

		// All presents option
		inv.setItem(23, Utils.createItem(Material.BEDROCK, Utils.format("&4&lReset Values"),
				Utils.format("&7Resets all present values back to 1")));

		// Option to exit
		inv.setItem(26, InventoryItems.exit());

		return inv;
	}

	// Menu for changing present value in an arena
	public static Inventory createPresentValueMenu(int arena, PresentType type) {
		Arena arenaInstance = ArenaManager.getArena(arena);

		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(type.label.toUpperCase(), arena), 9,
				Utils.format("&k") +
				Utils.format("&6&l" + type.label.substring(0, 1).toUpperCase() + type.label.substring(1) +
						" Present Value: " + arenaInstance.getPresentValue(type)));

		// Option to decrease
		for (int i = 0; i < 4; i++)
			inv.setItem(i, Utils.createItem(Material.RED_CONCRETE, Utils.format("&4&lDecrease")));

		// Option to increase
		for (int i = 4; i < 8; i++)
			inv.setItem(i, Utils.createItem(Material.LIME_CONCRETE, Utils.format("&2&lIncrease")));

		// Option to exit
		inv.setItem(8, InventoryItems.exit());

		return inv;
	}

	// Confirmation menu for resetting all present values
	public static Inventory createPresentsResetConfirmInventory(int arena) {
		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&4&lReset All Presents?"));

		// "No" option
		inv.setItem(0, InventoryItems.no());

		// "Yes" option
		inv.setItem(8, InventoryItems.yes());

		return inv;
	}

	// Menu for editing the game settings of an arena
	public static Inventory createGameSettingsInventory(int arena) {
		Arena arenaInstance = ArenaManager.getArena(arena);

		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&8&lGame Settings: " + arenaInstance.getName()));

		// Option to wave time limit
		inv.setItem(1, Utils.createItem(Material.CLOCK, Utils.format("&2&lWave Time Limit")));

		// Option to edit sounds
		inv.setItem(3, Utils.createItem(Material.MUSIC_DISC_13,
				Utils.format("&d&lSounds"),
				Utils.BUTTON_FLAGS,
				null));

		// Option to copy game settings from another arena or a preset
		inv.setItem(5, Utils.createItem(Material.WRITABLE_BOOK,
				Utils.format("&f&lCopy Game Settings"),
				Utils.format("&7Copy settings of another arena or"),
				Utils.format("&7choose from a menu of presets")));

		// Option to exit
		inv.setItem(8, InventoryItems.exit());

		return inv;
	}

	// Menu for changing wave time limit of an arena
	public static Inventory createWaveTimeLimitInventory(int arena) {
		Inventory inv;

		// Create inventory
		if (ArenaManager.getArena(arena).getTimeLimit() < 0)
			inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
					Utils.format("&2&lWave Time Limit: Unlimited"));
		else inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&2&lWave Time Limit: " + ArenaManager.getArena(arena).getTimeLimit()));

		// Option to decrease
		for (int i = 0; i < 3; i++)
			inv.setItem(i, Utils.createItem(Material.RED_CONCRETE, Utils.format("&4&lDecrease")));

		// Option to set to unlimited
		inv.setItem(3, Utils.createItem(Material.ORANGE_CONCRETE, Utils.format("&6&lUnlimited")));

		// Option to reset to 1
		inv.setItem(4, Utils.createItem(Material.LIGHT_BLUE_CONCRETE, Utils.format("&3&lReset to 1")));

		// Option to increase
		for (int i = 5; i < 8; i++)
			inv.setItem(i, Utils.createItem(Material.LIME_CONCRETE, Utils.format("&2&lIncrease")));

		// Option to exit
		inv.setItem(8, InventoryItems.exit());

		return inv;
	}

	// Menu for editing the sounds of an arena
	public static Inventory createSoundsInventory(int arena) {
		Arena arenaInstance = ArenaManager.getArena(arena);

		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 9, Utils.format("&k") +
				Utils.format("&d&lSounds: " + ArenaManager.getArena(arena).getName()));

//		// Option to edit win sound
//		inv.setItem(0, Utils.createItem(Material.MUSIC_DISC_PIGSTEP,
//				Utils.format("&a&lWin Sound: " + getToggleStatus(arenaInstance.hasWinSound())),
//				Utils.BUTTON_FLAGS,
//				null,
//				Utils.format("&7Played when game ends and players win")));
//
//		// Option to edit lose sound
//		inv.setItem(1, Utils.createItem(Material.MUSIC_DISC_11,
//				Utils.format("&e&lLose Sound: " + getToggleStatus(arenaInstance.hasLoseSound())),
//				Utils.BUTTON_FLAGS,
//				null,
//				Utils.format("&7Played when game ends and players lose")));
//
//		// Option to edit wave start sound
//		inv.setItem(2, Utils.createItem(Material.MUSIC_DISC_CAT,
//				Utils.format("&2&lWave Start Sound: " + getToggleStatus(arenaInstance.hasWaveStartSound())),
//				Utils.BUTTON_FLAGS,
//				null,
//				Utils.format("&7Played when a wave starts")));
//
//		// Option to edit wave finish sound
//		inv.setItem(3, Utils.createItem(Material.MUSIC_DISC_BLOCKS,
//				Utils.format("&9&lWave Finish Sound: " + getToggleStatus(arenaInstance.hasWaveFinishSound())),
//				Utils.BUTTON_FLAGS,
//				null,
//				Utils.format("&7Played when a wave ends")));

		// Option to edit waiting music
		inv.setItem(4, Utils.createItem(Material.MUSIC_DISC_MELLOHI,
				Utils.format("&6&lWaiting Sound"),
				Utils.BUTTON_FLAGS,
				null,
				Utils.format("&7Played while players wait"), Utils.format("&7for the game to start")));

		// Option to exit
		inv.setItem(8, InventoryItems.exit());

		return inv;
	}

	// Menu for editing the win sound of an arena
	public static Inventory createWaitSoundInventory(int arena) {
		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 18, Utils.format("&k") +
				Utils.format("&6&lWaiting Sound: " + ArenaManager.getArena(arena).getWaitingSoundName()));

		Arena arenaInstance = ArenaManager.getArena(arena);

		// Sound options
		inv.setItem(0, arenaInstance.getWaitingSoundButton(0));
		inv.setItem(1, arenaInstance.getWaitingSoundButton(1));
		inv.setItem(2, arenaInstance.getWaitingSoundButton(2));
		inv.setItem(3, arenaInstance.getWaitingSoundButton(3));
		inv.setItem(4, arenaInstance.getWaitingSoundButton(4));
		inv.setItem(5, arenaInstance.getWaitingSoundButton(5));

		inv.setItem(9, arenaInstance.getWaitingSoundButton(9));
		inv.setItem(10, arenaInstance.getWaitingSoundButton(10));
		inv.setItem(11, arenaInstance.getWaitingSoundButton(11));
		inv.setItem(12, arenaInstance.getWaitingSoundButton(12));
		inv.setItem(13, arenaInstance.getWaitingSoundButton(13));
		inv.setItem(14, arenaInstance.getWaitingSoundButton(14));

		// Option to exit
		inv.setItem(17, InventoryItems.exit());

		return inv;
	}

	// Menu to copy game settings
	public static Inventory createCopySettingsInventory(int arena) {
		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena), 54,  Utils.format("&k") +
				Utils.format("&8&lCopy Game Settings"));

		// Options to choose any of the 45 possible arenas
		for (int i = 1; i <= 45; i++) {
			// Check if arena exists, set button accordingly
			if (ArenaManager.getArena(i) == null)
				inv.setItem(i - 1, Utils.createItem(Material.BLACK_CONCRETE,
						Utils.format("&c&lArena " + i  + " not available")));
			else if (i == arena)
				inv.setItem(i - 1, Utils.createItem(Material.GRAY_GLAZED_TERRACOTTA,
						Utils.format("&6&l" + ArenaManager.getArena(i).getName())));
			else
				inv.setItem(i - 1, Utils.createItem(Material.WHITE_CONCRETE,
						Utils.format("&a&lCopy " + ArenaManager.getArena(i).getName())));
		}

		// Option to exit
		inv.setItem(53, InventoryItems.exit());

		return inv;
	}

	// Display player stats
	public static Inventory createPlayerStatsInventory(Main plugin, String name) {
		FileConfiguration playerData = plugin.getPlayerData();

		// Create inventory
		Inventory inv = Bukkit.createInventory(null, 9, Utils.format("&k") +
				Utils.format("&2&l" + name + "'s Stats"));

		// Total kills
		inv.setItem(0, Utils.createItem(Material.DRAGON_HEAD, Utils.format("&4&lTotal Kills: &4" +
				playerData.getInt(name + ".totalKills")), Utils.format("&7Lifetime kill count")));

		// Top kills
		inv.setItem(1, Utils.createItem(Material.ZOMBIE_HEAD, Utils.format("&c&lTop Kills: &c" +
				playerData.getInt(name + ".topKills")), Utils.format("&7Most kills in a game")));

		// Total gems
		inv.setItem(2, Utils.createItem(Material.EMERALD_BLOCK, Utils.format("&2&lTotal Gems: &2" +
				playerData.getInt(name + ".totalGems")), Utils.format("&7Lifetime gems collected")));

		// Top balance
		inv.setItem(3, Utils.createItem(Material.EMERALD, Utils.format("&a&lTop Balance: &a" +
				playerData.getInt(name + ".topBalance")),
				Utils.format("&7Highest gem balance in a game")));

		// Top wave
		inv.setItem(4, Utils.createItem(Material.GOLDEN_SWORD, Utils.format("&3&lTop Wave: &3" +
				playerData.getInt(name + ".topWave")), Utils.BUTTON_FLAGS, null,
				Utils.format("&7Highest completed wave")));

		// Kits
		inv.setItem(6, Utils.createItem(Material.ENDER_CHEST, Utils.format("&9&lKits")));

		// Crystal balance
		inv.setItem(8, Utils.createItem(Material.DIAMOND, Utils.format("&b&lCrystal Balance: &b" +
				playerData.getInt(name + ".crystalBalance"))));

		return inv;
	}

	// Display arena information
	public static Inventory createArenaInfoInventory(Arena arena) {
		// Create inventory
		Inventory inv = Bukkit.createInventory(new InventoryMeta(arena.getArena()), 9, Utils.format("&k") +
				Utils.format("&6&l" + arena.getName() + " Info"));

		// Maximum players
		inv.setItem(1, Utils.createItem(Material.NETHERITE_HELMET,
				Utils.format("&4&lMaximum players: &4" + arena.getMaxPlayers()), Utils.BUTTON_FLAGS, null,
				Utils.format("&7The most players an arena can have")));

		// Minimum players
		inv.setItem(2, Utils.createItem(Material.NETHERITE_BOOTS,
				Utils.format("&2&lMinimum players: &2" + arena.getMinPlayers()), Utils.BUTTON_FLAGS, null,
				Utils.format("&7The least players an arena can have to start")));

		// Wave time limit
		String limit;
		if (arena.getTimeLimit() < 0)
			limit = "Unlimited";
		else limit = arena.getTimeLimit() + " minute(s)";
		inv.setItem(4, Utils.createItem(Material.CLOCK,
				Utils.format("&9&lWave time limit: &9" + limit),
				Utils.format("&7The time limit for each wave before"), Utils.format("&7the game ends")));

//		// Arena records
//		List<String> records = new ArrayList<>();
//		arena.getSortedDescendingRecords().forEach(arenaRecord -> {
//			records.add(Utils.format("&fWave " + arenaRecord.getWave()));
//			for (int i = 0; i < arenaRecord.getPlayers().size() / 4 + 1; i++) {
//				StringBuilder players = new StringBuilder(Utils.format("&7"));
//				if (i * 4 + 4 < arenaRecord.getPlayers().size()) {
//					for (int j = i * 4; j < i * 4 + 4; j++)
//						players.append(arenaRecord.getPlayers().get(j)).append(", ");
//					records.add(Utils.format(players.substring(0, players.length() - 1)));
//				} else {
//					for (int j = i * 4; j < arenaRecord.getPlayers().size(); j++)
//						players.append(arenaRecord.getPlayers().get(j)).append(", ");
//					records.add(Utils.format(players.substring(0, players.length() - 2)));
//				}
//			}
//		});
//		inv.setItem(5, Utils.createItem(Material.GOLDEN_HELMET, Utils.format("&e&lArena Records"), Utils.BUTTON_FLAGS,
//				null, records));

		return inv;
	}

	// Shows sign GUI to name arena
	public static void nameArena(Player player, Arena arena) {
		Location location = player.getLocation();
		location.setY(0);
		BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
		player.sendBlockChange(location, Bukkit.createBlockData(Material.OAK_SIGN));

		TileEntitySign sign = new TileEntitySign();
		sign.setPosition(blockPosition);
		sign.lines[0] = CraftSign.sanitizeLines(new String[]{String.format("Rename Arena %d:", arena.getArena())})[0];
		sign.lines[1] = CraftSign.sanitizeLines(new String[]{Utils.format("===============")})[0];
		sign.lines[3] = CraftSign.sanitizeLines(new String[]{Utils.format("===============")})[0];
		sign.lines[2] = CraftSign.sanitizeLines(new String[]{arena.getName()})[0];
		sign.update();

		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		entityPlayer.playerConnection.sendPacket(sign.getUpdatePacket());
		entityPlayer.playerConnection.sendPacket(new PacketPlayOutOpenSignEditor(blockPosition));
	}

	// Set the pre game hotbar
	public static void setPreGameHotbar(Player player) {
		Inventory inv = player.getInventory();

		inv.setItem(6, InventoryItems.leave());
	}

	// Set a specific iteration of the editor hotbar
	public static void setEditorHotbar(Player player, int index) {
		Inventory inv = player.getInventory();

		inv.setItem(0, InventoryItems.previous());
		for (int i = index; i < index + 6; i++)
			inv.setItem(i - index + 1, InventoryItems.presents()[i % InventoryItems.presents().length]);
		inv.setItem(7, InventoryItems.next());
		inv.setItem(8, InventoryItems.exit());
	}

	// Easy way to get a string for a toggle status
	private static String getToggleStatus(boolean status) {
		String toggle;
		if (status)
			toggle = "&a&lON";
		else toggle = "&c&lOFF";
		return toggle;
	}

	private static void sort(List<ItemStack> list) {
		list.sort(Comparator.comparingInt(itemStack -> {
			List<String> lore = Objects.requireNonNull(itemStack.getItemMeta()).getLore();
			assert lore != null;
			return Integer.parseInt(lore.get(lore.size() - 1).substring(10));
		}));
	}
}
