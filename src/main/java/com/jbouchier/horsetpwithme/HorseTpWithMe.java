package com.jbouchier.horsetpwithme;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class HorseTpWithMe extends JavaPlugin {

    public static final boolean DETECT_NON_VANILLA_VEHICLES = checkSpigot();

    private static boolean checkSpigot() {
        try {
            Class.forName("org.spigotmc.event.entity.EntityDismountEvent");
            return true;
        } catch (ClassNotFoundException ignore) {
        }
        return false;
    }

    public void onEnable() {
        TeleportLogic tpLogic = new TeleportLogic(this, determineProtocol());
        register(new CoreListeners(tpLogic));
        if (DETECT_NON_VANILLA_VEHICLES) register(new SpigotListeners(tpLogic));
    }

    private IProtocol determineProtocol() {
        return null;
    }

    private void register(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }
}