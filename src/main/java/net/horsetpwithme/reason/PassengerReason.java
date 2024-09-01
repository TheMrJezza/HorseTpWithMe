package net.horsetpwithme.reason;

import net.horsetpwithme.api.ReasonHTWM;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PassengerReason extends ReasonHTWM {
    static final String REASON_NAME = "passenger";

    public PassengerReason() {
        super(REASON_NAME);
    }

    @Override
    public @NotNull Set<Entity> getHandledEntities(
            @NotNull Player player,
            @NotNull List<Entity> nearbyEntities,
            @NotNull Location from,
            @NotNull Location to) {
        final var result = new LinkedHashSet<Entity>();
        if (!nearbyEntities.isEmpty()) {
            result.addAll(nearbyEntities.getFirst().getPassengers());
            result.remove(player);
            return result;
        }
        return result;
    }

    @Override
    public void handleEntityAfterTeleport(
            @NotNull Player player,
            @NotNull Entity entity,
            @NotNull Location from,
            @NotNull Location to) {
        final var vehicle = player.getVehicle();
        if (vehicle != null) {
            vehicle.addPassenger(entity);
        }
    }
}
