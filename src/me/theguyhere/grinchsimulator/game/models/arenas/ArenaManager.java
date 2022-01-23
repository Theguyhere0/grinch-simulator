package me.theguyhere.grinchsimulator.game.models.arenas;

import me.theguyhere.grinchsimulator.Main;
import me.theguyhere.grinchsimulator.exceptions.InvalidLocationException;
import me.theguyhere.grinchsimulator.game.displays.InfoBoard;
import me.theguyhere.grinchsimulator.game.displays.Leaderboard;
import me.theguyhere.grinchsimulator.game.displays.Portal;
import me.theguyhere.grinchsimulator.game.models.Tasks;
import me.theguyhere.grinchsimulator.game.models.players.GPlayer;
import me.theguyhere.grinchsimulator.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

public class ArenaManager {
	private final Main plugin;

	// Tracks arenas, info boards, and leaderboards for the game
	private static final Arena[] arenas = new Arena[45];
	public static InfoBoard[] infoBoards = new InfoBoard[8];
	public static Map<String, Leaderboard> leaderboards = new HashMap<>();

	private static Location lobby;

	public ArenaManager(Main plugin) {
		this.plugin = plugin;

		// Gather arena data
		Objects.requireNonNull(plugin.getArenaData().getConfigurationSection("")).getKeys(false)
				.forEach(path -> {
			if (path.charAt(0) == 'a' && path.length() < 4)
				arenas[Integer.parseInt(path.substring(1)) - 1] = new Arena(plugin,
						Integer.parseInt(path.substring(1)),
						new Tasks(plugin, Integer.parseInt(path.substring(1))));
		});
		try {
			Objects.requireNonNull(plugin.getArenaData().getConfigurationSection("infoBoard")).getKeys(false)
					.forEach(path -> {
						try {
							Location location = Utils.getConfigLocationNoPitch(plugin, "infoBoard." + path);
							if (location != null)
								infoBoards[Integer.parseInt(path) - 1] = new InfoBoard(location, plugin);
						} catch (InvalidLocationException ignored) {
						}
					});
		} catch (Exception ignored) {
		}
		try {
			Objects.requireNonNull(plugin.getArenaData().getConfigurationSection("leaderboard")).getKeys(false)
					.forEach(path -> {
						try {
							Location location = Utils.getConfigLocationNoPitch(plugin, "leaderboard." + path);
							if (location != null)
								leaderboards.put(path, new Leaderboard(path, plugin));
						} catch (InvalidLocationException ignored) {
						}
					});
		} catch (Exception ignored) {
		}
		setLobby(Utils.getConfigLocation(plugin, "lobby"));
	}

	public static Arena getArena(int id) {
		return arenas[id - 1];
	}

	public static Arena[] getArenas() {
		return arenas;
	}

	public static void setArena(int id, Arena arena) {
		arenas[id - 1] = arena;
	}

	public void checkArenas() {
		Arrays.stream(arenas).filter(Objects::nonNull).forEach(Arena::checkClose);
	}

