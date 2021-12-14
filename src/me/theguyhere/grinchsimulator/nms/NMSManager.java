package me.theguyhere.grinchsimulator.nms;

import me.theguyhere.grinchsimulator.nms.v1_16_R3.EntityNMSArmorStand;
import me.theguyhere.grinchsimulator.nms.v1_16_R3.EntityNMSCreeper;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * A manager class to retrieve version-agnostic Bukkit entities based on the detected server Minecraft version.
 */
public class NMSManager {
    /**
     * Retrieves a version-agnostic Bukkit entity of the armor stand based on the detected server Minecraft version.
     *
     * @param text - The text to be displayed by the armor stand.
     * @param location - The location of the armor stand.
     * @return - Bukkit entity of the armor stand.
     */
    public static Entity getArmorStand(String text, @NotNull Location location) {
        switch (NMSVersion.getCurrent()) {
            case v1_16_R2:
                return null;
            case v1_16_R3:
                return (new EntityNMSArmorStand(location, text)).getBukkitEntity();
            default:
                return null;
        }
    }

    /**
     * Retrieves a version-agnostic Bukkit entity of the creeper based on the detected server Minecraft version.
     *
     * @param location - The location of the creeper.
     * @return - Bukkit entity of the creeper.
     */
    public static Entity getCreeper(@NotNull Location location) {
        switch (NMSVersion.getCurrent()) {
            case v1_16_R2:
                return null;
            case v1_16_R3:
                return (new EntityNMSCreeper(location)).getBukkitEntity();
            default:
                return null;
        }
    }
}
