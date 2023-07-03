package com.jbouchier.horsetpwithme;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MiscUtil {

    public @NotNull
    static Location getNearbySafeLocation(@NotNull Location unsafe) {
        // TODO implement some logic here
        return unsafe;
    }

    public static @NotNull Set<LivingEntity> getNearbyLeashedEntities(Location origin) {
        return origin.getNearbyEntities(20, 20, 20).stream()
                .filter(e -> e instanceof LivingEntity living && living.isLeashed()
                             && living.getLeashHolder() instanceof LivingEntity)
                .map(e -> ((LivingEntity) e)).collect(Collectors.toSet());
    }

    public static @NotNull Set<LivingEntity> getLeashed(@NotNull Set<LivingEntity> entities, @NotNull Player holder) {
        Set<LivingEntity> result = new HashSet<>();
        entities.removeIf(e -> {
            if (holder.equals(e.getLeashHolder())) {
                result.add(e);
                return true;
            }
            return false;
        });
        return result;
    }

    public static boolean isTapDisabled(Player player) {
        return !player.getPersistentDataContainer().getOrDefault(getTapKey(),
                PersistentDataType.BOOLEAN, true
        );
    }

    public static NamespacedKey getTapKey() {
        return new NamespacedKey(JavaPlugin.getPlugin(HorseTpWithMe.class), "tap_status");
    }

    public static Player extractTarget(CommandSender cs, String[] args) {
        Player target;
        if (cs instanceof Player player) target = player;
        else {
            if (args.length >= 1) {
                var match = Bukkit.getServer().matchPlayer(args[0]);
                if (match.isEmpty()) {
                    cs.sendMessage("Player not found!");
                    return null;
                }
                target = match.get(0);
            } else {
                cs.sendMessage("Must specify a player when used from the console!");
                return null;
            }
        }
        return target;
    }
}