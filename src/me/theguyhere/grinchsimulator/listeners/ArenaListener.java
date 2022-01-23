package me.theguyhere.grinchsimulator.listeners;

import me.theguyhere.grinchsimulator.GUI.Inventories;
import me.theguyhere.grinchsimulator.Main;
import me.theguyhere.grinchsimulator.events.*;
import me.theguyhere.grinchsimulator.game.models.Tasks;
import me.theguyhere.grinchsimulator.game.models.arenas.Arena;
import me.theguyhere.grinchsimulator.game.models.arenas.ArenaManager;
import me.theguyhere.grinchsimulator.game.models.arenas.ArenaRecord;
import me.theguyhere.grinchsimulator.game.models.arenas.ArenaStatus;
import me.theguyhere.grinchsimulator.game.models.players.GPlayer;
import me.theguyhere.grinchsimulator.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.*;

public class ArenaListener implements Listener {
    private final Main plugin;

    public ArenaListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(JoinArenaEvent e) {
        Player player = e.getPlayer();
        BukkitScheduler scheduler = Bukkit.getScheduler();
        FileConfiguration language = plugin.getLanguageData();

        // Ignore if player is already in a game somehow
        if (Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).anyMatch(a -> a.hasPlayer(player))) {
            e.setCancelled(true);
            player.sendMessage(Utils.notify(language.getString("joinError")));
            return;
        }

        Arena arena = e.getArena();
        Location spawn;
        Location waiting;

        // Check if arena is closed
        if (arena.isClosed()) {
            player.sendMessage(Utils.notify(language.getString("closeError")));
            e.setCancelled(true);
            return;
        }

        // Try to get waiting room
        try {
            waiting = arena.getWaitingRoom();
        } catch (Exception err) {
            waiting = null;
        }

        // Try to get player spawn
        try {
            spawn = arena.getPlayerSpawn();
        } catch (Exception err) {
            err.printStackTrace();
            player.sendMessage(Utils.notify(language.getString("fatalError")));
            return;
        }

        // Set waiting room to spawn if absent
        if (waiting == null)
            waiting = spawn;

        int players = arena.getPlayers().size();

        if (plugin.getConfig().getBoolean("keepInv")) {
            // Save player exp and items before going into arena
            plugin.getPlayerData().set(player.getName() + ".health", player.getHealth());
            plugin.getPlayerData().set(player.getName() + ".food", player.getFoodLevel());
            plugin.getPlayerData().set(player.getName() + ".saturation", (double) player.getSaturation());
            plugin.getPlayerData().set(player.getName() + ".level", player.getLevel());
            plugin.getPlayerData().set(player.getName() + ".exp", (double) player.getExp());
            for (int i = 0; i < player.getInventory().getContents().length; i++)
                plugin.getPlayerData().set(player.getName() + ".inventory." + i, player.getInventory().getContents()[i]);
            plugin.savePlayerData();
        }

        // Prepares player to enter arena if it doesn't exceed max capacity and if the arena is still waiting
        if (players < arena.getMaxPlayers() && arena.getStatus() == ArenaStatus.WAITING) {
            // Teleport to arena or waiting room
            Utils.teleAdventure(player, waiting);
            player.setInvulnerable(true);

            // Notify everyone in the arena
            arena.getPlayers().forEach(gamer ->
                    gamer.getPlayer().sendMessage(Utils.notify(
                            String.format(Objects.requireNonNull(language.getString("join")),
                            player.getName()))));

            // Update player tracking and in-game stats
            GPlayer grinch = new GPlayer(player);
            arena.getPlayers().add(grinch);
            arena.refreshPortal();

            // Give them a game board
            ArenaManager.createBoard(grinch);

            // Play waiting music
            if (arena.getWaitingSound() != null)
                try {
                    if (arena.getWaitingRoom() != null)
                        player.playSound(arena.getWaitingRoom(), arena.getWaitingSound(), 4, 1);
                    else player.playSound(arena.getPlayerSpawn(), arena.getWaitingSound(), 4, 1);
                } catch (Exception err) {
                    Utils.debugError(err.getMessage(), 0);
                }

            // Give player pre game hotbar
            Inventories.setPreGameHotbar(player);

            // Debug message to console
            Utils.debugInfo(player.getName() + "joined Arena " + arena.getArena(), 2);
        }

        // Join players as spectators if arena is full or game already started
        else {
            // Notify player of full arena
            player.sendMessage(Utils.notify(language.getString("fullError")));

            // Don't touch task updating
            return;
        }

        players = arena.getPlayers().size();
        Tasks task = arena.getTask();
        Map<Runnable, Integer> tasks = task.getTasks();
        List<Runnable> toRemove = new ArrayList<>();

