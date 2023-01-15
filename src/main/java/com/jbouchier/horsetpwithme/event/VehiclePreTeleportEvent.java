package com.jbouchier.horsetpwithme.event;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VehiclePreTeleportEvent extends VehicleTeleportEvent {

    private static final HandlerList handlers = new HandlerList();
    private boolean requireSaddle;
    private boolean requireTamed;
    private boolean requireAdult;
    private boolean isAllowedTeleport;

    public boolean isRequireSaddle() {
        return requireSaddle;
    }

    public void setRequireSaddle(boolean requireSaddle) {
        this.requireSaddle = requireSaddle;
    }

    public boolean isRequireTamed() {
        return requireTamed;
    }

    public void setRequireTamed(boolean requireTamed) {
        this.requireTamed = requireTamed;
    }

    public boolean isRequireAdult() {
        return requireAdult;
    }

    public void setRequireAdult(boolean requireAdult) {
        this.requireAdult = requireAdult;
    }

    public boolean isAllowedTeleport() {
        return isAllowedTeleport;
    }

    public void setAllowedTeleport(boolean allowedTeleport) {
        isAllowedTeleport = allowedTeleport;
    }

    public boolean isTeleportPassengers() {
        return teleportPassengers;
    }

    public void setTeleportPassengers(boolean teleportPassengers) {
        this.teleportPassengers = teleportPassengers;
    }

    public boolean isMatchVehicleRequirements() {
        return matchVehicleRequirements;
    }

    public void setMatchVehicleRequirements(boolean matchVehicleRequirements) {
        this.matchVehicleRequirements = matchVehicleRequirements;
    }

    public boolean isFromWorldDisabled() {
        return fromWorldDisabled;
    }

    public void setFromWorldDisabled(boolean fromWorldDisabled) {
        this.fromWorldDisabled = fromWorldDisabled;
    }

    public boolean isToWorldDisabled() {
        return toWorldDisabled;
    }

    public void setToWorldDisabled(boolean toWorldDisabled) {
        this.toWorldDisabled = toWorldDisabled;
    }

    private boolean teleportPassengers;
    private boolean matchVehicleRequirements;
    private boolean fromWorldDisabled;
    private boolean toWorldDisabled;

    public VehiclePreTeleportEvent(
            @NotNull Player driver, @NotNull Entity vehicle, @NotNull List<Entity> passengers,
            @NotNull Location destination, boolean requireSaddle, boolean requireTamed, boolean requireAdult,
            boolean isAllowedTeleport, boolean teleportPassengers, boolean matchVehicleRequirements,
            boolean fromWorldDisabled, boolean toWorldDisabled
    ) {
        super(driver, vehicle, passengers, destination);
        this.requireSaddle = requireSaddle;
        this.requireTamed = requireTamed;
        this.requireAdult = requireAdult;
        this.isAllowedTeleport = isAllowedTeleport;
        this.teleportPassengers = teleportPassengers;
        this.matchVehicleRequirements = matchVehicleRequirements;
        this.fromWorldDisabled = fromWorldDisabled;
        this.toWorldDisabled = toWorldDisabled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}