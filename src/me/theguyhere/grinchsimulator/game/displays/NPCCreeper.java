package me.theguyhere.grinchsimulator.game.displays;

import me.theguyhere.grinchsimulator.exceptions.EntitySpawnPacketException;
import me.theguyhere.grinchsimulator.exceptions.InvalidLocationException;
import me.theguyhere.grinchsimulator.nms.NMSManager;
import me.theguyhere.grinchsimulator.packets.PacketManager;
import me.theguyhere.grinchsimulator.tools.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A creeper NPC.
 */
public class NPCCreeper {
    /** The location of the NPCCreeper.*/
    private final Location location;
    /** The creeper entity used to create the NPCCreeper.*/
    private final Entity creeper;

    public NPCCreeper(@NotNull Location location) throws InvalidLocationException {
        // Check for null world
        if (location.getWorld() == null)
            throw new InvalidLocationException("Location world cannot be null!");

        // Set location and creeper
        this.location = location;
        creeper = NMSManager.getCreeper(location);
    }

    public Location getLocation() {
        return location;
    }

    public Entity getCreeper() {
        return creeper;
    }

    /**
     * Spawn in the NPCCreeper for every online player.
     */
    public void displayForOnline() {
        try {
            PacketManager.spawnEntityLivingForOnline(creeper);
            PacketManager.entityHeadRotationForOnline(creeper, location.getYaw());
        } catch (EntitySpawnPacketException e) {
            Utils.debugError(e.getMessage(), 1);
        }
    }

    /**
     * Spawn in the NPCCreeper for a specific player.
     * @param player - The player to display the NPCCreeper for.
     */
    public void displayForPlayer(Player player) {
        try {
            PacketManager.spawnEntityLivingForPlayer(creeper, player);
            PacketManager.entityHeadRotationForPlayer(creeper, location.getYaw(), player);
        } catch (EntitySpawnPacketException e) {
            Utils.debugError(e.getMessage(), 1);
        }
    }

    /**
     * Stop displaying the NPCCreeper for every online player.
     */
    public void remove() {
        PacketManager.destroyEntityForOnline(creeper);
    }
}
