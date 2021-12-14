package me.theguyhere.grinchsimulator.game.models;

import me.theguyhere.grinchsimulator.tools.Utils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class GameItems {
	private static final boolean[] FLAGS = {false, false};

	// Standard game items
	public static @NotNull ItemStack leave() {
		HashMap<Enchantment, Integer> enchants = new HashMap<>();
		enchants.put(Enchantment.DURABILITY, 1);

		ItemStack item = Utils.createItem(Material.BARRIER, Utils.format("&c&lLEAVE"),
				Utils.HIDE_ENCHANT_FLAGS, enchants);

		return item == null ? new ItemStack(Material.AIR) : item;
	}
}
