package com.archivesmc.archblock.commands;

import com.archivesmc.archblock.Plugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class UnfriendCommand implements CommandExecutor {
    private Plugin plugin;

    public UnfriendCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(String.format(
                    "%s[%sArchBlock%s]%s Usage: %s/%s%s %s<user>",
                    ChatColor.LIGHT_PURPLE, ChatColor.GOLD, ChatColor.LIGHT_PURPLE,
                    ChatColor.BLUE, ChatColor.AQUA, ChatColor.DARK_AQUA,
                    label, ChatColor.DARK_GREEN
            ));
        } else {
            String player;
            String friend;

            if (sender instanceof Player) {
                player = ((Player) sender).getUniqueId().toString();
                friend = this.plugin.getApi().getUuidForUsername(args[0]);

                if (friend == null) {
                    sender.sendMessage(String.format(
                            "%s[%sArchBlock%s]%s Unknown player: %s%s",
                            ChatColor.LIGHT_PURPLE, ChatColor.GOLD, ChatColor.LIGHT_PURPLE,
                            ChatColor.RED, ChatColor.AQUA, args[0]
                    ));
                } else {
                    if (! this.plugin.getApi().hasFriendship(UUID.fromString(player), UUID.fromString(friend))) {
                        sender.sendMessage(String.format(
                                "%s[%sArchBlock%s]%s You are not friends with %s%s",
                                ChatColor.LIGHT_PURPLE, ChatColor.GOLD, ChatColor.LIGHT_PURPLE,
                                ChatColor.RED, ChatColor.AQUA, args[0]
                        ));
                    } else {
                        this.plugin.getApi().destroyFriendship(UUID.fromString(player), UUID.fromString(friend));

                        sender.sendMessage(String.format(
                                "%s[%sArchBlock%s]%s You are no longer friends with %s%s",
                                ChatColor.LIGHT_PURPLE, ChatColor.GOLD, ChatColor.LIGHT_PURPLE,
                                ChatColor.GREEN, ChatColor.AQUA, args[0]
                        ));
                    }
                }
            } else {
                if (args.length < 2) {
                    sender.sendMessage(String.format(
                            "%s[%sArchBlock%s]%s Usage: %s/%s%s %s<user> <friend>",
                            ChatColor.LIGHT_PURPLE, ChatColor.GOLD, ChatColor.LIGHT_PURPLE,
                            ChatColor.BLUE, ChatColor.AQUA, ChatColor.DARK_AQUA,
                            label, ChatColor.DARK_GREEN
                    ));
                } else {
                    player = this.plugin.getApi().getUuidForUsername(args[0]);
                    friend = this.plugin.getApi().getUuidForUsername(args[1]);

                    if (player == null) {
                        sender.sendMessage(String.format(
                                "%s[%sArchBlock%s]%s Unknown player: %s%s",
                                ChatColor.LIGHT_PURPLE, ChatColor.GOLD, ChatColor.LIGHT_PURPLE,
                                ChatColor.RED, ChatColor.AQUA, args[0]
                        ));
                    } else if (friend == null) {
                        sender.sendMessage(String.format(
                                "%s[%sArchBlock%s]%s Unknown player: %s%s",
                                ChatColor.LIGHT_PURPLE, ChatColor.GOLD, ChatColor.LIGHT_PURPLE,
                                ChatColor.RED, ChatColor.AQUA, args[1]
                        ));
                    } else {
                        if (! this.plugin.getApi().hasFriendship(UUID.fromString(player), UUID.fromString(friend))) {
                            sender.sendMessage(String.format(
                                    "%s[%sArchBlock%s]%s %s%s is not friends with %s%s",
                                    ChatColor.LIGHT_PURPLE, ChatColor.GOLD, ChatColor.LIGHT_PURPLE,
                                    ChatColor.AQUA, args[0], ChatColor.RED, ChatColor.AQUA, args[1]
                            ));
                        } else {
                            this.plugin.getApi().destroyFriendship(UUID.fromString(player), UUID.fromString(friend));

                            sender.sendMessage(String.format(
                                    "%s[%sArchBlock%s]%s %s%s is no longer friends with %s%s",
                                    ChatColor.LIGHT_PURPLE, ChatColor.GOLD, ChatColor.LIGHT_PURPLE,
                                    ChatColor.AQUA, args[0], ChatColor.GREEN, ChatColor.AQUA, args[1]
                            ));
                        }
                    }
                }
            }
        }

        return true;
    }
}