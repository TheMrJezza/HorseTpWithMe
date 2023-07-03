package com.jbouchier.horsetpwithme;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public final class DataState {
    private boolean debugOn = false;
    private final HorseTpWithMe plugin;
    private final HashMap<Player, Entity> vehicleCache;

    private DataState() {
        plugin = JavaPlugin.getPlugin(HorseTpWithMe.class);
        vehicleCache = new HashMap<>(5);
    }

    void saveVehicle(@NotNull Player player, @NotNull Entity vehicle) {
        if (vehicleCache.isEmpty()) {
            plugin.getServer().getScheduler().runTask(plugin, vehicleCache::clear);
        }
        vehicleCache.put(player, vehicle);
    }

    public @Nullable Entity getVehicle(@NotNull Player player) {
        return player.isInsideVehicle() ? player.getVehicle() : vehicleCache.get(player);
    }

    public boolean isDebugOn() {
        return debugOn;
    }

    void setDebugOn(boolean debugOn) {
        this.debugOn = debugOn;
    }

    private static final class DataStateHolder {
        private static final DataState instance = new DataState();
    }

    public static @NotNull DataState getInstance() {
        return DataStateHolder.instance;
    }
}