package cf.zandercraft.zcblockprot.bukkit.commands;

import cf.zandercraft.zcblockprot.wrappers.Block;
import cf.zandercraft.zcblockprot.runnables.RelayRunnable;
import cf.zandercraft.zcblockprot.runnables.database.commands.MassOwnershipChanger;
import cf.zandercraft.zcblockprot.wrappers.bukkit.BukkitBlock;
import cf.zandercraft.zcblockprot.wrappers.bukkit.BukkitPlugin;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

/**
 * The command handler for the /setowner command for setting the owner
 * of a large number of blocks, contained within a WorldEdit selection
 */
public class SetOwnerCommand implements CommandExecutor {
    private final BukkitPlugin plugin;

    public SetOwnerCommand(BukkitPlugin plugin) {
        this.plugin = plugin;
    }

    public static org.bukkit.Location getLocation(World world, Vector vec) {
        if (vec == null) {
            return null;
        }
        return new Location(world, vec.getX(), vec.getY(), vec.getZ());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("archblock.admin")) {
            sender.sendMessage(this.plugin.getPrefixedLocalisedString("command_no_permission"));
        } else {
            if (args.length < 1) {
                sender.sendMessage(this.plugin.getPrefixedLocalisedString("setowner_command_usage", label));
                sender.sendMessage(this.plugin.getPrefixedLocalisedString("setowner_command_usage_exclamation_point"));
            } else if (sender instanceof Player) {
                if (this.plugin.isTaskRunning()) {
                    sender.sendMessage(this.plugin.getPrefixedLocalisedString("task_already_running"));
                    return true;
                }

                UUID target = this.plugin.getApi().getUuidForUsername(args[0]);

                if (target == null && !("!".equals(args[0]))) {
                    sender.sendMessage(this.plugin.getPrefixedLocalisedString("unknown_player", args[0]));
                } else {
                    Region selection = null;
                    try {
                        selection = this.plugin.getWorldEdit().getSession((Player) sender).getSelection(new BukkitWorld(((Player)sender).getWorld()));
                    } catch (IncompleteRegionException e) {
                        e.printStackTrace();
                    }
                    ;

                    if (selection instanceof CuboidRegion) {
                        CuboidRegion cs = (CuboidRegion) selection;

                        ArrayList<Block> blocks = new ArrayList<>();

                        Location min = getLocation(((Player)sender).getWorld(), cs.getMinimumPoint());
                        Location max = getLocation(((Player)sender).getWorld(), cs.getMaximumPoint());

                        int minX = min.getBlockX();
                        int minY = min.getBlockY();
                        int minZ = min.getBlockZ();

                        int maxX = max.getBlockX();
                        int maxY = max.getBlockY();
                        int maxZ = max.getBlockZ();

                        int i, j, k;

                        for (i = minX; i <= maxX; i += 1) {
                            for (j = minY; j <= maxY; j += 1) {
                                for (k = minZ; k <= maxZ; k += 1) {
                                    Block b = new BukkitBlock(((Player) sender).getWorld().getBlockAt(i, j, k));
                                    if (((org.bukkit.block.Block) b.getWrapped()).getType() != Material.AIR) {
                                        blocks.add(b);
                                    }
                                }
                            }
                        }

                        RelayRunnable finishRunnable = new RelayRunnable(this.plugin, sender.getName(), this.plugin.getPrefixedLocalisedString(
                                "ownership_blocks_changed", blocks.size()
                        ));

                        MassOwnershipChanger changer = new MassOwnershipChanger(
                                this.plugin, blocks, target, finishRunnable
                        );

                        changer.start();

                        sender.sendMessage(this.plugin.getPrefixedLocalisedString("setowner_command_changing_ownership", blocks.size()));
                    } else {
                        sender.sendMessage(this.plugin.getPrefixedLocalisedString("setowner_command_make_worldedit_selection"));
                    }
                }
            } else {
                sender.sendMessage(this.plugin.getPrefixedLocalisedString("player_only"));
            }
        }

        return true;
    }
}
