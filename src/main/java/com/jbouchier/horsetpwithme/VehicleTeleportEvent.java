package com.jbouchier.horsetpwithme;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class VehicleTeleportEvent extends EntityTeleportEvent {
    private final Player player;
    private boolean clearChests;

    VehicleTeleportEvent(@NotNull Entity what, @NotNull Location from,
                         @NotNull Location to, @NotNull Player rider, boolean clearChests) {
        super(what, from, to);
        player = rider;
        this.clearChests = clearChests;
    }

    @NotNull
    public Player getRider() {
        return player;
    }

    public boolean isClearingChests() {
        return clearChests;
    }

    public void setClearChests(boolean clearChests) {
        this.clearChests = clearChests;
    }

    @NotNull
    public Location getTo() {
        return Objects.requireNonNull(super.getTo());
    }

    @Override
    public void setTo(Location set) {
        if (set != null) super.setTo(set);
    }
}