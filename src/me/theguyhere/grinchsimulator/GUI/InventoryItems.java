package me.theguyhere.grinchsimulator.GUI;

import me.theguyhere.grinchsimulator.game.models.presents.Presents;
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
        return Utils.relore(
                Utils.rename(
                Utils.getPlayerHeadByBase("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjg0ZjU5NzEzMWJiZTI1ZGMwNThhZjg4OGNiMjk4MzFmNzk1OTliYzY3Yzk1YzgwMjkyNWNlNGFmYmEzMzJmYyJ9fX0="),
                Utils.format("&f&lPrevious")));
    }

    // "Next" button
    public static ItemStack next() {
        return Utils.relore(
                Utils.rename(
                Utils.getPlayerHeadByBase("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTYzMzlmZjJlNTM0MmJhMThiZGM0OGE5OWNjYTY1ZDEyM2NlNzgxZDg3ODI3MmY5ZDk2NGVhZDNiOGFkMzcwIn19fQ=="),
                Utils.format("&f&lNext")));
    }

    // Standard game items
    public static @NotNull ItemStack leave() {
        HashMap<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.DURABILITY, 1);

        ItemStack item = Utils.createItem(Material.BARRIER, Utils.format("&c&lLEAVE"),
                Utils.HIDE_ENCHANT_FLAGS, enchants);

        return item == null ? new ItemStack(Material.AIR) : item;
    }

    // Editor item array
    public static @NotNull ItemStack[] presents() {
        return new ItemStack[]{
                Utils.relore(Utils.rename(Presents.WOOD_PRESENT, Utils.format("&5&lWood Present")),
                        Utils.format("&00")),
                Utils.relore(Utils.rename(Presents.STONE_PRESENT, Utils.format("&8&lStone Present")),
                        Utils.format("&01")),
                Utils.relore(Utils.rename(Presents.IRON_PRESENT, Utils.format("&7&lIron Present")),
                        Utils.format("&02")),
                Utils.relore(Utils.rename(Presents.COPPER_PRESENT, Utils.format("&3&lCopper Present")),
                        Utils.format("&03")),
                Utils.relore(Utils.rename(Presents.GOLD_PRESENT, Utils.format("&6&lGold Present")),
                        Utils.format("&04")),
                Utils.relore(Utils.rename(Presents.DIAMOND_PRESENT, Utils.format("&b&lDiamond Present")),
                        Utils.format("&05")),
                Utils.relore(Utils.rename(Presents.EMERALD_PRESENT, Utils.format("&a&lEmerald Present")),
                        Utils.format("&06")),
                Utils.relore(Utils.rename(Presents.NETHERITE_PRESENT, Utils.format("&7&lNetherite Present")),
                        Utils.format("&07")),
                Utils.relore(Utils.rename(Presents.BLACK_PRESENT, Utils.format("&8&lBlack Present")),
                        Utils.format("&08")),
                Utils.relore(Utils.rename(Presents.BROWN_PRESENT, Utils.format("&5&lBrown Present")),
                        Utils.format("&09")),
                Utils.relore(Utils.rename(Presents.RED_PRESENT, Utils.format("&c&lRed Present")),
                        Utils.format("&010")),
                Utils.relore(Utils.rename(Presents.ORANGE_PRESENT, Utils.format("&6&lOrange Present")),
                        Utils.format("&011")),
                Utils.relore(Utils.rename(Presents.YELLOW_PRESENT, Utils.format("&e&lYellow Present")),
                        Utils.format("&012")),
                Utils.relore(Utils.rename(Presents.GREEN_PRESENT, Utils.format("&2&lGreen Present")),
                        Utils.format("&013")),
                Utils.relore(Utils.rename(Presents.CYAN_PRESENT, Utils.format("&3&lCyan Present")),
                        Utils.format("&014")),
                Utils.relore(Utils.rename(Presents.BLUE_PRESENT, Utils.format("&9&lBlue Present")),
                        Utils.format("&015")),
                Utils.relore(Utils.rename(Presents.PURPLE_PRESENT, Utils.format("&5&lPurple Present")),
                        Utils.format("&016")),
                Utils.relore(Utils.rename(Presents.PINK_PRESENT, Utils.format("&d&lPink Present")),
                        Utils.format("&017")),
                Utils.relore(Utils.rename(Presents.WHITE_PRESENT, Utils.format("&f&lWhite Present")),
                        Utils.format("&018")),
        };
    }
}
