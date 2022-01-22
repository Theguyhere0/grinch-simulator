package me.theguyhere.grinchsimulator.game.displays;

import me.theguyhere.grinchsimulator.exceptions.InvalidLocationException;
import me.theguyhere.grinchsimulator.game.models.arenas.Arena;
import me.theguyhere.grinchsimulator.game.models.arenas.ArenaRecord;
import me.theguyhere.grinchsimulator.game.models.arenas.ArenaRecordType;
import me.theguyhere.grinchsimulator.tools.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The scoreboard of an Arena.
 */
public class ArenaBoard {
	/** The information for the ArenaBoard.*/
	private final Hologram hologram;
	/** The location of the ArenaBoard.*/
	private final Location location;
	/** The type of records this ArenaBoard holds.*/
	private final ArenaRecordType type;

	public ArenaBoard(@NotNull Location location, Arena arena, ArenaRecordType type) throws InvalidLocationException {
		// Check for null world
		if (location.getWorld() == null)
			throw new InvalidLocationException("Location world cannot be null!");

		// Set location and type
		this.location = location;
		this.type = type;

		// Gather relevant stats
		List<String> info = new ArrayList<>();
		if (type == ArenaRecordType.PRESENTS)
			info.add(Utils.format("&6&l" + arena.getName() + " Present Records"));
		else if (type == ArenaRecordType.HAPPINESS)
			info.add(Utils.format("&6&l" + arena.getName() + " Happiness Records"));

		List<ArenaRecord> records = arena.getSortedDescendingRecords(type);

		if (!records.isEmpty())
			records.forEach(record -> {
				int rank = records.indexOf(record) + 1;
				info.add(rank, Utils.format("&6" + rank + ") &f" + record.getPlayer() + " - &b" +
						record.getValue()));
			});

		// Set hologram
		this.hologram = new Hologram(location.clone().add(0, 2.5, 0), false,
				info.toArray(new String[]{}));
	}

	public Location getLocation() {
		return location;
	}

	public Hologram getHologram() {
		return hologram;
	}

	public ArenaRecordType getType() {
		return type;
	}

	/**
	 * Spawn in the ArenaBoard for every online player.
	 */
	public void displayForOnline() {
		hologram.displayForOnline();
	}

	/**
	 * Spawn in the ArenaBoard for a specific player.
	 * @param player - The player to display the ArenaBoard for.
	 */
	public void displayForPlayer(Player player) {
		hologram.displayForPlayer(player);
	}

	/**
	 * Stop displaying the ArenaBoard for every online player.
	 */
	public void remove() {
		hologram.remove();
	}
}
