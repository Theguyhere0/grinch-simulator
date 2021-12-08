package me.theguyhere.grinchsimulator.game.models.arenas;

import me.theguyhere.grinchsimulator.Main;
import me.theguyhere.grinchsimulator.exceptions.InvalidNameException;
import me.theguyhere.grinchsimulator.exceptions.PlayerNotFoundException;
import me.theguyhere.grinchsimulator.game.models.Tasks;
import me.theguyhere.grinchsimulator.game.models.players.GPlayer;
import me.theguyhere.grinchsimulator.tools.Utils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A class managing data about a Villager Defense arena.
 */
public class Arena {
    /** Instance of the plugin.*/
    private final Main plugin;
    /** Arena number.*/
    private final int arena;
    /** A variable more quickly access the file configuration of the arena file.*/
    private final FileConfiguration config;
    /** Common string for all data paths in the arena file.*/
    private final String path;
    private final Tasks task; // The tasks object for the arena

    /** Status of the arena.*/
    private ArenaStatus status;
    /** The ID of the game currently in progress.*/
    private int gameID;
    /** ID of task managing player spawn particles.*/
    private int playerParticlesID = 0;
    /** ID of task managing corner particles.*/
    private int cornerParticlesID = 0;
    /** A list of players in the arena.*/
    private final List<GPlayer> players = new ArrayList<>();
    /** Time limit bar object.*/
    private BossBar timeLimitBar;
//    /** Portal object for the arena.*/
//    private Portal portal;
//    /** The player spawn for the arena.*/
//    private ArenaSpawn playerSpawn;
//    /** Arena scoreboard object for the arena.*/
//    private ArenaBoard arenaBoard;

    public Arena(Main plugin, int arena, Tasks task) {
        this.plugin = plugin;
        config = plugin.getArenaData();
        this.arena = arena;
        path = "a" + arena;
        this.task = task;
        status = ArenaStatus.WAITING;
//        refreshArenaBoard();
//        refreshPlayerSpawn();
//        refreshPortal();
//        checkClosedParticles();
    }

    public int getArena() {
        return arena;
    }

    /**
     * Retrieves the name of the arena from the arena file.
     * @return Arena name.
     */
    public String getName() {
        return config.getString(path + ".name");
    }

    /**
     * Writes the new name of the arena into the arena file.
     * @param name New arena name.
     */
    public void setName(String name) throws InvalidNameException {
        // Check if name is not empty
        if (name == null || name.length() == 0) throw new InvalidNameException();
        else {
            config.set(path + ".name", name);
            plugin.saveArenaData();
        }

        // Set default max players to 12 if it doesn't exist
        if (getMaxPlayers() == 0)
            setMaxPlayers(12);

        // Set default min players to 1 if it doesn't exist
        if (getMinPlayers() == 0)
            setMinPlayers(1);
        
        // Set default wave time limit to -1 if it doesn't exist
        if (getWaveTimeLimit() == 0)
            setWaveTimeLimit(-1);

        // Set default to closed if arena closed doesn't exist
        if (!config.contains(path + ".closed"))
            setClosed(true);

        // Set default sound options
        if (!config.contains(path + ".sounds")) {
            setWinSound(true);
            setLoseSound(true);
            setWaveStartSound(true);
            setWaveFinishSound(true);
            setWaitingSound(14);
        }
        
        // Set default particle toggles
        if (!config.contains(path + ".particles.spawn"))
            setSpawnParticles(true);

//        // Refresh portal
//        if (getPortalLocation() != null)
//            refreshPortal();
    }

    /**
     * Retrieves the difficulty label of the arena from the arena file.
     * @return Arena difficulty label.
     */
    public String getDifficultyLabel() {
        if (config.contains(path + ".difficultyLabel"))
            return config.getString(path + ".difficultyLabel");
        else return "";
    }

    /**
     * Writes the new difficulty label of the arena into the arena file.
     * @param label New difficulty label.
     */
    public void setDifficultyLabel(String label) {
        config.set(path + ".difficultyLabel", label);
        plugin.saveArenaData();
//        refreshPortal();
    }

