package me.theguyhere.grinchsimulator.game.models;

import me.theguyhere.grinchsimulator.Main;
import me.theguyhere.grinchsimulator.events.GameEndEvent;
import me.theguyhere.grinchsimulator.events.GameStartEvent;
import me.theguyhere.grinchsimulator.events.LeaveArenaEvent;
import me.theguyhere.grinchsimulator.game.models.arenas.Arena;
import me.theguyhere.grinchsimulator.game.models.arenas.ArenaManager;
import me.theguyhere.grinchsimulator.game.models.arenas.ArenaStatus;
import me.theguyhere.grinchsimulator.game.models.players.GPlayer;
import me.theguyhere.grinchsimulator.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("SpellCheckingInspection")
public class Tasks {
	private final Main plugin;
	private final int arena;
	/** Maps runnables to ID of the currently running runnable.*/
	private final Map<Runnable, Integer> tasks = new HashMap<>();

	public Tasks(Main plugin, int arena) {
		this.plugin = plugin;
		this.arena = arena;
	}

	public Map<Runnable, Integer> getTasks() {
		return tasks;
	}

	// Waiting for enough players message
	public final Runnable waiting = new Runnable() {
		@Override
		public void run() {
			ArenaManager.getArena(arena).getPlayers().forEach(player ->
				player.getPlayer().sendMessage(Utils.notify(plugin.getLanguageData().getString("waiting"))));
			Utils.debugInfo("Arena " + arena + " is currently waiting for players to start.", 2);
		}
	};

	// 2 minute warning
	public final Runnable min2 = new Runnable() {
		@Override
		public void run() {
			try {
				ArenaManager.getArena(arena).getPlayers().forEach(player ->
						player.getPlayer().sendMessage(Utils.notify(String.format(
								Objects.requireNonNull(plugin.getLanguageData().getString("minutesLeft")), 2))));
			} catch (Exception e) {
				Utils.debugError("The key 'minutesLeft' is missing or corrupt in the active language file",
						1);
			}
			Utils.debugInfo("Arena " + arena + " is starting in 2 minutes.", 2);
		}
	};

	// 1 minute warning
	public final Runnable min1 = new Runnable() {
		@Override
		public void run() {
			try {
				ArenaManager.getArena(arena).getPlayers().forEach(player ->
						player.getPlayer().sendMessage(Utils.notify(String.format(
								Objects.requireNonNull(plugin.getLanguageData().getString("minutesLeft")), 1))));
			} catch (Exception e) {
				Utils.debugError("The key 'minutesLeft' is missing or corrupt in the active language file",
						1);
			}
			Utils.debugInfo("Arena " + arena + " is starting in 1 minute.", 2);
		}
	};

	// 30 second warning
	public final Runnable sec30 = new Runnable() {
		@Override
		public void run() {
			try {
				ArenaManager.getArena(arena).getPlayers().forEach(player ->
						player.getPlayer().sendMessage(Utils.notify(String.format(
								Objects.requireNonNull(plugin.getLanguageData().getString("secondsLeft")), 30))));
			} catch (Exception e) {
				Utils.debugError("The key 'secondsLeft' is missing or corrupt in the active language file",
						1);
			}
			Utils.debugInfo("Arena " + arena + " is starting in 30 seconds.", 2);
		}
	};

	// 10 second warning
	public final Runnable sec10 = new Runnable() {
		@Override
		public void run() {
			try {
				ArenaManager.getArena(arena).getPlayers().forEach(player ->
						player.getPlayer().sendMessage(Utils.notify(String.format(
								Objects.requireNonNull(plugin.getLanguageData().getString("secondsLeft")), 10))));
			} catch (Exception e) {
				Utils.debugError("The key 'secondsLeft' is missing or corrupt in the active language file",
						1);
			}
			Utils.debugInfo("Arena " + arena + " is starting in 10 seconds.", 2);
		}
	};

	// 10 second warning when full
	public final Runnable full10 = new Runnable() {
		@Override
		public void run() {
			try {
				ArenaManager.getArena(arena).getPlayers().forEach(player -> {
					player.getPlayer().sendMessage(Utils.notify(plugin.getLanguageData().getString("maxCapacity")));
					player.getPlayer().sendMessage(Utils.notify(String.format(
							Objects.requireNonNull(plugin.getLanguageData().getString("secondsLeft")), 10)));
				});
			} catch (Exception e) {
				Utils.debugError("The key 'secondsLeft' is missing or corrupt in the active language file",
						1);
			}
			Utils.debugInfo("Arena " + arena + " is full and is starting in 10 seconds.", 2);
		}

	};

