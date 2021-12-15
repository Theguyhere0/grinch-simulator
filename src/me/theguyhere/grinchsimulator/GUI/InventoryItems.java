package me.theguyhere.grinchsimulator.GUI;

import me.theguyhere.grinchsimulator.tools.Utils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class InventoryItems {
    //	"No" button
    public static ItemStack no() {
        return Utils.createItem(Material.RED_CONCRETE, Utils.format("&c&lNO"));
    }

    //	"Yes" button
    public static ItemStack yes() {
        return Utils.createItem(Material.LIME_CONCRETE, Utils.format("&a&lYES"));
    }

    //	"Exit" button
    public static ItemStack exit() {
        return Utils.createItem(Material.BARRIER, Utils.format("&c&lEXIT"));
    }

    //	"Remove x" button
    public static ItemStack remove(String x) {
        return Utils.createItem(Material.LAVA_BUCKET, Utils.format("&4&lREMOVE " + x));
    }

    //	"Create x" button
    public static ItemStack create(String x) {
        return Utils.createItem(Material.END_PORTAL_FRAME, Utils.format("&a&lCreate " + x));
    }

    //	"Relocate x" button
    public static ItemStack relocate(String x) {
        return Utils.createItem(Material.END_PORTAL_FRAME, Utils.format("&a&lRelocate " + x));
    }

    //	"Teleport to x" button
    public static ItemStack teleport(String x) {
        return Utils.createItem(Material.ENDER_PEARL, Utils.format("&9&lTeleport to " + x));
    }

    //	"Center x" button
    public static ItemStack center(String x) {
        return Utils.createItem(Material.TARGET, Utils.format("&f&lCenter " + x),
                Utils.format("&7Center the x and z coordinates"));
    }

    // "Previous" button
    public static ItemStack previous() {
        return Utils.rename(
                Utils.getPlayerHeadByBase("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdhZWU5YTc1YmYwZGY3ODk3MTgzMDE1Y2NhMGIyYTdkNzU1YzYzMzg4ZmYwMTc1MmQ1ZjQ0MTlmYzY0NSJ9fX0="),
                Utils.format("&f&lPrevious"));
    }

    // "Next" button
    public static ItemStack next() {
        return Utils.rename(
                Utils.getPlayerHeadByBase("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTYzMzlmZjJlNTM0MmJhMThiZGM0OGE5OWNjYTY1ZDEyM2NlNzgxZDg3ODI3MmY5ZDk2NGVhZDNiOGFkMzcwIn19fQ=="),
                Utils.format("&f&lNext"));
    }

    // Standard game items
    public static @NotNull ItemStack leave() {
        HashMap<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.DURABILITY, 1);

        ItemStack item = Utils.createItem(Material.BARRIER, Utils.format("&c&lLEAVE"),
                Utils.HIDE_ENCHANT_FLAGS, enchants);

        return item == null ? new ItemStack(Material.AIR) : item;
    }
}
