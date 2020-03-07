package cf.zandercraft.zcblockprot.wrappers;

import java.util.UUID;

public interface Server {
    Player getPlayer(String name);
    Player getPlayer(UUID id);

    Object getWrapped();
}
