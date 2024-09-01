package net.horsetpwithme.api;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface DataStoreHTWM {
    void registerReason(@NotNull ReasonHTWM reason);

    @Nullable ReasonHTWM getRegisteredReason(Class<? extends ReasonHTWM> reasonClass);

    @Nullable Entity getLastDrivenVehicle(@NotNull Player player);

    @NotNull Set<ReasonHTWM> reasons();
}
