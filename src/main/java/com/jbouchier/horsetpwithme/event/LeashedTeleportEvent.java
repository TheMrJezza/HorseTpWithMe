package com.jbouchier.horsetpwithme.event;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class LeashedTeleportEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled = false;

    public final LivingEntity getLeashed;
    public final Location getTo, getFrom;
    public final Player getLeashHolder;

    public LeashedTeleportEvent(@NotNull LivingEntity leashed, @NotNull Location destination) {
        getLeashed = leashed;
        getTo = destination;
        getFrom = leashed.getLocation();
        getLeashHolder = (Player) leashed.getLeashHolder();
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}