    /**
     * Retrieves the maximum player count of the arena from the arena file.
     * @return Maximum player count.
     */
    public int getMaxPlayers() {
        return config.getInt(path + ".max");
    }

    /**
     * Writes the new maximum player count of the arena into the arena file.
     * @param maxPlayers New maximum player count.
     */
    public void setMaxPlayers(int maxPlayers) {
        config.set(path + ".max", maxPlayers);
        plugin.saveArenaData();
    }

    /**
     * Retrieves the minimum player count of the arena from the arena file.
     * @return Minimum player count.
     */
    public int getMinPlayers() {
        return config.getInt(path + ".min");
    }

    /**
     * Writes the new minimum player count of the arena into the arena file.
     * @param minPlayers New minimum player count.
     */
    public void setMinPlayers(int minPlayers) {
        config.set(path + ".min", minPlayers);
        plugin.saveArenaData();
    }
    
    /**
     * Retrieves the nominal time limit per wave of the arena from the arena file.
     * @return Nominal time limit per wave.
     */
    public int getWaveTimeLimit() {
        return config.getInt(path + ".waveTimeLimit");
    }

    /**
     * Writes the new nominal time limit per wave of the arena into the arena file.
     * @param timeLimit New nominal time limit per wave.
     */
    public void setWaveTimeLimit(int timeLimit) {
        config.set(path + ".waveTimeLimit", timeLimit);
        plugin.saveArenaData();
    }
    
    /**
     * Retrieves the waiting music of the arena from the arena file.
     * @return Waiting {@link Sound}.
     */
    public Sound getWaitingSound() {
        return switch (config.getInt(path + ".sounds.waiting")) {
            case 0 -> Sound.MUSIC_DISC_CAT;
            case 1 -> Sound.MUSIC_DISC_BLOCKS;
            case 2 -> Sound.MUSIC_DISC_FAR;
            case 3 -> Sound.MUSIC_DISC_STRAD;
            case 4 -> Sound.MUSIC_DISC_MELLOHI;
            case 5 -> Sound.MUSIC_DISC_WARD;
            case 9 -> Sound.MUSIC_DISC_CHIRP;
            case 10 -> Sound.MUSIC_DISC_STAL;
            case 11 -> Sound.MUSIC_DISC_MALL;
            case 12 -> Sound.MUSIC_DISC_WAIT;
            case 13 -> Sound.MUSIC_DISC_PIGSTEP;
            default -> null;
        };
    }

    /**
     * Create the button for a given waiting music of the arena from the arena file.
     * @return A button for GUIs.
     */
    public ItemStack getWaitingSoundButton(int number) {
        HashMap<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.DURABILITY, 1);
        int sound = config.getInt(path + ".sounds.waiting");
        boolean selected;

