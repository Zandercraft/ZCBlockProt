package com.archivesmc.archblock.commands;

import com.archivesmc.archblock.Plugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SetOwnerCommand implements CommandExecutor {
    private Plugin plugin;

    public SetOwnerCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(String.format(
                "%s[%sWatchBlock%s]%s This command hasn't been implemented yet.",
                ChatColor.LIGHT_PURPLE, ChatColor.GOLD, ChatColor.LIGHT_PURPLE, ChatColor.RED
        ));

        return true;
    }
}
