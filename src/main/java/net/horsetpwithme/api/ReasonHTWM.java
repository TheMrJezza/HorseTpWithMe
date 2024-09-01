package net.horsetpwithme.api;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public abstract class ReasonHTWM {

    private final String reasonName;
    private final Set<Class<?>> supportedEntityTypes;

    public ReasonHTWM(@NotNull String reasonName) {
        this(reasonName, null);
    }

    public ReasonHTWM(
            @NotNull String reasonName,
            @Nullable Set<Class<?>> supportedEntityTypes) {
        this.reasonName = reasonName;
        if (supportedEntityTypes != null) {
            this.supportedEntityTypes = Set.copyOf(supportedEntityTypes);
        } else {
            this.supportedEntityTypes = Set.of(Entity.class);
        }
    }

    public final String getReasonName() {
        return this.reasonName;
    }

    public abstract @NotNull Set<Entity> getHandledEntities(
            @NotNull Player player,
            @NotNull List<Entity> nearbyEntities,
            @NotNull Location from,
            @NotNull Location to);

    public void handleEntityBeforeTeleport(
            @NotNull Player player,
            @NotNull Entity entity,
            @NotNull Location from,
            @NotNull Location to) {
        // Nothing to do...
    }

    public void handleEntityAfterTeleport(
            @NotNull Player player,
            @NotNull Entity entity,
            @NotNull Location from,
            @NotNull Location to) {
        // Nothing to do...
    }

    public boolean appliesTo(
            @NotNull EntityType entityType,
            @NotNull World worldFrom,
            @NotNull World worldTo) {
        final var entityTypeClass = entityType.getEntityClass();
        if (entityTypeClass == null || !entityType.isSpawnable()) {
            return false;
        }
        return this.supportedEntityTypes.stream().allMatch(supportedType ->
                supportedType.isAssignableFrom(entityTypeClass));
    }
}