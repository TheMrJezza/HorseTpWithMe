package com.jbouchier.horsetpwithme.util;

import com.jbouchier.horsetpwithme.Language;
import com.jbouchier.horsetpwithme.ScreenerException;
import com.jbouchier.horsetpwithme.event.VehicleTeleportEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.jbouchier.horsetpwithme.Language.MessageKey.*;
import static com.jbouchier.horsetpwithme.util.GeneralUtil.*;

// Blink is a static implementation of burst...
// Burst was the first successful "same tick" teleport method
public class BlinkTeleportUtil {

    // teleport an entity and passengers as fast as possible
    // DO NOT CALL THIS METHOD UNLESS THE DRIVER HAS BEEN EJECTED!
    public static void teleport(
            @NotNull Player driver, @NotNull Entity vehicle, @NotNull Location destination
    ) throws ScreenerException {

        // check if the teleport is allowed to happen at all
        Language.MessageKey preventKey = preventKey(driver, vehicle, destination.getWorld());
        if (preventKey != null) throw new ScreenerException(preventKey);

        // get the current passengers of the vehicle [COPY THE LIST]
        List<Entity> passengers = new ArrayList<>(vehicle.getPassengers());

        // eject the vehicle
        vehicle.eject();

        // filter out any passengers that shouldn't teleport
        // 'filtered' == true if any passengers are excluded
        // from this teleport

        boolean filtered = processPassengers(driver, passengers);

        // mutate destination to be safe
        destination = toSafeLocation(destination);

        // call the VehicleTeleportEvent
        VehicleTeleportEvent event = new VehicleTeleportEvent(driver, vehicle, passengers, destination);
        if (event.isCancelled()) return;

        // teleport passengers and vehicle to destination
        {
            vehicle.teleport(destination);
            vehicle.setFallDistance(-Float.MAX_VALUE);
            EntityNMS.refreshEntity(driver, vehicle);
            for (Entity passenger : passengers) {
                passenger.teleport(destination);
                passenger.setFallDistance(-Float.MAX_VALUE);
                EntityNMS.refreshEntity(driver, passenger);
            }
        }

        // attempt to notify the driver if filtering has occurred
        if (filtered) sendKeyedMessage(PASSENGERS_FILTERED, driver);

        // run our logic at the end of the current tick
        runTask(() -> stageOne(vehicle, driver, passengers), 0);
    }

    private static void stageOne(
            @NotNull Entity vehicle, @NotNull Player driver, @NotNull Collection<Entity> passengers
    ) {
        // At this stage, it is possible that some or all entity references are invalid
        // remember to grab fresh instances at least once per tick going forward.

        final Entity vehicleValid, playerValid;

        // initial validation check
        if (!vehicle.isValid() || !driver.isValid()) {
            Map<UUID, Entity> found = findEntities(vehicle.getWorld(), vehicle, driver);
            vehicleValid = found.get(vehicle.getUniqueId());
            playerValid = found.get(driver.getUniqueId());
            if (vehicleValid == null || playerValid == null) {
                // run our logic at the end of the next tick
                runTask(() -> stageOne(vehicle, driver, passengers), 1);
                return;
            }
        } else {
            vehicleValid = vehicle;
            playerValid = driver;
        }

        vehicleValid.addPassenger(playerValid);
        runTask(() -> stageTwo(vehicleValid, (Player) playerValid, passengers), 1);
    }

    private static void stageTwo(
            @NotNull Entity vehicle, @NotNull Player driver, @NotNull Collection<Entity> passengers
    ) {
        ArrayList<Entity> toValidate = new ArrayList<>(passengers);
        toValidate.add(vehicle);
        toValidate.add(driver);
        Map<UUID, Entity> found = findEntities(vehicle.getWorld(), toValidate.toArray(new Entity[0]));
        if (found.size() != toValidate.size()) {
            runTask(() -> stageTwo(vehicle, driver, passengers), 1);
            return;
        }
        Entity vVehicle = found.get(vehicle.getUniqueId());
        Player vDriver = (Player) found.get(driver.getUniqueId());
        for (Entity e : passengers) {
            vVehicle.addPassenger(found.get(e.getUniqueId()));
        }
        EntityNMS.sendPassengers(vDriver, vVehicle);

    }

    // used to 'screen' the driver and vehicle for primary restrictions
    private static Language.MessageKey preventKey(
            @NotNull Player driver, @NotNull Entity vehicle, @Nullable World world
            //, @Nullable VehiclePreTeleportEvent event
    ) {

        // check vehicle type is allowed
        if (denied(driver, "teleport." + vehicle.getType().name().toLowerCase()))
            return NO_TELEPORT_PERMISSION;

        // check vehicle is tame
        if (has(driver, "require_tamed") && vehicle instanceof Tameable tameable && !tameable.isTamed())
            return MUST_BE_TAMED;

        // check vehicle is adult
        if (has(driver, "require_adult") && vehicle instanceof Ageable ageable && !ageable.isAdult())
            return MUST_BE_ADULT;

        // check vehicle has saddle
        if (has(driver, "require_saddle")) {

            // strider/pig
            if (vehicle instanceof Steerable steerable && !steerable.hasSaddle())
                return MUST_HAVE_SADDLE;

            // horse/mule/donkey/camel
            if (vehicle instanceof AbstractHorse horse && horse.getInventory().getSaddle() == null)
                return MUST_HAVE_SADDLE;

            // fyi: a ravager is always saddled, no need to check
        }

        // 'world' can be null in order to prevent duplicate checks for passengers.
        if (world != null) {
            // check destination world is allowed
            if (has(driver, "disabled_world." + world.getName()))
                return DESTINATION_WORLD_DISABLED;

            // check origin world is allowed
            if (has(driver, "disabled_world_from." + vehicle.getWorld().getName()))
                return CANNOT_LEAVE_WORLD;
        }

        // no primary reason to prevent this teleport
        return null;
    }

    // filter out 'illegal' passengers
    private static boolean processPassengers(
            @NotNull Player driver,
            @NotNull Collection<Entity> passengers
    ) {
        // finish fast if there's nothing to do
        if (passengers.isEmpty()) return false;

        // screen passengers and remove any that shouldn't be teleported
        return passengers.removeIf(passenger ->
        {
            // handle player passengers differently
            if (passenger instanceof Player player) {
                // make sure the passenger wants and is allowed to teleport
                return has(player, "deny_passenger_teleport");
            }

            // check if vehicle constraints/restrictions should apply to passengers
            if (has(driver, "match_vehicle_teleports")) {
                // screen passenger for primary restrictions
                return preventKey(driver, passenger, null) != null;
            }

            // no reason to exclude this passenger
            return false;
        });
    }

    // TODO implement some logic here...
    private static @NotNull Location toSafeLocation(@NotNull Location input) {
        // Checks to implement:
        // - test if vehicle hitbox can fit at the destination
        // - test if destination is safe from hazards (lava, fire etc..)
        // - test if destination is a trap (GriefPrevention Claim with no access etc.)
        return input.clone();
    }
}