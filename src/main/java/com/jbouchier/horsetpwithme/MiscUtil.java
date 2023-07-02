package com.jbouchier.horsetpwithme;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
}