        switch (number) {
            case 0 -> {
                selected = sound == 0;
                return Utils.createItem(Material.MUSIC_DISC_CAT,
                        Utils.format((selected ? "&a&l" : "&4&l") + "Cat"),
                        Utils.BUTTON_FLAGS, selected ? enchants : null);
            }
            case 1 -> {
                selected = sound == 1;
                return Utils.createItem(Material.MUSIC_DISC_BLOCKS,
                        Utils.format((selected ? "&a&l" : "&4&l") + "Blocks"),
                        Utils.BUTTON_FLAGS, selected ? enchants : null);
            }
            case 2 -> {
                selected = sound == 2;
                return Utils.createItem(Material.MUSIC_DISC_FAR,
                        Utils.format((selected ? "&a&l" : "&4&l") + "Far"),
                        Utils.BUTTON_FLAGS, selected ? enchants : null);
            }
            case 3 -> {
                selected = sound == 3;
                return Utils.createItem(Material.MUSIC_DISC_STRAD,
                        Utils.format((selected ? "&a&l" : "&4&l") + "Strad"),
                        Utils.BUTTON_FLAGS, selected ? enchants : null);
            }
            case 4 -> {
                selected = sound == 4;
                return Utils.createItem(Material.MUSIC_DISC_MELLOHI,
                        Utils.format((selected ? "&a&l" : "&4&l") + "Mellohi"),
                        Utils.BUTTON_FLAGS, selected ? enchants : null);
            }
            case 5 -> {
                selected = sound == 5;
                return Utils.createItem(Material.MUSIC_DISC_WARD,
                        Utils.format(((selected ? "&a&l" : "&4&l") + "Ward")),
                        Utils.BUTTON_FLAGS, selected ? enchants : null);
            }
            case 9 -> {
                selected = sound == 9;
                return Utils.createItem(Material.MUSIC_DISC_CHIRP,
                        Utils.format((selected ? "&a&l" : "&4&l") + "Chirp"),
                        Utils.BUTTON_FLAGS, selected ? enchants : null);
            }
            case 10 -> {
                selected = sound == 10;
                return Utils.createItem(Material.MUSIC_DISC_STAL,
                        Utils.format((selected ? "&a&l" : "&4&l") + "Stal"),
                        Utils.BUTTON_FLAGS, selected ? enchants : null);
            }
            case 11 -> {
                selected = sound == 11;
                return Utils.createItem(Material.MUSIC_DISC_MALL,
                        Utils.format((selected ? "&a&l" : "&4&l") + "Mall"),
                        Utils.BUTTON_FLAGS, selected ? enchants : null);
            }
            case 12 -> {
                selected = sound == 12;
                return Utils.createItem(Material.MUSIC_DISC_WAIT,
                        Utils.format((selected ? "&a&l" : "&4&l") + "Wait"),
                        Utils.BUTTON_FLAGS, selected ? enchants : null);
            }
            case 13 -> {
                selected = sound == 13;
                return Utils.createItem(Material.MUSIC_DISC_PIGSTEP,
                        Utils.format((selected ? "&a&l" : "&4&l") + "Pigstep"),
                        Utils.BUTTON_FLAGS, selected ? enchants : null);
            }
            default -> {
                selected = sound < 0 || sound > 5 && sound < 9 || sound > 13;
                return Utils.createItem(Material.LIGHT_GRAY_CONCRETE,
                        Utils.format((selected ? "&a&l" : "&4&l") + "None"),
                        Utils.BUTTON_FLAGS, selected ? enchants : null);
            }
        }
    }

    /**
     * Retrieves the waiting music title of the arena from the arena file.
     * @return Waiting music title.
     */
    public String getWaitingSoundName() {
        return switch (config.getInt(path + ".sounds.waiting")) {
            case 0 -> "Cat";
            case 1 -> "Blocks";
            case 2 -> "Far";
            case 3 -> "Strad";
            case 4 -> "Mellohi";
            case 5 -> "Ward";
            case 9 -> "Chirp";
            case 10 -> "Stal";
            case 11 -> "Mall";
            case 12 -> "Wait";
            case 13 -> "Pigstep";
            default -> "None";
        };
    }

    /**
     * Retrieves the waiting music numerical representation of the arena into the arena file.
     * @return Waiting music numerical representation.
     */
    public int getWaitingSoundNum() {
        return config.getInt(path + ".sounds.waiting");
    }

    /**
     * Writes the new waiting music of the arena into the arena file.
     * @param sound Numerical representation of the new waiting music.
     */
    public void setWaitingSound(int sound) {
        config.set(path + ".sounds.waiting", sound);
        plugin.saveArenaData();
    }

//    public Portal getPortal() {
//        return portal;
//    }
//
//    public Location getPortalLocation() {
//        return Utils.getConfigLocationNoPitch(plugin, path + ".portal");
//    }

