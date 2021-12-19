package me.theguyhere.grinchsimulator;

import me.theguyhere.grinchsimulator.GUI.Inventories;
import me.theguyhere.grinchsimulator.events.GameEndEvent;
import me.theguyhere.grinchsimulator.game.models.Tasks;
import me.theguyhere.grinchsimulator.game.models.arenas.Arena;
import me.theguyhere.grinchsimulator.game.models.arenas.ArenaManager;
import me.theguyhere.grinchsimulator.game.models.arenas.ArenaStatus;
import me.theguyhere.grinchsimulator.game.models.players.GPlayer;
import me.theguyhere.grinchsimulator.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

            // Force start
            if (args[0].equalsIgnoreCase("start")) {
                // Start current arena
                if (args.length == 1) {
                    // Check for player executing command
                    if (player == null) {
                        sender.sendMessage("Bad console!");
                        return true;
                    }

                    // Check for permission to use the command
                    if (!player.hasPermission("grinch.start")) {
                        player.sendMessage(Utils.notify(language.getString("permissionError")));
                        return true;
                    }

                    Arena arena;
                    GPlayer gamer;

                    // Attempt to get arena and player
                    try {
                        arena = Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull)
                                .filter(arena1 -> arena1.hasPlayer(player))
                                .collect(Collectors.toList()).get(0);
                        gamer = arena.getPlayer(player);
                    } catch (Exception e) {
                        player.sendMessage(Utils.notify(language.getString("forceStartError1")));
                        return true;
                    }

                    // Check if player is an active player
                    if (!arena.getPlayers().contains(gamer)) {
                        player.sendMessage(Utils.notify(language.getString("forceStartError2")));
                        return true;
                    }

                    // Check if arena already started
                    if (arena.getStatus() != ArenaStatus.WAITING) {
                        player.sendMessage(Utils.notify(language.getString("forceStartError3")));
                        return true;
                    }

                    Tasks task = arena.getTask();
                    Map<Runnable, Integer> tasks = task.getTasks();
                    BukkitScheduler scheduler = Bukkit.getScheduler();

                    // Bring game to quick start if not already
                    if (tasks.containsKey(task.full10) || tasks.containsKey(task.sec10) &&
                            !scheduler.isQueued(tasks.get(task.sec10))) {
                        player.sendMessage(Utils.notify(language.getString("forceStartError4")));
                        return true;
                    } else {
                        // Remove all tasks
                        tasks.forEach((runnable, id) -> scheduler.cancelTask(id));
                        tasks.clear();

                        // Schedule accelerated countdown tasks
                        task.sec10.run();
                        tasks.put(task.sec10, 0); // Dummy task id to note that quick start condition was hit
                        tasks.put(task.sec5,
                                scheduler.scheduleSyncDelayedTask(plugin, task.sec5, Utils.secondsToTicks(5)));
                        tasks.put(task.start,
                                scheduler.scheduleSyncDelayedTask(plugin, task.start, Utils.secondsToTicks(10)));
                    }
                }

                // Start specific arena
                else {
                    // Check for permission to use the command
                    if (player != null && !player.hasPermission("grinch.admin")) {
                        player.sendMessage(Utils.notify(language.getString("permissionError")));
                        return true;
                    }

                    StringBuilder name = new StringBuilder(args[1]);
                    for (int i = 0; i < args.length - 2; i++)
                        name.append(" ").append(args[i + 2]);

                    // Check if this arena exists
                    if (Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull)
                            .noneMatch(arena -> arena.getName().equals(name.toString()))) {
                        if (player != null)
                            player.sendMessage(Utils.notify("&cNo arena with this name exists!"));
                        else Utils.debugError("No arena with this name exists!", 0);
                        return true;
                    }

                    Arena arena = Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull)
                            .filter(arena1 -> arena1.hasPlayer(player))
                            .collect(Collectors.toList()).get(0);

                    // Check if arena already started
                    if (arena.getStatus() != ArenaStatus.WAITING) {
                        if (player != null)
                            player.sendMessage(Utils.notify(language.getString("forceStartError3")));
                        else Utils.debugError("The arena already has a game in progress!", 0);
                        return true;
                    }

                    // Check if there is at least 1 player
                    if (arena.getPlayers().size() == 0) {
                        if (player != null)
                            player.sendMessage(Utils.notify("&cThe arena needs at least 1 player to start!"));
                        else Utils.debugError("The arena needs at least 1 player to start!", 0);
                        return true;
                    }

                    Tasks task = arena.getTask();
                    Map<Runnable, Integer> tasks = task.getTasks();
                    BukkitScheduler scheduler = Bukkit.getScheduler();

                    // Bring game to quick start if not already
                    if (tasks.containsKey(task.full10) || tasks.containsKey(task.sec10) &&
                            !scheduler.isQueued(tasks.get(task.sec10))) {
                        if (player != null)
                            player.sendMessage(Utils.notify(language.getString("forceStartError4")));
                        else Utils.debugError("The game is already starting soon!", 0);
                        return true;
                    } else {
                        // Remove all tasks
                        tasks.forEach((runnable, id) -> scheduler.cancelTask(id));
                        tasks.clear();

                        // Schedule accelerated countdown tasks
                        task.sec10.run();
                        tasks.put(task.sec10, 0); // Dummy task id to note that quick start condition was hit
                        tasks.put(task.sec5,
                                scheduler.scheduleSyncDelayedTask(plugin, task.sec5, Utils.secondsToTicks(5)));
                        tasks.put(task.start,
                                scheduler.scheduleSyncDelayedTask(plugin, task.start, Utils.secondsToTicks(10)));

                        // Notify console
                        Utils.debugInfo("Arena " + arena.getArena() + " was force started.", 1);
                    }
                }

                return true;
            }

            // Force end
            if (args[0].equalsIgnoreCase("end")) {
                // End current arena
                if (args.length == 1) {
                    // Check for player executing command
                    if (player == null) {
                        sender.sendMessage("Bad console!");
                        return true;
                    }

                    // Check for permission to use the command
                    if (!player.hasPermission("grinch.admin")) {
                        player.sendMessage(Utils.notify(language.getString("permissionError")));
                        return true;
                    }

                    Arena arena;

                    // Attempt to get arena
                    try {
                        arena = Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull)
                                .filter(arena1 -> arena1.hasPlayer(player)).toList().get(0);
                    } catch (Exception e) {
                        player.sendMessage(Utils.notify(language.getString("forceStartError1")));
                        return true;
                    }

                    // Check if arena has a game in progress
                    if (arena.getStatus() != ArenaStatus.ACTIVE && arena.getStatus() != ArenaStatus.ENDING) {
                        player.sendMessage(Utils.notify("&cNo game to end!"));
                        return true;
                    }

                    // Check if game is about to end
                    if (arena.getStatus() == ArenaStatus.ENDING) {
                        player.sendMessage(Utils.notify("&cGame about to end!"));
                        return true;
                    }

                    // Force end
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
                            Bukkit.getPluginManager().callEvent(new GameEndEvent(arena)));

                    // Notify console
                    Utils.debugInfo("Arena " + arena.getArena() + " was force ended.", 1);
                }

                else {
                    // Check for permission to use the command
                    if (player != null && !player.hasPermission("grinch.admin")) {
                        player.sendMessage(Utils.notify(language.getString("permissionError")));
                        return true;
                    }

                    StringBuilder name = new StringBuilder(args[1]);
                    for (int i = 0; i < args.length - 2; i++)
                        name.append(" ").append(args[i + 2]);

                    // Check if this arena exists
                    if (Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull)
                            .noneMatch(arena -> arena.getName().equals(name.toString()))) {
                        if (player != null)
                            player.sendMessage(Utils.notify("&cNo arena with this name exists!"));
                        else Utils.debugError("No arena with this name exists!", 0);
                        return true;
                    }

                    Arena arena = Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull)
                            .filter(arena1 -> arena1.hasPlayer(player))
                            .collect(Collectors.toList()).get(0);

                    // Check if arena has a game in progress
                    if (arena.getStatus() != ArenaStatus.ACTIVE && arena.getStatus() != ArenaStatus.ENDING) {
                        if (player != null)
                            player.sendMessage(Utils.notify("&cNo game to end!"));
                        else Utils.debugError("No game to end!", 0);
                        return true;
                    }

                    // Check if game is about to end
                    if (arena.getStatus() == ArenaStatus.ENDING) {
                        if (player != null)
                            player.sendMessage(Utils.notify("&cGame about to end!"));
                        else Utils.debugError("Game about to end!", 0);
                        return true;
                    }

                    // Force end
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
                            Bukkit.getPluginManager().callEvent(new GameEndEvent(arena)));

                    // Notify console
                    Utils.debugInfo("Arena " + arena.getArena() + " was force ended.", 1);

                    return true;
                }
            }

            // Force delay start
            if (args[0].equalsIgnoreCase("delay")) {
                // Delay current arena
                if (args.length == 1) {
                    // Check for player executing command
                    if (player == null) {
                        sender.sendMessage("Bad console!");
                        return true;
                    }

                    // Check for permission to use the command
                    if (!player.hasPermission("grinch.start")) {
                        player.sendMessage(Utils.notify(language.getString("permissionError")));
                        return true;
                    }

                    Arena arena;

                    // Attempt to get arena
                    try {
                        arena = Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull)
                                .filter(arena1 -> arena1.hasPlayer(player))
                                .collect(Collectors.toList()).get(0);
                    } catch (Exception e) {
                        player.sendMessage(Utils.notify(language.getString("forceStartError1")));
                        return true;
                    }

                    // Check if arena already started
                    if (arena.getStatus() != ArenaStatus.WAITING) {
                        player.sendMessage(Utils.notify(language.getString("forceStartError3")));
                        return true;
                    }

                    Tasks task = arena.getTask();
                    Map<Runnable, Integer> tasks = task.getTasks();
                    BukkitScheduler scheduler = Bukkit.getScheduler();

                    // Remove all tasks
                    tasks.forEach((runnable, id) -> scheduler.cancelTask(id));
                    tasks.clear();

                    // Reschedule countdown tasks
                    task.min2.run();
                    tasks.put(task.min1, scheduler.scheduleSyncDelayedTask(plugin, task.min1,
                            Utils.secondsToTicks(Utils.minutesToSeconds(1))));
                    tasks.put(task.sec30, scheduler.scheduleSyncDelayedTask(plugin, task.sec30,
                            Utils.secondsToTicks(Utils.minutesToSeconds(2) - 30)));
                    tasks.put(task.sec10, scheduler.scheduleSyncDelayedTask(plugin, task.sec10,
                            Utils.secondsToTicks(Utils.minutesToSeconds(2) - 10)));
                    tasks.put(task.sec5, scheduler.scheduleSyncDelayedTask(plugin, task.sec5,
                            Utils.secondsToTicks(Utils.minutesToSeconds(2) - 5)));
                    tasks.put(task.start, scheduler.scheduleSyncDelayedTask(plugin, task.start,
                            Utils.secondsToTicks(Utils.minutesToSeconds(2))));
                }

                // Delay specific arena
                else {
                    // Check for permission to use the command
                    if (player != null && !player.hasPermission("grinch.admin")) {
                        player.sendMessage(Utils.notify(language.getString("permissionError")));
                        return true;
                    }

                    StringBuilder name = new StringBuilder(args[1]);
                    for (int i = 0; i < args.length - 2; i++)
                        name.append(" ").append(args[i + 2]);

                    // Check if this arena exists
                    if (Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull)
                            .noneMatch(arena -> arena.getName().equals(name.toString()))) {
                        if (player != null)
                            player.sendMessage(Utils.notify("&cNo arena with this name exists!"));
                        else Utils.debugError("No arena with this name exists!", 0);
                        return true;
                    }

                    Arena arena = Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull)
                            .filter(arena1 -> arena1.hasPlayer(player))
                            .collect(Collectors.toList()).get(0);

                    // Check if arena already started
                    if (arena.getStatus() != ArenaStatus.WAITING) {
                        if (player != null)
                            player.sendMessage(Utils.notify(language.getString("forceStartError3")));
                        else Utils.debugError("The arena already has a game in progress!", 0);
                        return true;
                    }

                    // Check if there is at least 1 player
                    if (arena.getPlayers().size() == 0) {
                        if (player != null)
                            player.sendMessage(Utils.notify("&cThe arena has no players!"));
                        else Utils.debugError("The arena has no players!", 0);
                        return true;
                    }

                    Tasks task = arena.getTask();
                    Map<Runnable, Integer> tasks = task.getTasks();
                    BukkitScheduler scheduler = Bukkit.getScheduler();

                    // Remove all tasks
                    tasks.forEach((runnable, id) -> scheduler.cancelTask(id));
                    tasks.clear();

                    // Reschedule countdown tasks
                    task.min2.run();
                    tasks.put(task.min1, scheduler.scheduleSyncDelayedTask(plugin, task.min1,
                            Utils.secondsToTicks(Utils.minutesToSeconds(1))));
                    tasks.put(task.sec30, scheduler.scheduleSyncDelayedTask(plugin, task.sec30,
                            Utils.secondsToTicks(Utils.minutesToSeconds(2) - 30)));
                    tasks.put(task.sec10, scheduler.scheduleSyncDelayedTask(plugin, task.sec10,
                            Utils.secondsToTicks(Utils.minutesToSeconds(2) - 10)));
                    tasks.put(task.sec5, scheduler.scheduleSyncDelayedTask(plugin, task.sec5,
                            Utils.secondsToTicks(Utils.minutesToSeconds(2) - 5)));
                    tasks.put(task.start, scheduler.scheduleSyncDelayedTask(plugin, task.start,
                            Utils.secondsToTicks(Utils.minutesToSeconds(2))));

                    // Notify console
                    Utils.debugInfo("Arena " + arena.getArena() + " was delayed.", 1);
                }

                return true;
            }

            // Fix certain default files
            if (args[0].equalsIgnoreCase("fix")) {
                boolean fixed = false;

                // Check for permission to use the command
                if (player != null && !player.hasPermission("grinch.admin")) {
                    player.sendMessage(Utils.notify(language.getString("permissionError")));
                    return true;
                }

                // Check for correct format
                if (args.length > 1) {
                    if (player != null)
                        player.sendMessage(Utils.notify("&cCommand format: /grinch fix"));
                    else Utils.debugError("Command format: 'grinch fix'", 0);
                    return true;
                }

                // Check if plugin.yml is outdated
                if (plugin.getConfig().getInt("version") < plugin.configVersion)
                    if (player != null)
                        player.sendMessage(Utils.notify("&cplugin.yml must be updated manually."));
                    else Utils.debugError("plugin.yml must be updated manually.", 0);

                // Check if arenaData.yml is outdated
                if (plugin.getConfig().getInt("arenaData") < plugin.arenaDataVersion &&
                        plugin.getArenaData().getConfigurationSection("") != null) {
                    if (plugin.getConfig().getInt("arenaData") < 2) {
                        // Flip flag
                        fixed = true;

                        // Fix
                        try {
                            Objects.requireNonNull(plugin.getArenaData().getConfigurationSection(""))
                                    .getKeys(false).stream().filter(a -> a.length() < 4).forEach(a -> {
                                        plugin.getArenaData().set(a + ".timeLimit",
                                                plugin.getArenaData().getInt(a + ".waveTimeLimit"));
                                        plugin.getArenaData().set(a + ".waveTimeLimit", null);
                                    });
                            plugin.getConfig().set("arenaData", 2);
                            plugin.saveArenaData();
                            plugin.saveConfig();
                        } catch (Exception e) {
                            fixed = false;

                            // Notify
                            if (player != null)
                                player.sendMessage(Utils.notify("&carenaData.yml must be updated manually."));
                            else Utils.debugError("arenaData.yml must be updated manually.", 0);
                        }

                        // Notify
                        if (player != null)
                            player.sendMessage(Utils.notify("&aarenaData.yml has been automatically updated. " +
                                    "Please restart the plugin."));
                        Utils.debugInfo("arenaData.yml has been automatically updated. Please restart the plugin.",
                                0);

                    } else {
                        if (player != null)
                            player.sendMessage(Utils.notify("&carenaData.yml must be updated manually."));
                        else Utils.debugError("arenaData.yml must be updated manually.", 0);
                    }
                }

                // Check if playerData.yml is outdated
                if (plugin.getConfig().getInt("playerData") < plugin.playerDataVersion)
                    if (player != null)
                        player.sendMessage(Utils.notify("&cplayerData.yml must be updated manually."));
                    else Utils.debugError("playerData.yml must be updated manually.", 0);

                // Update default language file
                if (plugin.getConfig().getInt("languageFile") < plugin.languageFileVersion) {
                    // Flip flag
                    fixed = true;

                    // Fix
                    plugin.saveResource("languages/en_US.yml", true);
                    plugin.getConfig().set("languageFile", plugin.languageFileVersion);
                    plugin.saveConfig();

                    // Notify
                    if (player != null)
                        player.sendMessage(Utils.notify("&aen_US.yml has been automatically updated. " +
                                "Please restart the plugin."));
                    Utils.debugInfo("en_US.yml has been automatically updated. Please restart the plugin.",
                            0);
                }

                // Message to player depending on whether the command fixed anything
                if (!fixed)
                    if (player != null)
                        player.sendMessage(Utils.notify("There was nothing that could be updated automatically."));
                    else Utils.debugInfo("There was nothing that could be updated automatically.", 0);

                return true;
            }

            // No valid command sent
            if (player != null)
                player.sendMessage(Utils.notify(language.getString("commandError")));
            else Utils.debugError("Invalid command. Use 'grinch help' for more info.", 0);
            return true;
        }
        return false;
    }
}
