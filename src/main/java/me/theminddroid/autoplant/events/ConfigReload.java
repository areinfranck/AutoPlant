package me.theminddroid.autoplant.events;

import me.theminddroid.autoplant.AutoPlant;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ConfigReload implements CommandExecutor
{
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!command.getName().equalsIgnoreCase("autoplant")) return true;

        if (args.length == 0)
        {
            sender.sendMessage(ChatColor.RED + "Please enter an argument...");
            return true;
        }

        if (!args[0].equalsIgnoreCase("reload"))
        {
            sender.sendMessage(ChatColor.RED + "Unknown command. Try /autoplant reload");
            return true;
        }

        if (!sender.hasPermission("autoplant.reload"))
        {
            sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
            return true;
        }

        try
        {
            AutoPlant.getPlugin(AutoPlant.class).reloadConfig();
            sender.sendMessage(ChatColor.DARK_GREEN + "AutoPlant config successfully reloaded!");
        }
        catch (Exception e)
        {
            sender.sendMessage(ChatColor.RED + "Unable to reload config file. Does the file exist?");
            Bukkit.getLogger().warning("[AutoPlant]: Unable to reload plugin: " + e);
        }
        return true;
    }
}