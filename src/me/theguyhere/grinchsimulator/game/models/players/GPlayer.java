package me.theguyhere.grinchsimulator.game.models.players;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * A class holding data about players in a Villager Defense game.
 */
public class GPlayer {
    /** UUID of corresponding {@link Player}.*/
    private final UUID player;
    /** Gem balance.*/
    private int gems;
    /** The number of times this player violated arena boundaries.*/
    private int infractions;

    public GPlayer(Player player) {
        this.player = player.getUniqueId();
        gems = 0;
        infractions = 0;
    }

    public UUID getID() {
        return player;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(player);
    }

    public int getGems() {
        return gems;
    }

    public void addGems(int change) {
        gems += change;
    }

    public int incrementInfractions() {
        return ++infractions;
    }

    public void resetInfractions() {
        infractions = 0;
    }
}
