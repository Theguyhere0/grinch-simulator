package me.theguyhere.grinchsimulator;

import me.theguyhere.grinchsimulator.tools.DataManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    // Yaml file managers
    private final DataManager arenaData = new DataManager(this, "arenaData.yml");
    private final DataManager playerData = new DataManager(this, "playerData.yml");
    private final DataManager languageData = new DataManager(this, "languages/" +
            getConfig().getString("locale") + ".yml");

    /**
     * The amount of debug information to display in the console.
     *
     * 3 (Override) - All errors and information tracked will be displayed. Certain behavior will be overridden.
     * 2 (Verbose) - All errors and information tracked will be displayed.
     * 1 (Normal) - Errors that drastically reduce performance and important information will be displayed.
     * 0 (Quiet) - Only the most urgent error messages will be displayed.
     */
    private static int debugLevel = 0;

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

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

    public static int getDebugLevel() {
        return debugLevel;
    }

    public static void setDebugLevel(int newDebugLevel) {
        debugLevel = newDebugLevel;
    }
}
