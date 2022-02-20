package com.jbouchier.horsetpwithme;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;

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

    boolean isController(Entity v, Entity r) {
        for (Entity p : v.getPassengers()) if (p instanceof Player pl) return pl.equals(r);
        return false;
    }

    void storeVehicle(Entity vehicle, Entity rider) {
        if (rider instanceof Player && isController(vehicle, rider)) vehicleLookup.put(rider, vehicle);
    }

    Entity retrieveVehicle(Player player) {
        return vehicleLookup.get(player);
    }

    // TODO Add permission Checks.
    // TODO Add Multi-Passenger Support.
    void processTeleport(Entity vehicle, Player player, Location from, Location to) {

        final World wTo = to.getWorld(), wFrom = from.getWorld();
        if (wTo == null || wFrom == null) return;

        if (!isAuthed(player, "horsetpwithme.teleport.",
                "*", vehicle.getType().name())) {
            return;
        }

        boolean clearChests = false;

        if ((vehicle instanceof Steerable steerable && !steerable.hasSaddle()) ||
            (vehicle instanceof AbstractHorse horse && horse.getInventory().getSaddle() == null)) {
            if (player.hasPermission("horsetpwithme.require_saddle")) return;
        }

        if (vehicle instanceof Tameable tame && !tame.isTamed()) {
            if (player.hasPermission("horsetpwithme.require_tamed")) return;
        }

        if (vehicle instanceof Ageable ageable && !ageable.isAdult()) {
            if (player.hasPermission("horsetpwithme.require_adult")) return;
        }

        if (!wTo.equals(wFrom)) {
            if (player.hasPermission("horsetpwithme.deny_cross_world")) return;
            if (player.hasPermission("horsetpwithme.disabled_world." + wTo.getName().toLowerCase())) return;
            if (isAuthed(player, "horsetpwithme.empty_chests_",
                    "to." + wTo.getName().toLowerCase(),
                    "from." + wFrom.getName().toLowerCase())) {
                clearChests = true;
            }
        }

        VehicleTeleportEvent event = new VehicleTeleportEvent(vehicle, from, to, player, clearChests);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(HorseTpWithMe.class), () -> {

            List<Entity> passengers = new ArrayList<>();
            passengers.add(player);

            // TODO Check if player is allowed to teleport passengers
            /*IF (PLAYER IS ALLOWED TO TELEPORT PASSENGERS)*/
            {
                for (Entity passenger : vehicle.getPassengers()) {
                    if (passenger instanceof Player pl) {
                        // TODO Check if this player passenger has as-passenger TP enabled.
                        /*IF (THIS PASSENGER HAS AS-PASSENGER TP DISABLED)*/
                        {
                            continue;
                        }
                    }
                    passengers.add(passenger);
                    passenger.teleport(event.getTo());
                }
            }
            vehicle.teleport(event.getTo());
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(HorseTpWithMe.class),
                    () -> reseat(vehicle, passengers), 0
            );
        });
    }

    private boolean isAuthed(Player player, String permBase, String... keys) {
        for (String key : keys) if (player.hasPermission((permBase + key).toLowerCase())) return true;
        return false;
    }

    private void reseat(Entity vehicle, List<Entity> passengers) {
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