//    /**
//     * Creates a new portal at the given location and deletes the old portal.
//     * @param location New location
//     */
//    public void setPortal(Location location) {
//        // Save config location
//        Utils.setConfigurationLocation(plugin, path + ".portal", location);
//
//        // Recreate the portal
//        refreshPortal();
//    }
//
//    /**
//     * Recreates the portal in game based on the location in the arena file.
//     */
//    public void refreshPortal() {
//        // Try recreating the portal
//        try {
//            // Delete old portal if needed
//            if (portal != null)
//                portal.remove();
//
//            // Create a new portal and display it
//            portal = new Portal(Objects.requireNonNull(Utils.getConfigLocationNoPitch(plugin, path + ".portal")),
//                    this);
//            portal.displayForOnline();
//        } catch (Exception e) {
//            Utils.debugError("Invalid location for arena board " + arena, 1);
//            Utils.debugInfo("Portal location data may be corrupt. If data cannot be manually corrected in " +
//                    "arenaData.yml, please delete the portal location data for arena " + arena + ".", 1);
//        }
//    }
//
//    /**
//     * Centers the portal location along the x and z axis.
//     */
//    public void centerPortal() {
//        // Center the location
//        Utils.centerConfigLocation(plugin, path + ".portal");
//
//        // Recreate the portal
//        refreshPortal();
//    }
//
//    /**
//     * Removes the portal from the game and from the arena file.
//     */
//    public void removePortal() {
//        if (portal != null) {
//            portal.remove();
//            portal = null;
//        }
//        Utils.setConfigurationLocation(plugin, path + ".portal", null);
//        checkClose();
//    }
//
//    public ArenaBoard getArenaBoard() {
//        return arenaBoard;
//    }
//
//    public Location getArenaBoardLocation() {
//        return Utils.getConfigLocationNoPitch(plugin, path + ".arenaBoard");
//    }
//    
//    /**
//     * Creates a new arena leaderboard at the given location and deletes the old arena leaderboard.
//     * @param location New location
//     */
//    public void setArenaBoard(Location location) {
//        // Save config location
//        Utils.setConfigurationLocation(plugin, path + ".arenaBoard", location);
//
//        // Recreate the board
//        refreshArenaBoard();
//    }
//
//    /**
//     * Recreates the arena leaderboard in game based on the location in the arena file.
//     */
//    public void refreshArenaBoard() {
//        // Try recreating the board
//        try {
//            // Delete old board if needed
//            if (arenaBoard != null)
//                arenaBoard.remove();
//
//            // Create a new board and display it
//            arenaBoard = new ArenaBoard(
//                    Objects.requireNonNull(Utils.getConfigLocationNoPitch(plugin, path + ".arenaBoard")),
//                    this
//            );
//            arenaBoard.displayForOnline();
//        } catch (Exception e) {
//            Utils.debugError("Invalid location for arena board " + arena, 1);
//            Utils.debugInfo("Arena board location data may be corrupt. If data cannot be manually corrected in " +
//                    "arenaData.yml, please delete the arena board location data for arena " + arena + ".", 1);
//        }
//    }
//
//    /**
//     * Centers the arena leaderboard location along the x and z axis.
//     */
//    public void centerArenaBoard() {
//        // Center the location
//        Utils.centerConfigLocation(plugin, path + ".arenaBoard");
//
//        // Recreate the board
//        refreshArenaBoard();
//    }
//
//    /**
//     * Removes the arena board from the game and from the arena file.
//     */
//    public void removeArenaBoard() {
//        if (arenaBoard != null) {
//            arenaBoard.remove();
//            arenaBoard = null;
//        }
//        Utils.setConfigurationLocation(plugin, path + ".arenaBoard", null);
//    }

//    /**
//     * Refreshes the player spawn of the arena.
//     */
//    public void refreshPlayerSpawn() {
//        // Prevent refreshing player spawn when arena is open
//        if (!isClosed())
//            return;
//
//        // Close off any particles if they are on
//        if (playerSpawn != null && playerSpawn.isOn())
//            playerSpawn.turnOffIndicator();
//
//        // Attempt to fetch new player spawn
//        try {
//            playerSpawn = new ArenaSpawn(
//                    Objects.requireNonNull(Utils.getConfigLocation(plugin, path + ".spawn")),
//                    ArenaSpawnType.PLAYER,
//                    0);
//        } catch (InvalidLocationException | NullPointerException e) {
//            playerSpawn = null;
//        }
//    }
//
//    public ArenaSpawn getPlayerSpawn() {
//        return playerSpawn;
//    }

