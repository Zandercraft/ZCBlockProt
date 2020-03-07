package cf.zandercraft.zcblockprot.bukkit.events;

import cf.zandercraft.zcblockprot.wrappers.bukkit.BukkitPlayer;
import cf.zandercraft.zcblockprot.wrappers.bukkit.BukkitPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * Event handler that gets called when a player connects and puts their
 * UUID into the database
 */
public class PlayerConnectEvent implements Listener{
    private final BukkitPlugin plugin;

    public PlayerConnectEvent(BukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEvent(PlayerLoginEvent event) {
        // This is just to be sure that the UUID has been stored
        this.plugin.getApi().storePlayer(new BukkitPlayer(event.getPlayer()));
    }
}
