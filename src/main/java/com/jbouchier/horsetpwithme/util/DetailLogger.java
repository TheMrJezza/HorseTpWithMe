package com.jbouchier.horsetpwithme.util;

import org.bukkit.Bukkit;

public class DetailLogger {
    private static final boolean ENABLED = true;
    private static final String PREFIX = "§7[§aHorseTpWithMe§7] §f";

    public static void log(String message, Object... params) {
        if (ENABLED) forceLog(message, params);
    }

    public static void forceLog(String message, Object... params) {
        Bukkit.getConsoleSender().sendMessage(String.format(PREFIX + message, params));
    }
}