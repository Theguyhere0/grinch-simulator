package me.theguyhere.grinchsimulator.game.displays;

import me.theguyhere.grinchsimulator.Main;
import me.theguyhere.grinchsimulator.exceptions.InvalidLocationException;
import me.theguyhere.grinchsimulator.tools.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Leaderboard {
	/** The information for the Leaderboard.*/
	private final Hologram hologram;
	/** The location of the Leaderboard.*/
	private final Location location;

	public Leaderboard(@NotNull String type, Main plugin) throws InvalidLocationException {
		Location location = Objects.requireNonNull(Utils.getConfigLocationNoPitch(plugin, "leaderboard." + type));
		// Check for null world
		if (location.getWorld() == null)
			throw new InvalidLocationException("Location world cannot be null!");

		// Gather info text
		List<String> info = new ArrayList<>();
		Map<String, Integer> mapping = new HashMap<>();

		// Determine leaderboard title
		switch (type) {
			case "totalPresents" -> info.add(Utils.format("&d&lTotal Presents Leaderboard"));
			case "topPresents" -> info.add(Utils.format("&c&lTop Presents Leaderboard"));
			case "totalHappiness" -> info.add(Utils.format("&e&lTotal Happiness Leaderboard"));
			case "topHappiness" -> info.add(Utils.format("&a&lTop Happiness Leaderboard"));
			default -> info.add("");
		}

		// Gather relevant stats
		for (String key : Objects.requireNonNull(plugin.getPlayerData().getConfigurationSection(""))
				.getKeys(false)) {
			if (!key.equals("logger") && plugin.getPlayerData().contains(key + "." + type))
				mapping.put(key, plugin.getPlayerData().getInt(key + "." + type));
		}

		// Put names and values into the leaderboard
		mapping.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(10)
				.forEachOrdered(map -> info.add(map.getKey() + " - &b" + map.getValue()));

		for (int i = 1; i < info.size(); i++)
			info.set(i, Utils.format("&6" + i + ") &f" + info.get(i)));

		// Set location and hologram
		this.location = location;
		this.hologram = info.get(0).isEmpty() ? null : new Hologram(location.clone().add(0, 2.5, 0),
				false, info.toArray(new String[]{}));
	}

	public Location getLocation() {
		return location;
	}

	public Hologram getHologram() {
		return hologram;
	}

	/**
	 * Spawn in the Leaderboard for every online player.
	 */
	public void displayForOnline() {
		hologram.displayForOnline();
	}

	/**
	 * Spawn in the Leaderboard for a specific player.
	 * @param player - The player to display the Leaderboard for.
	 */
	public void displayForPlayer(Player player) {
		hologram.displayForPlayer(player);
	}

	/**
	 * Stop displaying the Leaderboard for every online player.
	 */
	public void remove() {
		hologram.remove();
	}
}
