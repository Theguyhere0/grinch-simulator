package me.theguyhere.grinchsimulator.game.displays;

import me.theguyhere.grinchsimulator.Main;
import me.theguyhere.grinchsimulator.exceptions.InvalidLocationException;
import me.theguyhere.grinchsimulator.tools.Utils;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InfoBoard {
	/** The information for the InfoBoard.*/
	private final Hologram hologram;
	/** The location of the InfoBoard.*/
	private final Location location;

	public InfoBoard(@NotNull Location location, Main plugin) throws InvalidLocationException {
		// Check for null world
		if (location.getWorld() == null)
			throw new InvalidLocationException("Location world cannot be null!");

		// Gather info text
		FileConfiguration language = plugin.getLanguageData();
		String[] text = {Utils.format(language.getString("info1")),
				Utils.format(language.getString("info2")),
				Utils.format(language.getString("info3")),
				Utils.format(language.getString("info4")),
				Utils.format(language.getString("info5"))};

		// Set location and hologram
		this.location = location;
		this.hologram = new Hologram(location.clone().add(0, .5, 0), text);
	}

	public Location getLocation() {
		return location;
	}

	public Hologram getHologram() {
		return hologram;
	}

	/**
	 * Spawn in the InfoBoard for every online player.
	 */
	public void displayForOnline() {
		hologram.displayForOnline();
	}

	/**
	 * Spawn in the InfoBoard for a specific player.
	 * @param player - The player to display the InfoBoard for.
	 */
	public void displayForPlayer(Player player) {
		hologram.displayForPlayer(player);
	}

	/**
	 * Stop displaying the InfoBoard for every online player.
	 */
	public void remove() {
		hologram.remove();
	}
}