        // Waiting condition
        if (players < arena.getMinPlayers() &&
                (tasks.isEmpty() || !scheduler.isCurrentlyRunning(tasks.get(task.waiting))) &&
                !tasks.containsKey(task.full10)) {

            // Remove other tasks that's not the waiting task
            tasks.forEach((runnable, id) -> {
                if (!runnable.equals(task.waiting)) toRemove.add(runnable);
            });
            toRemove.forEach(r -> {
                scheduler.cancelTask(tasks.get(r));
                tasks.remove(r);
            });

            // Schedule and record the waiting task
            tasks.put(task.waiting, scheduler.scheduleSyncRepeatingTask(plugin, task.waiting, 0,
                    Utils.secondsToTicks(Utils.minutesToSeconds(1))));
        }

        // Can start condition
        else if (players < arena.getMaxPlayers() && !tasks.containsKey(task.full10) &&
                !tasks.containsKey(task.min1)) {
            // Remove the waiting task if it exists
            if (tasks.containsKey(task.waiting)) {
                scheduler.cancelTask(tasks.get(task.waiting));
                tasks.remove(task.waiting);
            }

            // Schedule all the countdown tasks
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

        // Quick start condition
        else if (players == arena.getMaxPlayers() && !tasks.containsKey(task.full10) &&
                !(tasks.containsKey(task.sec10) && !scheduler.isQueued(tasks.get(task.sec10)))) {
            // Remove all tasks
            tasks.forEach((runnable, id) -> scheduler.cancelTask(id));
            tasks.clear();

            // Schedule accelerated countdown tasks
            task.full10.run();
            tasks.put(task.full10, 0); // Dummy task id to note that quick start condition was hit
            tasks.put(task.sec5, scheduler.scheduleSyncDelayedTask(plugin, task.sec5, Utils.secondsToTicks(5)));
            tasks.put(task.start, scheduler.scheduleSyncDelayedTask(plugin, task.start, Utils.secondsToTicks(10)));
        }
    }

    @EventHandler
    public void onGameStart(GameStartEvent e) {
        Arena arena = e.getArena();

        // Don't continue if the arena is not active
        if (arena.getStatus() != ArenaStatus.ACTIVE) {
            e.setCancelled(true);
            return;
        }

        // Play wave start sound
        if (arena.hasStartSound()) {
            for (GPlayer vdPlayer : arena.getPlayers()) {
                vdPlayer.getPlayer().playSound(arena.getPlayerSpawn(),
                        Sound.ENTITY_ENDER_DRAGON_GROWL, 10, .25f);
            }
        }

        Tasks task = arena.getTask();

        // Start wave count down
        if (arena.getTimeLimit() != -1)
            task.getTasks().put(task.updateBar,
                Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, task.updateBar, 0, Utils.secondsToTicks(1)));

