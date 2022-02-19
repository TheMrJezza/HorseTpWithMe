package com.jbouchier.horsetpwithme;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public final class PermManager {

    // If TRUE, the host server is not a Spigot based fork of CraftBukkit.
    // Therefore, we cannot support non-vanilla vehicles.
    public static final boolean DETECT_NON_VANILLA_VEHICLES;

    // For some reason fatigued me would rather jump through hoops to use NIO instead of regular IO.
    // TODO Rewrite NIO code to use regular IO.
    public static final Path DATA_PATH = JavaPlugin.getPlugin(HorseTpWithMe.class).getDataFolder().toPath();

    // TODO Use String#format where date formatting is needed. We don't actually need this.
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    // '%s' is a placeholder for the Date/Time stamp for when this file was generated.
    private static final String
            HEADER = """
            THIS FILE IS A REFERENCE ONLY. Changing values in this file has no effect on the plugin.
            Please note that these permissions are automatically generated, and some teleport perms
            may be irrelevant.

            This File Was Generated at %tT %tZ %tb %td %tY



            """,
            TELEPORT_PERM = "horsetpwithme.teleport.";

    private static final Path PERMISSION_FILE_PATH;
    private static final String[] WORLD_PERMS = {
            "horsetpwithme.empty_chests_from.",
            "horsetpwithme.empty_chests_to.",
            "horsetpwithme.disabled_world."
    };

    static {
        boolean temp = true;
        try {
            Class.forName("org.spigotmc.event.entity.EntityDismountEvent");
        } catch (ClassNotFoundException ignore) {
            temp = false;
        }
        DETECT_NON_VANILLA_VEHICLES = temp;
        PERMISSION_FILE_PATH = DATA_PATH.resolve("Permissions.txt");
        final List<String> lines = new ArrayList<>();

        // GENERAL Permissions

        // TODO Add General Permissions

        // WORLD Permissions
        lines.add(String.format("%-60s=== Description ===\n\n", "=== WORLD PERMISSIONS ==="));
        for (World w : Bukkit.getWorlds())
            for (String p : WORLD_PERMS)
                createPermission(lines, p + w.getName().toLowerCase(), "");

        // Entity Permissions
        lines.add(String.format("\n%-60s=== Description ===\n\n", "=== ENTITY/VEHICLE PERMISSIONS ==="));

        for (EntityType t : EntityType.values()) {

            // Ensure that this entity is a *proper* entity, i.e. not a "Dragon Part" or something else stupid.
            // CraftBukkit Only -> Also make sure that this entity is a Vanilla Vehicle Entity.
            final Class<?> c = t.getEntityClass();
            if (c != null && t.isSpawnable() && (DETECT_NON_VANILLA_VEHICLES || Vehicle.class.isAssignableFrom(c))) {

                StringBuilder entityType = new StringBuilder(isVowel(t.name().charAt(0)) ? "an " : "a ");

                for (String part : t.name().toLowerCase().split("_"))
                    entityType.append(part.charAt(0)).append(part.substring(1).toLowerCase()).append(" ");
                // 'horsetpwithme.teleport.the_entity_name' <- Note: underscores '_'.
                createPermission(lines, TELEPORT_PERM + t.name().toLowerCase(), "Teleport " + entityType + "with you!");
            }
        }

        // Message Permissions
        lines.add(String.format("\n%-60s=== Description ===\n\n", "=== MESSAGE PERMISSIONS ==="));
        createPermission(lines, "ddd", "d");
        // TODO Add Message Perms

        try (BufferedWriter writer = Files.newBufferedWriter(PERMISSION_FILE_PATH, StandardCharsets.UTF_8)) {

            // TODO Fix This
            writer.write(String.format(HEADER));

            for (String line : lines) writer.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PermManager() {
    }

    private static boolean isVowel(char c) {
        for (char v : new char[]{'A', 'E', 'I', 'O', 'U'}) if (v == c) return true;
        return false;
    }

    private static void createPermission(List<String> permList, String perm, String description) {
        createPermission(permList, perm, description, null);
    }

    private static void createPermission(List<String> permList, String perm, String description, Permission parent) {
        Permission permission = new Permission(perm);
        if (parent != null) permission.addParent(parent, true);
        Bukkit.getPluginManager().addPermission(new Permission(perm));
        if (description == null) description = "";
        permList.add(String.format("%-60s%s\n", perm, description));
    }

    public static String getVehiclePermission(EntityType entityType) {
        return "horsetpwithme.teleport." + entityType.name().toLowerCase().trim();
    }
}