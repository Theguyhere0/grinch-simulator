package me.theguyhere.grinchsimulator.game.displays;

import me.theguyhere.grinchsimulator.exceptions.InvalidLocationException;
import me.theguyhere.grinchsimulator.game.models.arenas.Arena;
import me.theguyhere.grinchsimulator.game.models.arenas.ArenaStatus;
import me.theguyhere.grinchsimulator.tools.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * The portal to an Arena.
 */
public class Portal {
    /** The NPC for the Portal.*/
    private final NPCCreeper npc;
    /** The information for the Portal.*/
    private final Hologram hologram;
    /** The location of the Portal.*/
    private final Location location;

    public Portal(@NotNull Location location, Arena arena) throws InvalidLocationException {
        // Check for null world
        if (location.getWorld() == null)
            throw new InvalidLocationException("Location world cannot be null!");

        // Get status
        String status;
        if (arena.isClosed())
            status = "&4&lClosed";
        else if (arena.getStatus() == ArenaStatus.ENDING)
            status = "&c&lEnding";
        else if (arena.getStatus() == ArenaStatus.WAITING)
            status = "&5&lWaiting";
        else status = "&a&lIn Game";

        // Get player count color
        String countColor;
        double fillRatio = arena.getPlayers().size() / (double) arena.getMaxPlayers();
        if (fillRatio < .8)
            countColor = "&a";
        else if (fillRatio < 1)
            countColor = "&6";
        else countColor = "&c";

        // Set location, hologram, and npc
        this.location = location;
        this.npc = new NPCCreeper(location);
        this.hologram = new Hologram(location.clone().add(0, 2, 0), false,
                Utils.format("&6&l" + arena.getName()),
                Utils.format(status),
                arena.isClosed() ? "" : Utils.format(
                        "&bPlayers: " + countColor + arena.getPlayers().size() + "&b / " + arena.getMaxPlayers()));
    }

    public Location getLocation() {
        return location;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public NPCCreeper getNpc() {
        return npc;
    }

    /**
     * Spawn in the Portal for every online player.
     */
    public void displayForOnline() {
        hologram.displayForOnline();
        npc.displayForOnline();
    }

    /**
     * Spawn in the Portal for a specific player.
     * @param player - The player to display the Portal for.
     */
    public void displayForPlayer(Player player) {
        hologram.displayForPlayer(player);
        npc.displayForPlayer(player);
    }

    /**
     * Stop displaying the Portal for every online player.
     */
    public void remove() {
        hologram.remove();
        npc.remove();
    }
}
