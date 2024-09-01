package net.horsetpwithme.api;

import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public interface PermissionHTWM {
    @NotNull String getRequiredPermission(
            @NotNull World worldFrom,
            @NotNull World worldTo,
            @NotNull ReasonHTWM reasonHTWM,
            @NotNull EntityType entityType);
}