//    /**
//     * Writes the new player spawn location of the arena into the arena file.
//     * @param location New player spawn location.
//     */
//    public void setPlayerSpawn(Location location) {
//        Utils.setConfigurationLocation(plugin, path + ".spawn", location);
//        refreshPlayerSpawn();
//    }
//
//    /**
//     * Centers the player spawn location of the arena along the x and z axis.
//     */
//    public void centerPlayerSpawn() {
//        Utils.centerConfigLocation(plugin, path + ".spawn");
//        refreshPlayerSpawn();
//    }
//
//    /**
//     * Retrieves the waiting room location of the arena from the arena file.
//     * @return Player spawn location.
//     */
//    public Location getWaitingRoom() {
//        return Utils.getConfigLocation(plugin, path + ".waiting");
//    }
//
//    /**
//     * Writes the new waiting room location of the arena into the arena file.
//     * @param location New player spawn location.
//     */
//    public void setWaitingRoom(Location location) {
//        Utils.setConfigurationLocation(plugin, path + ".waiting", location);
//        plugin.saveArenaData();
//    }
//
//    /**
//     * Centers the waiting room location of the arena along the x and z axis.
//     */
//    public void centerWaitingRoom() {
//        Utils.centerConfigLocation(plugin, path + ".waiting");
//    }
    
    public boolean hasSpawnParticles() {
        return config.getBoolean(path + ".particles.spawn");
    }

    public void setSpawnParticles(boolean bool) {
        config.set(path + ".particles.spawn", bool);
        plugin.saveArenaData();
    }

