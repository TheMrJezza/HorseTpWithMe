package com.jbouchier.horsetpwithme;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

class TeleportLogic {
    private final HashMap<Entity, Entity> vehicleLookup = new HashMap<>();

    TeleportLogic(HorseTpWithMe plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, vehicleLookup::clear, 0L, 0L);
    }

    boolean isController(Entity v, Entity r) {
        for (Entity p : v.getPassengers())
            if (p instanceof Player pl) return pl.equals(r);
        return false;
    }

    void storeVehicle(Entity vehicle, Entity rider) {
        if (rider instanceof Player && isController(vehicle, rider))
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
                    passenger.teleport(to);
                }
            }
            vehicle.teleport(to);
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(HorseTpWithMe.class),
                    () -> reseat(vehicle, passengers), 0
            );
        });
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

        final net.minecraft.world.entity.Entity handle = ((CraftEntity) vehicle).getHandle();

        sendPackets(rider,
                new PacketPlayOutSpawnEntity(handle),
                new PacketPlayOutEntityMetadata(vehicle.getEntityId(), handle.ai(), true)
        );

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

    private void sendPackets(Player player, Packet<?>... packets) {
        for (Packet<?> packet : packets) ((CraftPlayer) player).getHandle().b.a(packet);
    }
}