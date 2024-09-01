package net.horsetpwithme.reason;

import net.horsetpwithme.HorseTpWithMe;
import net.horsetpwithme.api.ReasonHTWM;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VehicleReason extends ReasonHTWM {
    static final String REASON_NAME = "vehicle";

    public VehicleReason() {
        super(REASON_NAME);
    }

    @Override
    public @NotNull Set<Entity> getHandledEntities(
            @NotNull Player player,
            @NotNull List<Entity> nearbyEntities,
            @NotNull Location from,
            @NotNull Location to) {
        final var handledEntities = new HashSet<Entity>();
        final var vehicle = HorseTpWithMe.API.DATA_STORE.getLastDrivenVehicle(player);
        if (vehicle != null) {
            handledEntities.add(vehicle);
        }
        return handledEntities;
    }

    @Override
    public void handleEntityAfterTeleport(
            @NotNull Player player,
            @NotNull Entity entity,
            @NotNull Location from,
            @NotNull Location to) {
        entity.addPassenger(player);
    }
}