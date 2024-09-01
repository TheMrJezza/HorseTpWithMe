package unused.com.jbouchier.horsetpwithme.event;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VehicleTeleportEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled;
    private final Player driver;
    private final Entity vehicle;
    private final List<Entity> passengers;
    private final Location destination, from;

    /**
     * A cancellable event that is called when a vehicle is to be teleported with its player driver.
     * Some entities may have been filtered out of the passengers list due to permission reasons, or
     * by choice in the case of Player passengers. tl;dr 'passengers' only contains the remaining
     * passengers that will be teleported and does not include the driver.
     *
     * @param driver      The primary passenger of the vehicle at the time of teleporting
     * @param vehicle     The vehicle entity that is teleporting
     * @param passengers  The entities that will be teleported as passengers. Can be empty, but not null.
     * @param destination The location where the vehicles will be sent.
     */
    public VehicleTeleportEvent(
            @NotNull Player driver, @NotNull Entity vehicle,
            @NotNull List<Entity> passengers, @NotNull Location destination
    ) {
        this.driver = driver;
        this.vehicle = vehicle;
        this.passengers = passengers;
        this.destination = destination;
        this.from = vehicle.getLocation();
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public @NotNull Location getTo() {
        return this.destination;
    }

    public @NotNull Location getFrom() {
        return this.from;
    }

    public @NotNull Entity getVehicle() {
        return this.vehicle;
    }

    public @NotNull Player getDriver() {
        return this.driver;
    }

    public @NotNull List<Entity> getPassengers() {
        return this.passengers;
    }
}