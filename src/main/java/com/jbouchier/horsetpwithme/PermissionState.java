package com.jbouchier.horsetpwithme;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public final class PermissionState {

    private enum PermissionType {
        TP_VEHICLE("vehicle"),
        TP_PASSENGER("passenger"),
        TP_LEASH("leash");
        public final String node;

        PermissionType(String permName) {
            this.node = permName;
        }
    }

    static void buildPerms(HorseTpWithMe plugin) {
        for (World world : plugin.getServer().getWorlds()) {
            for (EntityType type : EntityType.values()) {
                for (PermissionType permissionType : PermissionType.values()) {
                    for (var perm : buildPerms(permissionType, type, world)) {
                        var node = new Permission(perm, PermissionDefault.OP);
                        var pm = plugin.getServer().getPluginManager();
                        if (pm.getPermission(perm) == null) pm.addPermission(node);
                    }
                }
            }
        }
    }

    public static boolean preventLeash(@NotNull Player player, @NotNull LivingEntity leashed, @NotNull World world) {
        return permissionDenied(PermissionType.TP_LEASH, player, leashed, world);
    }

    public static boolean preventVehicle(@NotNull Player player, @NotNull Entity vehicle, @NotNull World world) {
        return permissionDenied(PermissionType.TP_VEHICLE, player, vehicle, world);
    }

    public static boolean allowPassenger(@NotNull Player player, @NotNull Entity passenger, @NotNull World world) {
        return !permissionDenied(PermissionType.TP_PASSENGER, player, passenger, world);
    }

    private static List<String> buildPerms(@NotNull PermissionType pType, @NotNull EntityType type, @NotNull World world) {
        var clazz = type.getEntityClass();
        if (clazz == null) return new ArrayList<>();

        final var format = "horsetpwithme.%s.%s." + pType.node + '.' + type.name().toLowerCase();
        ArrayList<String> result = new ArrayList<>();

        result.add(format.formatted("teleport", world.getName()));

        if (Steerable.class.isAssignableFrom(clazz) || AbstractHorse.class.isAssignableFrom(clazz)) {
            result.add(format.formatted("setting", "nosaddle"));
        }
        if (Tameable.class.isAssignableFrom(clazz)) {
            result.add(format.formatted("setting", "untamed"));
        }
        if (Ageable.class.isAssignableFrom(clazz)) {
            result.add(format.formatted("setting", "notadult"));
        }
        return result;
    }

    private static boolean permissionDenied(
            @NotNull PermissionType pType, @NotNull Player player, @NotNull Entity entity, @NotNull World world
    ) {
        final var required = buildPerms(pType, entity.getType(), world);
        required.removeIf(perm -> {
            if (player.hasPermission(perm)) return true;
            if (perm.contains(".nosaddle.")) {
                if (entity instanceof Steerable steerable) {
                    return steerable.hasSaddle();
                } else if (entity instanceof AbstractHorse horse) {
                    return horse.getInventory().getSaddle() != null;
                }
            } else if (perm.contains(".untamed.")) {
                if (entity instanceof Tameable tameable) return tameable.isTamed();
            } else if (perm.contains(".notadult.")) {
                if (entity instanceof Ageable ageable) return ageable.isAdult();
            }
            return false;
        });
        if (required.isEmpty()) return false;
        if (DataState.getInstance().isDebugOn()) {
            Bukkit.broadcastMessage("§7[§aHTpWM Debug§7] §cTeleport Denied§7: §a" + player.getName());
            required.forEach(perm -> Bukkit.broadcastMessage("§7- §f" + perm));
        }
        return true;
    }
}