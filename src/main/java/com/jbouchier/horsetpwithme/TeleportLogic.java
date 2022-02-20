package com.jbouchier.horsetpwithme;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

class TeleportLogic {
    private final IProtocol protocol;
    private final HashMap<Entity, Entity> vehicleLookup = new HashMap<>();

    TeleportLogic(HorseTpWithMe plugin, IProtocol protocol) {
        Bukkit.getScheduler().runTaskTimer(plugin, vehicleLookup::clear, 0L, 0L);
        this.protocol = protocol;
    }

    boolean isController(@NotNull Entity v, Entity r) {
        for (Entity p : v.getPassengers()) if (p instanceof Player pl) return pl.equals(r);
        return false;
    }

    void storeVehicle(Entity vehicle, Entity rider) {
        if (rider instanceof Player && isController(vehicle, rider)) vehicleLookup.put(rider, vehicle);
    }

    Entity retrieveVehicle(Player player) {
        return vehicleLookup.get(player);
    }

    void processTeleport(Entity vehicle, Player player, @NotNull Location from, @NotNull Location to) {

        final World wTo = to.getWorld(), wFrom = from.getWorld();
        if (wTo == null || wFrom == null) return;

        if (!player.hasPermission("horsetpwithme.teleport." + vehicle.getType().name().toLowerCase())) {

            // TODO Replace with proper configurable message
            player.sendMessage("You are not authorised to teleport this vehicle!");

            return;
        }

        boolean clearChests = false;

        if ((vehicle instanceof Steerable steerable && !steerable.hasSaddle()) ||
            (vehicle instanceof AbstractHorse horse && horse.getInventory().getSaddle() == null)) {
            if (player.hasPermission("horsetpwithme.require_saddle")) {

                // TODO Replace with proper configurable message
                player.sendMessage("You must put a saddle on this vehicle!");
                return;
            }
        }

        if (vehicle instanceof Tameable tame && !tame.isTamed()) {
            if (player.hasPermission("horsetpwithme.require_tamed")) {

                // TODO Replace with proper configurable message
                player.sendMessage("You must tame this vehicle!");
                return;
            }
        }

        if (vehicle instanceof Ageable ageable && !ageable.isAdult()) {
            if (player.hasPermission("horsetpwithme.require_adult")) {

                // TODO Replace with proper configurable message
                player.sendMessage("This Vehicle is too young!");
                return;
            }
        }

        if (!wTo.equals(wFrom)) {
            if (player.hasPermission("horsetpwithme.deny_cross_world")) {

                // TODO Replace with proper configurable message
                player.sendMessage("You are not authorised to teleport vehicles into another world!");
                return;
            }
            if (player.hasPermission("horsetpwithme.disabled_world." + wTo.getName().toLowerCase())) {

                // TODO Replace with proper configurable message
                player.sendMessage("You are not authorised to teleport this vehicle into THIS world!");
                return;
            }
            if (isAuthed(player,
                    "to." + wTo.getName().toLowerCase(),
                    "from." + wFrom.getName().toLowerCase())) {
                clearChests = true;
            }
        }

        VehicleTeleportEvent event = new VehicleTeleportEvent(vehicle, from, to, player, clearChests);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {

            // TODO - DELETE THIS DEBUG MESSAGE
            player.sendMessage("Event Cancelled!");
            return;
        }

        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(HorseTpWithMe.class), () -> {

            List<Entity> passengers = new ArrayList<>();
            passengers.add(player);

            if (player.hasPermission("horsetpwithme.teleport_passengers")) {
                for (Entity passenger : vehicle.getPassengers()) {
                    if (passenger instanceof Player pl) {
                        if (pl.hasPermission("horsetpwithme.deny_passenger_teleport") || !getTAPStatus(pl))
                            continue;
                    }
                    passengers.add(passenger);
                    passenger.teleport(event.getTo());
                    passenger.setFallDistance(-Float.MAX_VALUE);
                }
            }
            vehicle.teleport(event.getTo());
            vehicle.setFallDistance(-Float.MAX_VALUE);
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(HorseTpWithMe.class),
                    () -> reseat(vehicle, passengers), 0
            );
        });
    }

    private boolean getTAPStatus(Player player) {

        // TODO - Implement a proper T.A.P. Status System.
        return true;
    }

    private boolean isAuthed(Player player, String @NotNull ... keys) {
        for (String key : keys) if (player.hasPermission(("horsetpwithme.empty_chests_" + key).toLowerCase())) return true;
        return false;
    }

    private void reseat(@NotNull Entity vehicle, @NotNull List<Entity> passengers) {
        final Player rider = (Player) passengers.get(0);
        final UUID uuid = vehicle.getUniqueId();

        if (!vehicle.isValid()) {
            final Entity found = Bukkit.getEntity(uuid), original = vehicle;
            if (found == null || !found.isValid()) {
                Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(HorseTpWithMe.class),
                        () -> reseat(original, passengers), 1
                );
                return;
            }
            vehicle = found;
        }

        protocol.updateVehicle(rider, vehicle);

        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(HorseTpWithMe.class), () -> {
            final Entity f = Bukkit.getEntity(uuid);
            if (f == null || !f.isValid()) return;
            for (Entity passenger : passengers) {
                if (!passenger.isValid()) {
                    passenger = Bukkit.getEntity(passenger.getUniqueId());
                    if (passenger == null || !passenger.isValid()) continue;
                }
                f.addPassenger(passenger);
            }
        }, 1);
    }
}