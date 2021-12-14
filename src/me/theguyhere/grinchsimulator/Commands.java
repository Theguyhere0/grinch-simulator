package me.theguyhere.grinchsimulator;

import me.theguyhere.grinchsimulator.GUI.Inventories;
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

            // Admin panel
            if (args[0].equalsIgnoreCase("admin")) {
                // Check for player executing command
                if (player == null) {
                    sender.sendMessage("Bad console!");
                    return true;
                }

                // Check for permission to use the command
                if (!player.hasPermission("grinch.use")) {
                    player.sendMessage(Utils.notify(language.getString("permissionError")));
                    return true;
                }

                player.openInventory(Inventories.createArenasInventory());
                return true;
            }

            // Change plugin debug level
            if (args[0].equalsIgnoreCase("debug")) {
                // Check for permission to use the command
                if (player != null && !player.hasPermission("grinch.admin")) {
                    player.sendMessage(Utils.notify(language.getString("permissionError")));
                    return true;
                }

                // Check for correct format
                if (args.length != 2) {
                    if (player != null)
                        player.sendMessage(Utils.notify("&cCommand format: /grinch debug [debug level (0-3)]"));
                    else Utils.debugError("Command format: /grinch debug [debug level (0-3)]", 0);
                    return true;
                }

                // Set debug level
                try {
                    Main.setDebugLevel(Integer.parseInt(args[1]));
                } catch (Exception e) {
                    if (player != null)
                        player.sendMessage(Utils.notify("&cCommand format: /grinch debug [debug level (0-3)]"));
                    else Utils.debugError("Command format: /grinch debug [debug level (0-3)]", 0);
                    return true;
                }

                // Notify
                if (player != null)
                    player.sendMessage(Utils.notify("&aDebug level set to " + args[1] + "."));
                else Utils.debugInfo("Debug level set to " + args[1] + ".", 0);

                return true;
            }
        }
        return false;
    }
}