        // Debug message to console
        Utils.debugInfo("Arena " + arena.getArena() + " has started.", 2);
    }

    @EventHandler
    public void onLeave(LeaveArenaEvent e) {
        Player player = e.getPlayer();
        Arena arena;
        GPlayer gamer;

        // Attempt to get arena and player
        try {
            arena = Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).filter(a -> a.hasPlayer(player))
                    .toList().get(0);
            gamer = arena.getPlayer(player);
        } catch (Exception err) {
            e.setCancelled(true);
            player.sendMessage(Utils.notify(plugin.getLanguageData().getString("leaveError")));
            return;
        }

        // Stop playing possible ending sound
        player.stopSound(Sound.ENTITY_ENDER_DRAGON_DEATH);
        if (arena.getWaitingSound() != null)
            player.stopSound(arena.getWaitingSound());

        // Remove the player from the arena and time limit bar if exists
        arena.getPlayers().remove(gamer);
        if (arena.getTimeLimitBar() != null)
            arena.removePlayerFromTimeLimitBar(gamer.getPlayer());

        // Notify people in arena player left
        arena.getPlayers().forEach(grinch ->
                grinch.getPlayer().sendMessage(Utils.notify(String.format(
                        Objects.requireNonNull(plugin.getLanguageData().getString("leave")),
                        player.getName()))));

        int actives = arena.getPlayers().size();

        // Sets them up for teleport to lobby
        player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
        Utils.teleAdventure(player, ArenaManager.getLobby());

        Tasks task = arena.getTask();
        Map<Runnable, Integer> tasks = task.getTasks();
        BukkitScheduler scheduler = Bukkit.getScheduler();
        List<Runnable> toRemove = new ArrayList<>();

        // Check if arena can no longer start
        if (actives < arena.getMinPlayers() && arena.getStatus() == ArenaStatus.WAITING) {
            // Remove other tasks that's not the waiting task
            tasks.forEach((runnable, id) -> {
                if (actives == 0 || !runnable.equals(task.waiting)) toRemove.add(runnable);
            });
            toRemove.forEach(r -> {
                scheduler.cancelTask(tasks.get(r));
                tasks.remove(r);
            });

            // Schedule and record the waiting task if appropriate
            if (actives != 0)
                tasks.put(task.waiting, scheduler.scheduleSyncRepeatingTask(plugin, task.waiting, 0,
                        Utils.secondsToTicks(60)));
        }

        // Checks if the game has ended because no players are left
        if (arena.getPlayers().size() == 0 && arena.getStatus() == ArenaStatus.ACTIVE)
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
                    Bukkit.getPluginManager().callEvent(new GameEndEvent(arena)));

        // Return player health, food, exp, and items
        if (plugin.getConfig().getBoolean("keepInv") && player.isOnline()) {
            if (plugin.getPlayerData().contains(player.getName() + ".health"))
                player.setHealth(plugin.getPlayerData().getDouble(player.getName() + ".health"));
            plugin.getPlayerData().set(player.getName() + ".health", null);
            if (plugin.getPlayerData().contains(player.getName() + ".food"))
                player.setFoodLevel(plugin.getPlayerData().getInt(player.getName() + ".food"));
            plugin.getPlayerData().set(player.getName() + ".food", null);
            if (plugin.getPlayerData().contains(player.getName() + ".saturation"))
                player.setSaturation((float) plugin.getPlayerData().getDouble(player.getName() + ".saturation"));
            plugin.getPlayerData().set(player.getName() + ".saturation", null);
            if (plugin.getPlayerData().contains(player.getName() + ".level"))
                player.setLevel(plugin.getPlayerData().getInt(player.getName() + ".level"));
            plugin.getPlayerData().set(player.getName() + ".level", null);
            if (plugin.getPlayerData().contains(player.getName() + ".exp"))
                player.setExp((float) plugin.getPlayerData().getDouble(player.getName() + ".exp"));
            plugin.getPlayerData().set(player.getName() + ".exp", null);
            if (plugin.getPlayerData().contains(player.getName() + ".inventory"))
                Objects.requireNonNull(plugin.getPlayerData()
                                .getConfigurationSection(player.getName() + ".inventory"))
                        .getKeys(false)
                        .forEach(num -> player.getInventory().setItem(Integer.parseInt(num),
                                (ItemStack) plugin.getPlayerData().get(player.getName() + ".inventory." + num)));
            plugin.getPlayerData().set(player.getName() + ".inventory", null);
            plugin.savePlayerData();
        }

        // Refresh the game portal
        arena.refreshPortal();

        // Refresh all displays for the player
        ArenaManager.displayEverything(player);

        // Debug message to console
        Utils.debugInfo(player.getName() + " left Arena " + arena.getArena(), 2);
    }

    @EventHandler
    public void onGameEnd(GameEndEvent e) {
        Arena arena = e.getArena();
        FileConfiguration language = plugin.getLanguageData();

        // Set the arena to ending
        arena.setStatus(ArenaStatus.ENDING);

        // Notify players that the game has ended (Title)
        try {
            arena.getPlayers().forEach(player ->
                    player.getPlayer().sendTitle(Utils.format(
                            Objects.requireNonNull(plugin.getLanguageData().getString("gameOver"))),
                            "", Utils.secondsToTicks(.5), Utils.secondsToTicks(2.5), Utils.secondsToTicks(1)));
        } catch (Exception err) {
            Utils.debugError("The key 'gameOver' is either missing or corrupt in the active language file",
                    1);
        }

        // Notify players that the game has ended (Chat)
        try {
            arena.getPlayers().forEach(player ->
                    player.getPlayer().sendMessage(
                            Utils.notify(Objects.requireNonNull(language.getString("end")))));
        } catch (Exception err) {
            Utils.debugError("The key 'end' is either missing or corrupt in the active language file",
                    1);
        }

        // Play sound if turned on
        if (arena.hasLoseSound()) {
            for (GPlayer vdPlayer : arena.getPlayers()) {
                vdPlayer.getPlayer().playSound(arena.getPlayerSpawn(),
                        Sound.ENTITY_ENDER_DRAGON_DEATH, 10, .5f);
            }
        }

        Tasks task = arena.getTask();
        Map<Runnable, Integer> tasks = task.getTasks();

        // Reset the arena
        if (tasks.containsKey(task.updateBar)) {
            Bukkit.getScheduler().cancelTask(tasks.get(task.updateBar));
            tasks.remove(task.updateBar);
            arena.removeTimeLimitBar();
            arena.checkRecords();
            arena.returnPresents();
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
                Bukkit.getPluginManager().callEvent(new ArenaResetEvent(arena)), Utils.secondsToTicks(10));


        // Debug message to console
        Utils.debugInfo("Arena " + arena.getArena() + " is ending.", 2);
    }

    @EventHandler
    public void onArenaReset(ArenaResetEvent e) {
        e.getArena().getTask().reset.run();
    }

    @EventHandler
    public void onBoardReload(ReloadBoardsEvent e) {
        e.getArena().getTask().updateBoards.run();
    }
}