	/**
	 * Creates a scoreboard for a player.
	 * @param player Player to give a scoreboard.
	 */
	public static void createBoard(GPlayer player) {
		// Create scoreboard manager and check that it isn't null
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		assert manager != null;

		Scoreboard board = manager.getNewScoreboard();
		Arena arena = Arrays.stream(arenas).filter(Objects::nonNull).filter(a -> a.hasPlayer(player)).toList().get(0);
		List<GPlayer> presentLead = arena.getPlayers().stream()
				.sorted(Comparator.comparingInt(GPlayer::getPresents).reversed()).toList();
		List<GPlayer> happyLead = arena.getPlayers().stream()
				.sorted(Comparator.comparingInt(GPlayer::getHappiness).reversed()).toList();

		// Create score board
		Objective obj = board.registerNewObjective("GrinchSimulator", "dummy",
				Utils.format("&6&l   " + arena.getName() + "  "));
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		Score score9 = obj.getScore(Utils.format("&cPresents Left: " + arena.getPresentsLeft()));
		score9.setScore(9);
		Score score8 = obj.getScore(Utils.format("&4Happiness Left: " + arena.getHappinessLeft()));
		score8.setScore(8);
		Score score7 = obj.getScore(" ");
		score7.setScore(7);
		Score score6 = obj.getScore(Utils.format("&a&lPresents Poached: " + player.getPresents()));
		score6.setScore(6);
		Score score5 = obj.getScore(Utils.format("&aPresents Rank: " + (presentLead.indexOf(player) + 1)));
		score5.setScore(5);
		Score score4 = obj.getScore(Utils.format("&2&lHappiness Harvested: " + player.getHappiness()));
		score4.setScore(4);
		Score score3 = obj.getScore(Utils.format("&2Happiness Rank: " + (happyLead.indexOf(player) + 1)));
		score3.setScore(3);
		Score score2 = obj.getScore("");
		score2.setScore(2);
		Score score1 = obj.getScore(Utils.format(String.format("&eGifted Grinch: %s(&a%d&e)",
				presentLead.get(0).getPlayer().getName(), presentLead.get(0).getPresents())));
		score1.setScore(1);
		Score score = obj.getScore(Utils.format(String.format("&6Happiest Grinch: %s(&2%d&6)",
				happyLead.get(0).getPlayer().getName(), happyLead.get(0).getHappiness())));
		score.setScore(0);

		player.getPlayer().setScoreboard(board);
	}

	public static Location getLobby() {
		return lobby;
	}

	public static void setLobby(Location lobby) {
		ArenaManager.lobby = lobby;
	}

	public void reloadLobby() {
		lobby = Utils.getConfigLocation(plugin, "lobby");
	}

	/**
	 * Creates a new info board at the given location and deletes the old info board.
	 * @param location - New location.
	 */
	public void setInfoBoard(Location location, int num) {
		// Save config location
		Utils.setConfigurationLocation(plugin, "infoBoard." + num, location);

		// Recreate the info board
		refreshInfoBoard(num);
	}

	/**
	 * Recreates the info board in game based on the location in the arena file.
	 */
	public void refreshInfoBoard(int num) {
		int index = num - 1;

		// Delete old board if needed
		if (infoBoards[index] != null)
			infoBoards[index].remove();

		try {
			// Create a new board and display it
			infoBoards[index] = new InfoBoard(
					Objects.requireNonNull(Utils.getConfigLocationNoPitch(plugin, "infoBoard." + num)),
					plugin);
			infoBoards[index].displayForOnline();
		} catch (Exception e) {
			Utils.debugError("Invalid location for info board " + num, 1);
			Utils.debugInfo("Info board location data may be corrupt. If data cannot be manually corrected in " +
					"arenaData.yml, please delete the location data for info board " + num + ".", 1);
		}
	}

	/**
	 * Centers the info board location along the x and z axis.
	 */
	public void centerInfoBoard(int num) {
		// Center the location
		Utils.centerConfigLocation(plugin, "infoBoard." + num);

		// Recreate the portal
		refreshInfoBoard(num);
	}

	/**
	 * Removes the info board from the game and from the arena file.
	 */
	public void removeInfoBoard(int num) {
		int index = num - 1;
		if (infoBoards[index] != null) {
			infoBoards[index].remove();
			infoBoards[index] = null;
		}
		Utils.setConfigurationLocation(plugin, "infoBoard." + num, null);
	}

	/**
	 * Creates a new leaderboard at the given location and deletes the old leaderboard.
	 * @param location - New location.
	 */
	public void setLeaderboard(Location location, String type) {
		// Save config location
		Utils.setConfigurationLocation(plugin, "leaderboard." + type, location);

		// Recreate the leaderboard
		refreshLeaderboard(type);
	}

