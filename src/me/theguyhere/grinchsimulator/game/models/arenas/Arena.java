package me.theguyhere.grinchsimulator.game.models.arenas;

import me.theguyhere.grinchsimulator.GUI.Inventories;
import me.theguyhere.grinchsimulator.Main;
import me.theguyhere.grinchsimulator.exceptions.InvalidNameException;
import me.theguyhere.grinchsimulator.exceptions.PlayerNotFoundException;
import me.theguyhere.grinchsimulator.game.displays.Portal;
import me.theguyhere.grinchsimulator.game.models.Tasks;
import me.theguyhere.grinchsimulator.game.models.players.GPlayer;
import me.theguyhere.grinchsimulator.game.models.presents.PresentType;
import me.theguyhere.grinchsimulator.tools.Utils;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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
    /** A list of players in the arena.*/
    private final List<GPlayer> players = new ArrayList<>();
    /** Time limit bar object.*/
    private BossBar timeLimitBar;
    /** Portal object for the arena.*/
    private Portal portal;
//    /** Arena scoreboard object for the arena.*/
//    private ArenaBoard arenaBoard;
    /** A list of players that are editing the map.*/
    private final List<Player> editors = new ArrayList<>();
    /** A list of states of hidden presents.*/
    private final List<Location> found = new ArrayList<>();
    /** ID of task managing present particles.*/
    private int presentParticlesID = 0;

    public Arena(Main plugin, int arena, Tasks task) {
        this.plugin = plugin;
        config = plugin.getArenaData();
        this.arena = arena;
        path = "a" + arena;
        this.task = task;
        status = ArenaStatus.WAITING;
//        refreshArenaBoard();
        refreshPortal();
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
            setWaitingSound(14);
        }

        // Refresh portal
        if (getPortalLocation() != null)
            refreshPortal();
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

    public void startPresentParticles() {
        if (presentParticlesID != 0)
            return;

        presentParticlesID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            try {
                found.forEach(location -> Objects.requireNonNull(location.getWorld())
                        .spawnParticle(Particle.BARRIER, location.clone().add(.5, .25, .5), 1));
                System.out.println(found);
            } catch (Exception e) {
                Utils.debugError(String.format("Present particle generation error for arena %d.", arena),
                        2);
            }
        }, 0 , 80);
    }

    public void cancelPresentParticles() {
        if (presentParticlesID != 0)
            Bukkit.getScheduler().cancelTask(presentParticlesID);
        presentParticlesID = 0;
    }

    public void addPresent(PresentType type, Location location) {
        List<Location> newList = getPresents(type);
        if (newList == null)
            newList = new ArrayList<>();
        newList.add(location);
        plugin.getArenaData().set(path + ".presents." + type.label, newList);
        plugin.saveArenaData();
    }

    public boolean checkPresent(Location location) {
        try {
            if (Objects.requireNonNull(plugin.getArenaData().getConfigurationSection(path + ".presents"))
                    .getKeys(false).stream().anyMatch(type -> Utils.getConfigLocationList(plugin,
                            path + ".presents." + type).stream().anyMatch(loc -> {
                        loc.setYaw(0);
                        return loc.equals(location);
                    })))
                return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    public void findPresent(Location location) {
        found.add(location.add(-.5, -.5, -.5));
        Objects.requireNonNull(location.getWorld()).spawnParticle(Particle.BARRIER,
                location.clone().add(1, .75, 1), 1);
    }

    public boolean checkFound(Location location) {
        System.out.println(found.contains(location));
        return found.contains(location);
    }

    public void resetPresents() {
        found.clear();
    }

    private void safeRemovePresent(Location location, PresentType type) {
        // Remove from server
        location.getBlock().setType(Material.AIR);

        // Remove from files
        List<Location> newList = getPresents(type);
        newList.remove(location);
        plugin.getArenaData().set(path + ".presents." + type.label, newList);
        plugin.saveArenaData();
    }

    public boolean removePresent(Location location) {
        try {
            if (checkPresent(location)) {
                String presentType = Objects.requireNonNull(plugin.getArenaData()
                                .getConfigurationSection(path + ".presents"))
                        .getKeys(false).stream().filter(type -> Utils.getConfigLocationList(plugin,
                                path + ".presents." + type).stream().anyMatch(loc -> {
                            loc.setYaw(0);
                            return loc.equals(location);
                        })).toList().get(0).toUpperCase();
                safeRemovePresent(location, PresentType.valueOf(presentType));
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public void removePresents(PresentType type) {
        try {
            Objects.requireNonNull(plugin.getArenaData().getList(path + ".presents." + type.label))
                    .forEach(location -> safeRemovePresent((Location) location, type));
        } catch (Exception ignored) { }
    }

    public void removeAllPresents() {
        removePresents(PresentType.WOOD);
        removePresents(PresentType.STONE);
        removePresents(PresentType.IRON);
        removePresents(PresentType.COPPER);
        removePresents(PresentType.GOLD);
        removePresents(PresentType.DIAMOND);
        removePresents(PresentType.EMERALD);
        removePresents(PresentType.NETHERITE);
        removePresents(PresentType.BLACK);
        removePresents(PresentType.BROWN);
        removePresents(PresentType.RED);
        removePresents(PresentType.ORANGE);
        removePresents(PresentType.YELLOW);
        removePresents(PresentType.GREEN);
        removePresents(PresentType.CYAN);
        removePresents(PresentType.BLUE);
        removePresents(PresentType.PURPLE);
        removePresents(PresentType.PINK);
        removePresents(PresentType.WHITE);
    }

    public List<Location> getPresents(PresentType type) {
        return Utils.getConfigLocationList(plugin, path + ".presents." + type.label);
    }

    public int getPresentValue(PresentType type) {
        String newPath = path + ".presentValues." + type.label;
        if (plugin.getArenaData().contains(newPath))
            return plugin.getArenaData().getInt(newPath);
        else return 1;
    }

    public void setPresentValue(PresentType type, int value) {
        plugin.getArenaData().set(path + ".presentValues." + type.label, value);
        plugin.saveArenaData();
    }

    public void resetPresentValues() {
        plugin.getArenaData().set(path + ".presentValues", null);
        plugin.saveArenaData();
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

    public Portal getPortal() {
        return portal;
    }

    public Location getPortalLocation() {
        return Utils.getConfigLocationNoPitch(plugin, path + ".portal");
    }

    /**
     * Creates a new portal at the given location and deletes the old portal.
     * @param location New location
     */
    public void setPortal(Location location) {
        // Save config location
        Utils.setConfigurationLocation(plugin, path + ".portal", location);

        // Recreate the portal
        refreshPortal();
    }

    /**
     * Recreates the portal in game based on the location in the arena file.
     */
    public void refreshPortal() {
        // Try recreating the portal
        try {
            // Delete old portal if needed
            if (portal != null)
                portal.remove();

            // Create a new portal and display it
            portal = new Portal(Objects.requireNonNull(Utils.getConfigLocationNoPitch(plugin, path + ".portal")),
                    this);
            portal.displayForOnline();
        } catch (Exception e) {
            Utils.debugError("Invalid location for arena board " + arena, 1);
            Utils.debugInfo("Portal location data may be corrupt. If data cannot be manually corrected in " +
                    "arenaData.yml, please delete the portal location data for arena " + arena + ".", 1);
        }
    }

    /**
     * Centers the portal location along the x and z axis.
     */
    public void centerPortal() {
        // Center the location
        Utils.centerConfigLocation(plugin, path + ".portal");

        // Recreate the portal
        refreshPortal();
    }

    /**
     * Removes the portal from the game and from the arena file.
     */
    public void removePortal() {
        if (portal != null) {
            portal.remove();
            portal = null;
        }
        Utils.setConfigurationLocation(plugin, path + ".portal", null);
        checkClose();
    }

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

    public Location getPlayerSpawn() {
        return Utils.getConfigLocationNoPitch(plugin, path + ".spawn");
    }

    /**
     * Writes the new player spawn location of the arena into the arena file.
     * @param location New player spawn location.
     */
    public void setPlayerSpawn(Location location) {
        Utils.setConfigurationLocation(plugin, path + ".spawn", location);
    }

    /**
     * Centers the player spawn location of the arena along the x and z axis.
     */
    public void centerPlayerSpawn() {
        Utils.centerConfigLocation(plugin, path + ".spawn");
    }

    /**
     * Retrieves the waiting room location of the arena from the arena file.
     * @return Player spawn location.
     */
    public Location getWaitingRoom() {
        return Utils.getConfigLocation(plugin, path + ".waiting");
    }

    /**
     * Writes the new waiting room location of the arena into the arena file.
     * @param location New player spawn location.
     */
    public void setWaitingRoom(Location location) {
        Utils.setConfigurationLocation(plugin, path + ".waiting", location);
    }

    /**
     * Centers the waiting room location of the arena along the x and z axis.
     */
    public void centerWaitingRoom() {
        Utils.centerConfigLocation(plugin, path + ".waiting");
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

    public boolean isClosed() {
        return config.getBoolean(path + ".closed");
    }

    public void setClosed(boolean closed) {
        config.set(path + ".closed", closed);
        plugin.saveArenaData();
        refreshPortal();
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
        refreshPortal();
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
                    .toList().get(0);
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

    /**
     * Create a time limit bar to display.
     */
    public void startTimeLimitBar() {
        try {
            timeLimitBar = Bukkit.createBossBar(
                    Utils.format(Objects.requireNonNull(plugin.getLanguageData().getString("timeBar"))),
                    BarColor.YELLOW, BarStyle.SOLID);
        } catch (Exception e) {
            Utils.debugError("The active language file is missing text for the key 'timeBar'.", 1);
        }
    }

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

    public void addEditor(Player player) {
        // Save player inventory before going into editor mode, then clear inventory
        for (int i = 0; i < player.getInventory().getContents().length; i++)
            plugin.getPlayerData().set(player.getName() + ".inventory." + i, player.getInventory().getContents()[i]);
        plugin.savePlayerData();
        player.getInventory().clear();

        editors.add(player);

        // Set initial editor hotbar
        Inventories.setEditorHotbar(player, 0);
    }

    public void removeEditor(Player player) {
        // Return player inventory before going into editor mode
        player.getInventory().clear();
        if (plugin.getPlayerData().contains(player.getName() + ".inventory"))
            Objects.requireNonNull(plugin.getPlayerData()
                            .getConfigurationSection(player.getName() + ".inventory"))
                    .getKeys(false)
                    .forEach(num -> player.getInventory().setItem(Integer.parseInt(num),
                            (ItemStack) plugin.getPlayerData().get(player.getName() + ".inventory." + num)));
        plugin.getPlayerData().set(player.getName() + ".inventory", null);
        plugin.savePlayerData();

        editors.remove(player);
    }

    public boolean hasEditor(Player player) {
        return editors.contains(player);
    }

    /**
     * Checks and closes an arena if the arena does not meet opening requirements.
     */
    public void checkClose() {
        if (!plugin.getArenaData().contains("lobby") ||
                getPortalLocation() == null ||
                getPlayerSpawn() == null) {
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
        setWinSound(arenaToCopy.hasWinSound());
        setLoseSound(arenaToCopy.hasLoseSound());
        setWaveStartSound(arenaToCopy.hasWaveStartSound());
        setWaitingSound(arenaToCopy.getWaitingSoundNum());
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
        removePortal();
        config.set(path, null);
        plugin.saveArenaData();
        Utils.debugInfo(String.format("Removing arena %d.", arena), 1);
    }
}
