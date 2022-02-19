package com.jbouchier.horsetpwithme;

import org.bukkit.plugin.java.JavaPlugin;

public class HorseTpWithMe extends JavaPlugin {
    // Only Spigot/Spigot Forks can detect the mounting/dismounting of non-vanilla vehicles.
    // While non-vanilla support for CraftBukkit could* be added, it's not a priority.
    public static final boolean DETECT_NON_VANILLA_VEHICLES = checkSpigot();
    public final ConfigManager configs = setupFiles();

    private static boolean checkSpigot() {
        try {
            Class.forName("org.spigotmc.event.entity.EntityDismountEvent");
            return true;
        } catch (ClassNotFoundException ignore) {
        }
        return false;
    }

    private ConfigManager setupFiles() {
        return new ConfigManager();
    }

    public void onEnable() {
        TeleportLogic tpLogic = new TeleportLogic(this);
        getServer().getPluginManager().registerEvents(new CoreListeners(tpLogic), this);
        if (DETECT_NON_VANILLA_VEHICLES)
            getServer().getPluginManager().registerEvents(new SpigotListeners(tpLogic), this);
    }
}