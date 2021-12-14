package me.theguyhere.grinchsimulator;

import me.theguyhere.grinchsimulator.game.models.arenas.Arena;
import me.theguyhere.grinchsimulator.game.models.arenas.ArenaManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CommandTab implements TabCompleter {
    private final String[] arguments = {"admin", "start", "end", "delay", "fix", "debug"};

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                      @NotNull String label, String[] args) {
        // Complete as characters are added
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            for (String a : arguments)
                if (a.toLowerCase().startsWith(args[0].toLowerCase()))
                    result.add(a);
//        } else if (args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase("crystals")
//                && args.length == 2) {
//            Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).forEach(name -> {
//                if (name.toLowerCase().startsWith(args[1].toLowerCase()))
//                    result.add(name);
//            });
        } else if (args[0].equalsIgnoreCase("start") || args[0].equalsIgnoreCase("end")) {
            StringBuilder nameFrag = new StringBuilder(args[1]);
            for (int i = 0; i < args.length - 2; i++)
                nameFrag.append(" ").append(args[i + 2]);
            Arrays.stream(ArenaManager.getArenas()).filter(Objects::nonNull).map(Arena::getName).forEach(name -> {
                if (name.toLowerCase().startsWith(nameFrag.toString()))
                    result.add(name);
            });
        } else if (args[0].equalsIgnoreCase("debug"))
            for (int i = 0; i < 4; i++)
                result.add(String.valueOf(i));

        return result;
    }
}