	/**
	 * Recreates the leaderboard in game based on the location in the arena file.
	 */
	public void refreshLeaderboard(String type) {
		// Delete old board if needed
		if (leaderboards.get(type) != null) {
			leaderboards.get(type).remove();
			leaderboards.remove(type);
		}

		try {
			// Create a new board and display it
			leaderboards.put(type, new Leaderboard(type, plugin));
			leaderboards.get(type).displayForOnline();
		} catch (Exception e) {
			Utils.debugError("Invalid location for leaderboard " + type, 1);
			Utils.debugInfo("Leaderboard location data may be corrupt. If data cannot be manually corrected in " +
					"arenaData.yml, please delete the location data for leaderboard " + type + ".", 1);
		}
	}

	/**
	 * Centers the leaderboard location along the x and z axis.
	 */
	public void centerLeaderboard(String type) {
		// Center the location
		Utils.centerConfigLocation(plugin, "leaderboard." + type);

		// Recreate the leaderboard
		refreshLeaderboard(type);
	}

	/**
	 * Removes the leaderboard from the game and from the arena file.
	 */
	public void removeLeaderboard(String type) {
		if (leaderboards.get(type) != null) {
			leaderboards.get(type).remove();
			leaderboards.remove(type);
		}
		Utils.setConfigurationLocation(plugin, "leaderboard." + type, null);
	}

	/**
	 * Display all portals to a player.
	 * @param player - The player to display all portals to.
	 */
	public static void displayAllPortals(Player player) {
		Arrays.stream(arenas).filter(Objects::nonNull).map(Arena::getPortal)
				.filter(Objects::nonNull).forEach(portal -> portal.displayForPlayer(player));
	}

	/**
	 * Display all arena boards to a player.
	 * @param player - The player to display all arena boards to.
	 */
	public static void displayAllArenaBoards(Player player) {
		Arrays.stream(arenas).filter(Objects::nonNull).map(Arena::getPresentLeaders)
				.filter(Objects::nonNull).forEach(arenaBoard -> arenaBoard.displayForPlayer(player));
		Arrays.stream(arenas).filter(Objects::nonNull).map(Arena::getHappyLeaders)
				.filter(Objects::nonNull).forEach(arenaBoard -> arenaBoard.displayForPlayer(player));
	}

	/**
	 * Display all info boards to a player.
	 * @param player - The player to display all info boards to.
	 */
	public static void displayAllInfoBoards(Player player) {
		Arrays.stream(infoBoards).filter(Objects::nonNull).forEach(infoBoard -> infoBoard.displayForPlayer(player));
	}

	/**
	 * Display all leaderboards to a player.
	 * @param player - The player to display all leaderboards to.
	 */
	public static void displayAllLeaderboards(Player player) {
		leaderboards.forEach((type, board) -> board.displayForPlayer(player));
	}

	/**
	 * Display everything displayable to a player.
	 * @param player - The player to display everything to.
	 */
	public static void displayEverything(Player player) {
		displayAllPortals(player);
		displayAllArenaBoards(player);
		displayAllInfoBoards(player);
		displayAllLeaderboards(player);
	}

	/**
	 * Refresh the portal of every arena.
	 */
	public static void refreshPortals() {
		Arrays.stream(arenas).filter(Objects::nonNull).forEach(Arena::refreshPortal);
	}

	/**
	 * Refresh the arena board of every arena.
	 */
	public static void refreshArenaBoards() {
		Arrays.stream(arenas).filter(Objects::nonNull).forEach(Arena::refreshArenaBoards);
	}

	/**
	 * Refresh every info board.
	 */
	public void refreshInfoBoards() {
		for (int i = 1; i <= infoBoards.length; i++) {
			refreshInfoBoard(i);
		}
	}

	/**
	 * Refresh every leaderboard.
	 */
	public void refreshLeaderboards() {
		List<String> types = new ArrayList<>(leaderboards.keySet());
		types.forEach(this::refreshLeaderboard);
	}

	/**
	 * Refresh all holographics.
	 */
	public void refreshAll() {
		refreshPortals();
		refreshArenaBoards();
		refreshInfoBoards();
		refreshLeaderboards();
	}

	public static void removePortals() {
        Arrays.stream(arenas).filter(Objects::nonNull).map(Arena::getPortal).filter(Objects::nonNull)
				.forEach(Portal::remove);
    }
}
