package com.jbouchier.horsetpwithme;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public enum MessageCache {
    MUST_BE_TAMED,
    MUST_BE_ADULT,
    MUST_HAVE_SADDLE,
    NO_TELEPORT_PERMISSION,
    CHEST_EMPTIED_FROM_WORLD,
    CHEST_EMPTIED_TO_WORLD,
    DESTINATION_WORLD_DISABLED,
    CANNOT_LEAVE_WORLD,
    CHESTS_EMPTIED_PASSENGERS,
    TELEPORT_AS_PASSENGER_ENABLED,
    TELEPORT_AS_PASSENGER_DISABLED;

    private String message;

    @Override
    public final String toString() {
        return message;
    }

    static class Msg {
        private static final HorseTpWithMe INSTANCE = JavaPlugin.getPlugin(HorseTpWithMe.class);
        private static final String FILE_NAME = "Language.yml";

        // THIS IS HORRIBLE - I HATE THIS ENTIRE CLASS
        public static void reload() {
            INSTANCE.saveResource(FILE_NAME, false);
            YamlConfiguration file = YamlConfiguration.loadConfiguration(new File(INSTANCE.getDataFolder(), FILE_NAME));
            boolean forceUpdate = false;
            for (MessageCache msg : MessageCache.values()) {
                String found = file.getString(msg.name());
                if (found == null) forceUpdate = true;
                else found = ChatColor.translateAlternateColorCodes('&', found);
                msg.message = found;
            }

            if (!forceUpdate) return;
            INSTANCE.saveResource(FILE_NAME, true);
            file = YamlConfiguration.loadConfiguration(new File(INSTANCE.getDataFolder(), FILE_NAME));
            for (MessageCache msg : MessageCache.values()) {
                if (msg.message != null) file.set(msg.name(), msg.message);
                else msg.message = ChatColor.translateAlternateColorCodes('&', file.getString(msg.name(), ""));
            }
        }
    }
}