package com.jbouchier.horsetpwithme;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class TeleportLogic {
    private final HashMap<Entity, Entity> vehicleLookup = new HashMap<>();

    TeleportLogic(HorseTpWithMe plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, vehicleLookup::clear, 0L, 0L);
    }

    void storeVehicle(Entity vehicle, Entity rider) {
        if (rider instanceof Player && vehicle.getPassengers().get(0).equals(rider))
            vehicleLookup.put(rider, vehicle);
    }

    Entity retrieveVehicle(Player player) {
        return vehicleLookup.get(player);
    }


    // TODO Add permission Checks.
    // TODO Add Multi-Passenger Support.
    void processTeleport(Entity vehicle, Player player, Location from, Location to) {

        // TODO Check if this teleport is allowed to happen.
        // Disabled Worlds, player doesn't have permission, disabled vehicle etc.

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
                }
            }
            vehicle.teleport(to);

            // ====
            // Info Dump: Any teleport that doesn't require the client to "respawn" the vehicle,
            // i.e. short range teleportation, will cause the vehicle to slowly drift to the
            // destination, which is an issue. Server-side, the vehicle is at the destination,
            // however, client-side the vehicle isn't. We should not re-seat the player unless the
            // vehicle is at the correct position client-side.
            // The solution is to send the client an Entity Spawn Packet for the vehicle.
            // No need to send despawn/destroy packets.
            // It's a little tricky though, Living Entities require a different Packet to Non-Living,
            // however, all vehicles including living ones like horses use the non-living packet.
            //
            // THIS SECTION IS THE SOUL SINGLE REASON I HAVE NOT RELEASED AN UPDATE TO THE PLUGIN IN OVER
            // 2 YEARS.
            // WRITING THIS CODE IS SOMETHING I AM NOT FOND OF.
            // TODO Use Packets to lessen the Visual Glitches of Instant Teleportation.
            //
            // PACKET CODE GOES HERE
            //
            // ====

            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(HorseTpWithMe.class), () -> {
                reseat(vehicle, passengers);
            }, 0);
        });
    }

    private void reseat(Entity vehicle, List<Entity> passengers) {

    }
}