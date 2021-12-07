package me.theguyhere.grinchsimulator;

import me.theguyhere.grinchsimulator.tools.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Commands implements CommandExecutor {
    Main plugin;

    public Commands(Main plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (label.equalsIgnoreCase("grinch")) {
            FileConfiguration language = plugin.getLanguageData();

            Player player;

            if (sender instanceof Player)
                player = (Player) sender;
            else player = null;

            // No arguments
            if (args.length == 0) {
                if (player != null)
                    player.sendMessage(Utils.notify(language.getString("commandError")));
                else Utils.debugError("Invalid command. Use 'grinch help' for more info.",0);
                return true;
            }
        }
        return false;
    }
}