//    public void startSpawnParticles() {
//        if (getPlayerSpawn() == null)
//            return;
//
//        if (isClosed())
//            getPlayerSpawn().turnOnIndicator();
//
//        if (playerParticlesID != 0)
//            return;
//
//        playerParticlesID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
//            double var = 0;
//            double var2 = 0;
//            Location first, second;
//
//            @Override
//            public void run() {
//                try {
//                    // Update particle locations
//                    var += Math.PI / 12;
//                    var2 -= Math.PI / 12;
//                    first = getPlayerSpawn().getLocation().clone().add(Math.cos(var), Math.sin(var) + 1, Math.sin(var));
//                    second = getPlayerSpawn().getLocation().clone().add(Math.cos(var2 + Math.PI), Math.sin(var2) + 1,
//                            Math.sin(var2 + Math.PI));
//
//                    // Spawn particles
//                    Objects.requireNonNull(getPlayerSpawn().getLocation().getWorld())
//                            .spawnParticle(Particle.FLAME, first, 0);
//                    getPlayerSpawn().getLocation().getWorld().spawnParticle(Particle.FLAME, second, 0);
//                } catch (Exception e) {
//                    Utils.debugError(String.format("Player spawn particle generation error for arena %d.", arena),
//                            2);
//                }
//            }
//        }, 0 , 2);
//    }
//
//    public void cancelSpawnParticles() {
//        if (getPlayerSpawn() == null)
//            return;
//
//        getPlayerSpawn().turnOffIndicator();
//
//        if (playerParticlesID != 0)
//            Bukkit.getScheduler().cancelTask(playerParticlesID);
//        playerParticlesID = 0;
//    }
    
    public boolean hasBorderParticles() {
        return config.getBoolean(path + ".particles.border");
    }

    public void setBorderParticles(boolean bool) {
        config.set(path + ".particles.border", bool);
        plugin.saveArenaData();
    }

    public void startBorderParticles() {
        if (cornerParticlesID == 0 && getCorner1() != null && getCorner2() != null)
            cornerParticlesID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                World world;
                Location first, second;

                @Override
                public void run() {
                    // Spawn particles
                    try {
                        world = getCorner1().getWorld();
                        assert world != null;

                        first = new Location(world, Math.max(getCorner1().getX(), getCorner2().getX()),
                                Math.max(getCorner1().getY(), getCorner2().getY()),
                                Math.max(getCorner1().getZ(), getCorner2().getZ()));
                        second = new Location(world, Math.min(getCorner1().getX(), getCorner2().getX()),
                                Math.min(getCorner1().getY(), getCorner2().getY()),
                                Math.min(getCorner1().getZ(), getCorner2().getZ()));

                        for (double x = second.getX(); x <= first.getX(); x += 5)
                            for (double y = second.getY(); y <= first.getY(); y += 5)
                                world.spawnParticle(Particle.BARRIER, x, y, first.getZ(), 0);
                        for (double x = second.getX(); x <= first.getX(); x += 5)
                            for (double y = second.getY(); y <= first.getY(); y += 5)
                                world.spawnParticle(Particle.BARRIER, x, y, second.getZ(), 0);
                        for (double x = second.getX(); x <= first.getX(); x += 5)
                            for (double z = second.getZ(); z <= first.getZ(); z += 5)
                                world.spawnParticle(Particle.BARRIER, x, first.getY(), z, 0);
                        for (double x = second.getX(); x <= first.getX(); x += 5)
                            for (double z = second.getZ(); z <= first.getZ(); z += 5)
                                world.spawnParticle(Particle.BARRIER, x, second.getY(), z, 0);
                        for (double z = second.getZ(); z <= first.getZ(); z += 5)
                            for (double y = second.getY(); y <= first.getY(); y += 5)
                                world.spawnParticle(Particle.BARRIER, first.getX(), y, z, 0);
                        for (double z = second.getZ(); z <= first.getZ(); z += 5)
                            for (double y = second.getY(); y <= first.getY(); y += 5)
                                world.spawnParticle(Particle.BARRIER, second.getX(), y, z, 0);

                    } catch (Exception e) {
                        Utils.debugError(String.format("Border particle generation error for arena %d.", arena),
                                1);
                    }
                }
            }, 0 , 20);
    }

    public void cancelBorderParticles() {
        if (cornerParticlesID != 0)
            Bukkit.getScheduler().cancelTask(cornerParticlesID);
        cornerParticlesID = 0;
    }

    private void checkClosedParticles() {
        if (isClosed()) {
//            startSpawnParticles();
            startBorderParticles();
        } else {
//            cancelSpawnParticles();
            cancelBorderParticles();
        }
    }
    
    public Location getCorner1() {
        return Utils.getConfigLocationNoRotation(plugin, path + ".corner1");
    }

    public void setCorner1(Location location) {
        Utils.setConfigurationLocation(plugin, path + ".corner1", location);
    }

    public Location getCorner2() {
        return Utils.getConfigLocationNoRotation(plugin, path + ".corner2");
    }

    public void setCorner2(Location location) {
        Utils.setConfigurationLocation(plugin, path + ".corner2", location);
    }

    public BoundingBox getBounds() {
        return new BoundingBox(getCorner1().getX(), getCorner1().getY(), getCorner1().getZ(),
                getCorner2().getX(), getCorner2().getY(), getCorner2().getZ());
    }

    public boolean hasWinSound() {
        return config.getBoolean(path + ".sounds.win");
    }

    public void setWinSound(boolean bool) {
        config.set(path + ".sounds.win", bool);
        plugin.saveArenaData();
    }

    public boolean hasLoseSound() {
        return config.getBoolean(path + ".sounds.lose");
    }

    public void setLoseSound(boolean bool) {
        config.set(path + ".sounds.lose", bool);
        plugin.saveArenaData();
    }

    public boolean hasWaveStartSound() {
        return config.getBoolean(path + ".sounds.start");
    }

    public void setWaveStartSound(boolean bool) {
        config.set(path + ".sounds.start", bool);
        plugin.saveArenaData();
    }

    public boolean hasWaveFinishSound() {
        return config.getBoolean(path + ".sounds.end");
    }

    public void setWaveFinishSound(boolean bool) {
        config.set(path + ".sounds.end", bool);
        plugin.saveArenaData();
    }
    
    public boolean isClosed() {
        return config.getBoolean(path + ".closed");
    }

    public void setClosed(boolean closed) {
        config.set(path + ".closed", closed);
        plugin.saveArenaData();
//        refreshPortal();
        checkClosedParticles();
    }

