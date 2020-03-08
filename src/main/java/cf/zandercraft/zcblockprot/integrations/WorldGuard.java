package cf.zandercraft.zcblockprot.integrations;

import cf.zandercraft.zcblockprot.wrappers.Plugin;
import cf.zandercraft.zcblockprot.wrappers.Block;
import cf.zandercraft.zcblockprot.wrappers.bukkit.BukkitPlugin;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.World;


/**
 * WorldGuard integration, which is used to check if a block is within a
 * region that protection has been disabled in
 */
public class WorldGuard {
    private final Plugin plugin;

    public WorldGuard(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Check whether a block is in a region we don't check for protection in
     * @param block The block to check
     * @return true if the region exists and has protection disabled, false otherwise
     */
    public boolean isInIgnoredRegion(Block block) {
        if (this.plugin instanceof BukkitPlugin) {
            Vector3 point = BukkitAdapter.adapt(((org.bukkit.block.Block) block.getWrapped()).getLocation());
            RegionManager regionManager = ((BukkitPlugin) this.plugin).getWorldGuard().getRegionManager((World) block.getWorld().getWrapped());
            ApplicableRegionSet set = regionManager.getApplicableRegions(point);

            Boolean result = set.getFlag(BukkitPlugin.bypassProtectionFlag);

            if (result == null) {
                return false;
            }

            return result;
        }

        return false;
    }
}
