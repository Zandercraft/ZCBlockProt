package cf.zandercraft.zcblockprot.wrappers.bukkit;

import cf.zandercraft.zcblockprot.wrappers.World;

public class BukkitWorld implements World {
    private org.bukkit.World wrappedWorld;

    public BukkitWorld(org.bukkit.World wrappedWorld) {
        this.wrappedWorld = wrappedWorld;
    }

    @Override
    public String getName() {
        return this.wrappedWorld.getName();
    }

    @Override
    public Object getWrapped() {
        return this.wrappedWorld;
    }
}
