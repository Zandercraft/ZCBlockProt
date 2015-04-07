package com.archivesmc.archblock.commands;

import com.archivesmc.archblock.Plugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * The command handler for the /friend command for adding friends to
 * one's friends list.
 */
public class FriendCommand implements CommandExecutor {
    private final Plugin plugin;

    public FriendCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(this.plugin.getPrefixedLocalisedString("friend_command_usage", label));
        } else {
            UUID player;
            UUID friend;

            if (sender instanceof Player) {
                player = ((Player) sender).getUniqueId();
                friend = this.plugin.getApi().getUuidForUsername(args[0]);

                if (friend == null) {
                    sender.sendMessage(this.plugin.getPrefixedLocalisedString("unknown_player", args[0]));
                } else {
                    if (this.plugin.getApi().hasFriendship(player, friend)) {
                        sender.sendMessage(
                                this.plugin.getPrefixedLocalisedString("friend_command_already_friends", args[0])
                        );
                    } else {
                        this.plugin.getApi().createFriendship(player, friend);
                        sender.sendMessage(
                                this.plugin.getPrefixedLocalisedString("friend_command_now_friends", args[0])
                        );
                    }
                }
            } else {
                if (args.length < 2) {
                    sender.sendMessage(this.plugin.getPrefixedLocalisedString("friend_command_console_usage", label));
                } else {
                    player = this.plugin.getApi().getUuidForUsername(args[0]);
                    friend = this.plugin.getApi().getUuidForUsername(args[1]);

                    if (player == null) {
                        sender.sendMessage(this.plugin.getPrefixedLocalisedString("unknown_player", args[0]));
                    } else if (friend == null) {
                        sender.sendMessage(this.plugin.getPrefixedLocalisedString("unknown_player", args[1]));
                    } else {
                        if (this.plugin.getApi().hasFriendship(player, friend)) {
                            sender.sendMessage(
                                    this.plugin.getPrefixedLocalisedString("friend_command_console_already_friends", args[0], args[1])
                            );
                        } else {
                            this.plugin.getApi().createFriendship(player, friend);

                            sender.sendMessage(
                                    this.plugin.getPrefixedLocalisedString("friend_command_console_now_friends", args[0], args[1])
                            );
                        }
                    }
                }
            }
        }

        return true;
    }
}
