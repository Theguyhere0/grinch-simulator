package me.theguyhere.grinchsimulator.exceptions;

import me.theguyhere.grinchsimulator.game.models.players.GPlayer;

/**
 * An exception thrown when a {@link GPlayer} cannot be found.
 */
public class PlayerNotFoundException extends Exception {
    public PlayerNotFoundException(String message) {
        super(message);
    }

    public PlayerNotFoundException() {
        super();
    }
}