	// 5 second warning
	public final Runnable sec5 = new Runnable() {
		@Override
		public void run() {
			try {
				ArenaManager.getArena(arena).getPlayers().forEach(player ->
						player.getPlayer().sendMessage(Utils.notify(String.format(
								Objects.requireNonNull(plugin.getLanguageData().getString("secondsLeft")), 5))));
			} catch (Exception e) {
				Utils.debugError("The key 'secondsLeft' is missing or corrupt in the active language file",
						1);
			}
			Utils.debugInfo("Arena " + arena + " is starting in 5 seconds.", 2);

		}
	};

	// Start actual game
	public final Runnable start = new Runnable() {

		@Override
		public void run() {
			Arena arenaInstance = ArenaManager.getArena(arena);

			// Set arena to active
			arenaInstance.setStatus(ArenaStatus.ACTIVE);

			// Teleport players to arena if waiting room exists
			if (arenaInstance.getWaitingRoom() != null) {
				for (GPlayer gPlayer : arenaInstance.getPlayers()) {
					Utils.teleAdventure(gPlayer.getPlayer(), arenaInstance.getPlayerSpawn());
				}
			}

			// Stop waiting sound
			if (arenaInstance.getWaitingSound() != null)
				arenaInstance.getPlayers().forEach(player ->
						player.getPlayer().stopSound(arenaInstance.getWaitingSound()));

			// Trigger GameStartEvent
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
					Bukkit.getPluginManager().callEvent(new GameStartEvent(arenaInstance)));

			// Debug message to console
			Utils.debugInfo("Arena " + arena + " is starting.", 2);
		}
	};

	// Reset the arena
	public final Runnable reset = new Runnable() {
		@Override
		public void run() {
			Arena arenaInstance = ArenaManager.getArena(arena);

			// Update data
			arenaInstance.setStatus(ArenaStatus.WAITING);

			// Remove players from the arena
			arenaInstance.getPlayers().forEach(player ->
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
							Bukkit.getPluginManager().callEvent(new LeaveArenaEvent(player.getPlayer()))));

			// Refresh portal
			arenaInstance.refreshPortal();

			// Reset presents
			arenaInstance.resetPresents();
			arenaInstance.returnPresents();

			// Debug message to console
			Utils.debugInfo("Arena " + arena + " is resetting.", 2);
		}
	};

	// Update active player scoreboards
	public final Runnable updateBoards = new Runnable() {
		@Override
		public void run() {
			ArenaManager.getArena(arena).getPlayers().forEach(ArenaManager::createBoard);
		}
	};

	// Update time limit bar
	public final Runnable updateBar = new Runnable() {
		double progress = 1;
		double time;
		boolean messageSent;
		Arena arenaInstance;


		@Override
		public void run() {
			arenaInstance = ArenaManager.getArena(arena);

			// Add time limit bar if it doesn't exist
			if (arenaInstance.getTimeLimitBar() == null) {
				progress = 1;
				arenaInstance.startTimeLimitBar();
				arenaInstance.getPlayers().forEach(gPlayer ->
						arenaInstance.addPlayerToTimeLimitBar(gPlayer.getPlayer()));
				time = 1d / Utils.minutesToSeconds(arenaInstance.getTimeLimit());
				messageSent = false;

				// Debug message to console
				Utils.debugInfo("Adding time limit bar to Arena " + arena, 2);
			}

			else {
				// Trigger wave end event
				if (progress <= 0) {
					progress = 0;
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
							Bukkit.getPluginManager().callEvent(new GameEndEvent(arenaInstance)));
				}

				// Decrement time limit bar
				else {
					if (progress <= time * Utils.minutesToSeconds(1)) {
						arenaInstance.updateTimeLimitBar(BarColor.RED, progress);
						if (!messageSent) {
							// Send warning
							arenaInstance.getPlayers().forEach(player ->
									player.getPlayer().sendTitle(Utils.format(
											plugin.getLanguageData().getString("minuteWarning")), null,
											Utils.secondsToTicks(.5), Utils.secondsToTicks(1.5),
											Utils.secondsToTicks(.5)));

							messageSent = true;
						}
					} else arenaInstance.updateTimeLimitBar(progress);
					progress -= time;
				}
			}
		}
	};
}
