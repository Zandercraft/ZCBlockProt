package cf.zandercraft.zcblockprot.bukkit.commands;

import cf.zandercraft.zcblockprot.runnables.ConsoleRelayRunnable;
import cf.zandercraft.zcblockprot.runnables.RelayRunnable;
import cf.zandercraft.zcblockprot.runnables.database.commands.DisownBlocksPlayerThread;
import cf.zandercraft.zcblockprot.wrappers.bukkit.BukkitPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DisownPlayerCommand implements CommandExecutor {
    private final BukkitPlugin plugin;

    public DisownPlayerCommand(BukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("archblock.admin")) {
            sender.sendMessage(this.plugin.getPrefixedLocalisedString("command_no_permission"));
        } else {
            if (args.length < 1) {
                sender.sendMessage(this.plugin.getPrefixedLocalisedString("disown_command_usage", label));
            } else {
                if (this.plugin.isTaskRunning()) {
                    sender.sendMessage(this.plugin.getPrefixedLocalisedString("task_already_running"));
                    return true;
                }

                UUID uuid = this.plugin.getApi().getUuidForUsername(args[0]);

                if (uuid == null) {
                    sender.sendMessage(this.plugin.getPrefixedLocalisedString("unknown_player", args[0]));
                } else {
                    RelayRunnable callback;

                    if (sender instanceof Player) {
                        callback = new RelayRunnable(this.plugin, sender.getName());
                    } else if (sender instanceof ConsoleCommandSender) {
                        callback = new ConsoleRelayRunnable(this.plugin);
                    } else {
                        sender.sendMessage(this.plugin.getLocalisedString("player_or_console_only"));
                        return true;
                    }

                    new DisownBlocksPlayerThread(this.plugin, uuid, callback).start();

                    sender.sendMessage(
                            this.plugin.getPrefixedLocalisedString("disown_command_disowning_blocks", args[0])
                    );
                }
            }
        }

        return true;
    }
}
