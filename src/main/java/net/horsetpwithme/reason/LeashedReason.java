package net.horsetpwithme.reason;

import net.horsetpwithme.api.ReasonHTWM;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LeashedReason extends ReasonHTWM {
    static final String REASON_NAME = "leashed";

    public LeashedReason() {
        super(REASON_NAME, Set.of(LivingEntity.class));
    }

    @Override
    public @NotNull Set<Entity> getHandledEntities(
            @NotNull Player player,
            @NotNull List<Entity> nearbyEntities,
            @NotNull Location from,
            @NotNull Location to) {
        final Set<Entity> handledEntities = new HashSet<>();
        nearbyEntities.forEach(entity -> {
            if (entity instanceof LivingEntity living && living.isLeashed()) {
                if (player.equals(living.getLeashHolder())) {
                    handledEntities.add(entity);
                }
            }
        });
        return handledEntities;
    }

    @Override
    public void handleEntityBeforeTeleport(
            @NotNull Player player,
            @NotNull Entity entity,
            @NotNull Location from,
            @NotNull Location to) {
        ((LivingEntity) entity).setLeashHolder(null);
    }

    @Override
    public void handleEntityAfterTeleport(
            @NotNull Player player,
            @NotNull Entity entity,
            @NotNull Location from,
            @NotNull Location to) {
        ((LivingEntity) entity).setLeashHolder(player);
    }
}
