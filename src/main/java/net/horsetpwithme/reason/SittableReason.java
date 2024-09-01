package net.horsetpwithme.reason;

import net.horsetpwithme.api.ReasonHTWM;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sittable;
import org.bukkit.entity.Tameable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SittableReason extends ReasonHTWM {
    static final String REASON_NAME = "sittable";

    public SittableReason() {
        super(REASON_NAME, Set.of(Sittable.class, Tameable.class));
    }

    @Override
    public @NotNull Set<Entity> getHandledEntities(
            @NotNull Player player,
            @NotNull List<Entity> nearbyEntities,
            @NotNull Location from,
            @NotNull Location to) {
        final var handledEntities = new HashSet<Entity>();
        nearbyEntities.forEach(entity -> {
            if (entity instanceof Sittable sittable && entity instanceof Tameable tameable) {
                if (tameable.isTamed() && player.equals(tameable.getOwner())) {
                    if (!sittable.isSitting() && !entity.isInsideVehicle()) {
                        handledEntities.add(entity);
                    }
                }
            }
        });
        return handledEntities;
    }
}