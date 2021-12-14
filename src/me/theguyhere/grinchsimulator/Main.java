package me.theguyhere.grinchsimulator;

import me.theguyhere.grinchsimulator.game.models.arenas.ArenaManager;
import me.theguyhere.grinchsimulator.listeners.InventoryListener;
import me.theguyhere.grinchsimulator.listeners.JoinListener;
import me.theguyhere.grinchsimulator.packets.PacketReader;
import me.theguyhere.grinchsimulator.tools.DataManager;
import me.theguyhere.grinchsimulator.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Main extends JavaPlugin {
    // Yaml file managers
    private final DataManager arenaData = new DataManager(this, "arenaData.yml");
    private final DataManager playerData = new DataManager(this, "playerData.yml");
    private final DataManager languageData = new DataManager(this, "languages/" +
            getConfig().getString("locale") + ".yml");

    private final PacketReader reader = new PacketReader();
    private ArenaManager arenaManager;

    /**
     * The amount of debug information to display in the console.
     *
     * 3 (Override) - All errors and information tracked will be displayed. Certain behavior will be overridden.
     * 2 (Verbose) - All errors and information tracked will be displayed.
     * 1 (Normal) - Errors that drastically reduce performance and important information will be displayed.
     * 0 (Quiet) - Only the most urgent error messages will be displayed.
     */
    private static int debugLevel = 0;
    private boolean outdated = false;
    int configVersion = 1;
    int arenaDataVersion = 1;
    int playerDataVersion = 1;
    int languageFileVersion = 1;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        PluginManager pm = getServer().getPluginManager();
        arenaManager = new ArenaManager(this);
        Commands commands = new Commands(this);

        // Set up commands and tab complete
        Objects.requireNonNull(getCommand("grinch"), "'grinch' command should exist")
                .setExecutor(commands);
        Objects.requireNonNull(getCommand("grinch"), "'grinch' command should exist")
                .setTabCompleter(new CommandTab());

        // Register event listeners
        pm.registerEvents(new InventoryListener(this), this);
        pm.registerEvents(new JoinListener(this), this);

        // Inject online players into packet reader
        if (!Bukkit.getOnlinePlayers().isEmpty())
            for (Player player : Bukkit.getOnlinePlayers()) {
                reader.inject(player);
            }

        // Check config version
        if (getConfig().getInt("version") < configVersion) {
            Utils.debugError("Your config.yml is outdated!", 0);
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "[GrinchSimulator] " +
                    "Please update to the latest version (" + ChatColor.BLUE + configVersion + ChatColor.RED +
                    ") to ensure compatibility.");
            outdated = true;
        }

        // Check if arenaData.yml is outdated
        if (getConfig().getInt("arenaData") < arenaDataVersion) {
            Utils.debugError("Your arenaData.yml is no longer supported with this version!", 0);
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "[GrinchSimulator] " +
                    "Please manually transfer arena data to version " + ChatColor.BLUE + arenaDataVersion +
                    ChatColor.RED + ".");
            Utils.debugError("Please do not update your config.yml until your arenaData.yml has been updated.",
                    0);
            outdated = true;
        }

        // Check if playerData.yml is outdated
        if (getConfig().getInt("playerData") < playerDataVersion) {
            Utils.debugError("Your playerData.yml is no longer supported with this version!", 0);
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "[GrinchSimulator] " +
                    "Please manually transfer player data to version " + ChatColor.BLUE + playerDataVersion +
                    ChatColor.BLUE + ".");
            Utils.debugError("Please do not update your config.yml until your playerData.yml has been updated.",
                    0);
            outdated = true;
        }

        // Check if language files are outdated
        if (getConfig().getInt("languageFile") < languageFileVersion) {
            Utils.debugError("You language files are no longer supported with this version!", 0);
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "[GrinchSimulator] " +
                    "Please update en_US.yml and update any other language files to version " + ChatColor.BLUE +
                    languageFileVersion + ChatColor.RED + ".");
            Utils.debugError("Please do not update your config.yml until your language files have been updated.",
                    0);
            outdated = true;
        }
    }

    @Override
    public void onDisable() {

    }

    public PacketReader getReader() {
        return reader;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    // Returns arena data
    public FileConfiguration getArenaData() {
        return arenaData.getConfig();
    }

    // Saves arena data changes
    public void saveArenaData() {
        arenaData.saveConfig();
    }

    // Returns player data
    public FileConfiguration getPlayerData() {
        return playerData.getConfig();
    }

    // Saves arena data changes
    public void savePlayerData() {
        playerData.saveConfig();
    }

    public FileConfiguration getLanguageData() {
        return languageData.getConfig();
    }

    public boolean isOutdated() {
        return outdated;
    }

    public static int getDebugLevel() {
        return debugLevel;
    }

    public static void setDebugLevel(int newDebugLevel) {
        debugLevel = newDebugLevel;
    }
}
