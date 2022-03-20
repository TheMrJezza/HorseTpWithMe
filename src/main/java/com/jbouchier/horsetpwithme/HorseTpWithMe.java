package com.jbouchier.horsetpwithme;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class HorseTpWithMe extends JavaPlugin {

    public static final boolean DETECT_NON_VANILLA_VEHICLES = checkSpigot();
    private static final String[] WORLD_PERMS = {
            "horsetpwithme.empty_chests_from.",
            "horsetpwithme.empty_chests_to.",
            "horsetpwithme.disabled_world."
    };

    private static boolean checkSpigot() {
        try {
            Class.forName("org.spigotmc.event.entity.EntityDismountEvent");
            return true;
        } catch (ClassNotFoundException ignore) {

        }
        return false;
    }

    public void onEnable() {
        MessageCache.Msg.reload();
        PluginManager pm = getServer().getPluginManager();
        Permission teleport = new Permission("horsetpwithme.teleport.*", PermissionDefault.TRUE);
        pm.addPermission(teleport);
        for (World w : Bukkit.getWorlds())
            for (String p : WORLD_PERMS)
                pm.addPermission(new Permission(p + w.getName().toLowerCase(), PermissionDefault.FALSE));
        for (EntityType t : EntityType.values()) {
            final Class<?> c = t.getEntityClass();
            if (c != null && t.isSpawnable() && (DETECT_NON_VANILLA_VEHICLES || Vehicle.class.isAssignableFrom(c))) {
                Permission perm = new Permission("horsetpwithme.teleport."
                                                 + t.name().toLowerCase(), PermissionDefault.FALSE);
                perm.addParent(teleport, true);
                pm.addPermission(perm);
            }
        }

        TeleportLogic tpLogic = new TeleportLogic(this, determineProtocol());
        register(new CoreListeners(tpLogic));
        if (DETECT_NON_VANILLA_VEHICLES) register(new SpigotListeners(tpLogic));

        Objects.requireNonNull(getCommand("TeleportAsPassenger"),
                "TAP Toggle Command could not be registered!").setExecutor(new TapToggleCommand(tpLogic));
    }

    private IProtocol determineProtocol() {
        return new ProtocolHackMagic();
    }

    private void register(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }
}