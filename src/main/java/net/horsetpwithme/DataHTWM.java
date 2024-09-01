package net.horsetpwithme;

import net.horsetpwithme.api.DataStoreHTWM;
import net.horsetpwithme.api.ReasonHTWM;
import net.horsetpwithme.listener.EntityUnmountListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

class DataHTWM implements DataStoreHTWM {
    static final long REMEMBER_VEHICLES_DURATION_TICKS = 2L;
    private final HorseTpWithMe plugin;

    private final SequencedMap<Class<? extends ReasonHTWM>, ReasonHTWM> reasons;
    private final Map<Player, Entity> dismounts;

    DataHTWM(HorseTpWithMe plugin) {
        this.reasons = new LinkedHashMap<>();
        this.dismounts = new LinkedHashMap<>();
        new EntityUnmountListener(plugin, this::onEntityUnmount);
        this.plugin = plugin;
    }

    @Override
    public void registerReason(@NotNull ReasonHTWM reason) {
        this.reasons.put(reason.getClass(), reason);
    }

    @Override
    public @Nullable ReasonHTWM getRegisteredReason(Class<? extends ReasonHTWM> reasonClass) {
        return this.reasons.get(reasonClass);
    }

    @Override
    public @Nullable Entity getLastDrivenVehicle(@NotNull Player player) {
        if (player.getVehicle() != null) {
            onEntityUnmount(player, player.getVehicle());
        }
        return dismounts.get(player);
    }

    private void onEntityUnmount(@NotNull Entity entity, @NotNull Entity unmounted) {
        if (entity instanceof Player player) {
            if (dismounts.containsKey(player) || dismounts.containsValue(unmounted)) {
                return;
            }
            if (player.equals(unmounted.getPassengers().getFirst())) {
                dismounts.put(player, unmounted);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    dismounts.remove(player);
                }, REMEMBER_VEHICLES_DURATION_TICKS);
            }
        }
    }

    @Override
    public @NotNull Set<ReasonHTWM> reasons() {
        return new LinkedHashSet<>(reasons.values());
    }
}