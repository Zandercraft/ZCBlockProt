package cf.zandercraft.zcblockprot.wrappers.bukkit;

import cf.zandercraft.zcblockprot.wrappers.Player;
import cf.zandercraft.zcblockprot.wrappers.Server;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class BukkitServer implements Server {
    private org.bukkit.Server wrappedServer;

    public BukkitServer(org.bukkit.Server server) {
        this.wrappedServer = server;
    }

    @Override
    public Player getPlayer(String player) {
        return new BukkitPlayer(this.wrappedServer.getPlayer(player));
    }

    @Override
    public Player getPlayer(UUID id) {
        OfflinePlayer p = this.wrappedServer.getOfflinePlayer(id.toString());

        if (p.isOnline()) {
            return new BukkitPlayer(p.getPlayer());
        }

        return null;
    }

    @Override
    public Object getWrapped() {
        return this.wrappedServer;
    }
}
