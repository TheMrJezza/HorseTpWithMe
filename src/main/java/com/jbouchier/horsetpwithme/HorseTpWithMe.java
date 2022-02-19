package com.jbouchier.horsetpwithme;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
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
        register(new CoreListeners(tpLogic));
        if (DETECT_NON_VANILLA_VEHICLES) register(new SpigotListeners(tpLogic));
    }

    private void register(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    // I love confusing declarations
    private void register(CommandExecutor ce, TabCompleter tc, String... commands) {
        for (String c : commands) {
            PluginCommand cmd = getCommand(c);
            if (cmd == null) continue;
            if (ce != null) cmd.setExecutor(ce);
            if (tc != null) cmd.setTabCompleter(tc);
        }
    }
}