//    public List<ArenaRecord> getArenaRecords() {
//        List<ArenaRecord> arenaRecords = new ArrayList<>();
//        if (config.contains(path + ".records"))
//            try {
//                Objects.requireNonNull(config.getConfigurationSection(path + ".records")).getKeys(false)
//                        .forEach(index -> arenaRecords.add(new ArenaRecord(
//                                config.getInt(path + ".records." + index + ".wave"),
//                                config.getStringList(path + ".records." + index + ".players")
//                        )));
//            } catch (Exception e) {
//                Utils.debugError(
//                        String.format("Attempted to retrieve arena records for arena %d but found none.", arena),
//                        2);
//            }
//
//        return arenaRecords;
//    }
//
//    public List<ArenaRecord> getSortedDescendingRecords() {
//        return getArenaRecords().stream().filter(Objects::nonNull)
//                .sorted(Comparator.comparingInt(ArenaRecord::getWave).reversed())
//                .collect(Collectors.toList());
//    }
//
//    public boolean checkNewRecord(ArenaRecord record) {
//        List<ArenaRecord> records = getArenaRecords();
//
//        // Automatic record
//        if (records.size() < 4)
//            records.add(record);
//
//        // New record
//        else if (records.stream().filter(Objects::nonNull)
//                .anyMatch(arenaRecord -> arenaRecord.getWave() < record.getWave())) {
//            records.sort(Comparator.comparingInt(ArenaRecord::getWave));
//            records.set(0, record);
//        }
//
//        // No record
//        else return false;
//
//        // Save data
//        for (int i = 0; i < records.size(); i++) {
//            config.set(path + ".records." + i + ".wave", records.get(i).getWave());
//            config.set(path + ".records." + i + ".players", records.get(i).getPlayers());
//        }
//        plugin.saveArenaData();
//        return true;
//    }

    public Tasks getTask() {
        return task;
    }
    
    public ArenaStatus getStatus() {
        return status;
    }

    public void setStatus(ArenaStatus status) {
        this.status = status;
//        refreshPortal();
    }

    public int getGameID() {
        return gameID;
    }

    public void newGameID() {
        gameID = (int) (100 * Math.random());
    }

    /**
     * @return A list of all {@link GPlayer} in this arena.
     */
    public List<GPlayer> getPlayers() {
        return players;
    }

    /**
     * A function to get the corresponding {@link GPlayer} in the arena for a given {@link Player}.
     * @param player The {@link Player} in question.
     * @return The corresponding {@link GPlayer}.
     * @throws PlayerNotFoundException Thrown when the arena doesn't have a corresponding {@link GPlayer}.
     */
    public GPlayer getPlayer(Player player) throws PlayerNotFoundException {
        try {
            return players.stream().filter(Objects::nonNull).filter(p -> p.getID().equals(player.getUniqueId()))
                    .collect(Collectors.toList())
                    .get(0);
        } catch (Exception e) {
            throw new PlayerNotFoundException("Player not in this arena.");
        }
    }

    /**
     * Checks whether there is a corresponding {@link GPlayer} for a given {@link Player}.
     * @param player The {@link Player} in question.
     * @return Whether a corresponding {@link GPlayer} was found.
     */
    public boolean hasPlayer(Player player) {
        try {
            return players.stream().filter(Objects::nonNull).anyMatch(p -> p.getID().equals(player.getUniqueId()));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasPlayer(GPlayer player) {
        return players.stream().filter(Objects::nonNull).anyMatch(p -> p.equals(player));
    }

    public BossBar getTimeLimitBar() {
        return timeLimitBar;
    }

//    /**
//     * Create a time limit bar to display.
//     */
//    public void startTimeLimitBar() {
//        try {
//            timeLimitBar = Bukkit.createBossBar(Utils.format(
//                    String.format(Objects.requireNonNull(plugin.getLanguageData().getString("timeBar")),
//                            getCurrentWave())),
//                    BarColor.YELLOW, BarStyle.SOLID);
//        } catch (Exception e) {
//            Utils.debugError("The active language file is missing text for the key 'timeBar'.", 1);
//        }
//    }

    /**
     * Updates the time limit bar's progress.
     * @param progress The bar's new progress.
     */
    public void updateTimeLimitBar(double progress) {
        timeLimitBar.setProgress(progress);
    }

    /**
     * Updates the time limit bar's color and progress.
     * @param color The bar's new color.
     * @param progress The bar's new progress.
     */
    public void updateTimeLimitBar(BarColor color, double progress) {
        timeLimitBar.setColor(color);
        timeLimitBar.setProgress(progress);
    }

    /**
     * Removes the time limit bar from every player.
     */
    public void removeTimeLimitBar() {
        players.forEach(vdPlayer -> timeLimitBar.removePlayer(vdPlayer.getPlayer()));
        timeLimitBar = null;
    }

    /**
     * Displays the time limit bar to a player.
     * @param player {@link Player} to display the time limit bar to.
     */
    public void addPlayerToTimeLimitBar(Player player) {
        if (timeLimitBar != null)
            timeLimitBar.addPlayer(player);
    }

    /**
     * Removes the time limit bar from a player's display.
     * @param player {@link Player} to remove the time limit bar from.
     */
    public void removePlayerFromTimeLimitBar(Player player) {
        if (timeLimitBar != null && player != null)
            timeLimitBar.removePlayer(player);
    }

    /**
     * Checks and closes an arena if the arena does not meet opening requirements.
     */
    public void checkClose() {
        if (!plugin.getArenaData().contains("lobby") ||
//                getPortalLocation() == null || getPlayerSpawn() == null ||
                getCorner1() == null || getCorner2() == null ||
                !Objects.equals(getCorner1().getWorld(), getCorner2().getWorld())) {
            setClosed(true);
            Utils.debugInfo(String.format("Arena %d did not meet opening requirements and was closed.", arena),
                    2);
        }
    }

    /**
     * Copies permanent arena characteristics from an existing arena and saves the change to the arena file.
     * @param arenaToCopy The arena to copy characteristics from.
     */
    public void copy(Arena arenaToCopy) {
        setMaxPlayers(arenaToCopy.getMaxPlayers());
        setMinPlayers(arenaToCopy.getMinPlayers());
        setWaveTimeLimit(arenaToCopy.getWaveTimeLimit());
        setDifficultyLabel(arenaToCopy.getDifficultyLabel());
        setWinSound(arenaToCopy.hasWinSound());
        setLoseSound(arenaToCopy.hasLoseSound());
        setWaveStartSound(arenaToCopy.hasWaveStartSound());
        setWaveFinishSound(arenaToCopy.hasWaveFinishSound());
        setWaitingSound(arenaToCopy.getWaitingSoundNum());
        setSpawnParticles(arenaToCopy.hasSpawnParticles());
        setBorderParticles(arenaToCopy.hasBorderParticles());
        if (config.contains("a" + arenaToCopy.getArena() + ".customShop"))
            try {
                Objects.requireNonNull(config.getConfigurationSection("a" + arenaToCopy.getArena() + ".customShop"))
                        .getKeys(false)
                        .forEach(index -> config.set(path + ".customShop." + index,
                                config.getItemStack("a" + arenaToCopy.getArena() + ".customShop." + index)));
                plugin.saveArenaData();
            } catch (Exception e) {
                Utils.debugError(
                        String.format("Attempted to retrieve the custom shop inventory of arena %d but found none.",
                                arena), 1);
            }

        Utils.debugInfo(
                String.format("Copied the characteristics of arena %d to arena %d.", arenaToCopy.getArena(), arena),
                2);
    }

    /**
     * Removes all data of this arena from the arena file.
     */
    public void remove() {
//        removeArenaBoard();
//        removePortal();
        config.set(path, null);
        plugin.saveArenaData();
        Utils.debugInfo(String.format("Removing arena %d.", arena), 1);
    }
}
