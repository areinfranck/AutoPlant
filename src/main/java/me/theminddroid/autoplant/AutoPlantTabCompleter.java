package me.theminddroid.autoplant;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class AutoPlantTabCompleter implements TabCompleter {

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            return null;
        }

        final ArrayList<String> result = new ArrayList<>();
        result.add("reload");

        return result;
    }
}
