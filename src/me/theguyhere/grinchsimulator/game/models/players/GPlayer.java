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
    /** Presents found.*/
    private int presents;
    /** Happiness stolen.*/
    private int happiness;

    public GPlayer(Player player) {
        this.player = player.getUniqueId();
        presents = 0;
        happiness = 0;
    }

    public UUID getID() {
        return player;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(player);
    }

    public int getPresents() {
        return presents;
    }

    public void addPresents(int change) {
        presents += change;
    }

    public int getHappiness() {
        return happiness;
    }
    
    public void addHappiness(int change) {
        happiness += change;
    }
}
