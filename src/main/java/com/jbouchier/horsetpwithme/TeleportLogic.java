package com.jbouchier.horsetpwithme;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

class TeleportLogic {
    private final IProtocol protocol;
    private final HashMap<Entity, Entity> vehicleLookup = new HashMap<>();
    private final NamespacedKey namespace;

    TeleportLogic(HorseTpWithMe plugin, IProtocol protocol) {
        Bukkit.getScheduler().runTaskTimer(plugin, vehicleLookup::clear, 0L, 0L);
        this.protocol = protocol;
        namespace = new NamespacedKey(plugin, "tap_status");
    }

    private static void clearChests(Player player, Entity entity, int clearChests) {
        if (clearChests < 0 || clearChests > 2) return;
        if (entity instanceof ChestedHorse ch && ch.isCarryingChest()) {
            ch.setCarryingChest(false);
            ch.setCarryingChest(true);
            switch (clearChests) {
                case 0 -> player.sendMessage(MessageCache.CHEST_EMPTIED_TO_WORLD.toString());
                case 1 -> player.sendMessage(MessageCache.CHEST_EMPTIED_FROM_WORLD.toString());
                case 2 -> player.sendMessage(MessageCache.CHESTS_EMPTIED_PASSENGERS.toString());
            }
        }
    }

    boolean isController(@NotNull Entity v, Entity r) {
        for (Entity p : v.getPassengers()) if (p instanceof Player pl) return pl.equals(r);
        return false;
    }

    void storeVehicle(Entity vehicle, Entity rider) {
        if (rider instanceof Player && isController(vehicle, rider)) vehicleLookup.put(rider, vehicle);
    }

    void setTapStatus(Player player, boolean status) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (status) {
            player.sendMessage(MessageCache.TELEPORT_AS_PASSENGER_ENABLED.toString());
            pdc.remove(namespace);
        }
        else {
            pdc.set(namespace, PersistentDataType.INTEGER, 1);
            player.sendMessage(MessageCache.TELEPORT_AS_PASSENGER_DISABLED.toString());
        }
    }

    Entity retrieveVehicle(Player player) {
        return vehicleLookup.get(player);
    }

    void processTeleport(Entity vehicle, Player player, @NotNull Location from, @NotNull Location to) {

        final World wTo = to.getWorld(), wFrom = from.getWorld();
        if (wTo == null || wFrom == null) return;

        if (!player.hasPermission("horsetpwithme.teleport." + vehicle.getType().name().toLowerCase())) {

            // TODO Replace with proper configurable message
            player.sendMessage(MessageCache.NO_TELEPORT_PERMISSION.toString());
            return;
        }

        int clearChests = -1;

        if ((vehicle instanceof Steerable steerable && !steerable.hasSaddle()) ||
            (vehicle instanceof AbstractHorse horse && horse.getInventory().getSaddle() == null)) {
            if (player.hasPermission("horsetpwithme.require_saddle")) {

                // TODO Replace with proper configurable message
                player.sendMessage(MessageCache.MUST_HAVE_SADDLE.toString());
                return;
            }
        }

        if (vehicle instanceof Tameable tame && !tame.isTamed()) {
            if (player.hasPermission("horsetpwithme.require_tamed")) {

                // TODO Replace with proper configurable message
                player.sendMessage(MessageCache.MUST_BE_TAMED.toString());
                return;
            }
        }

        if (vehicle instanceof Ageable ageable && !ageable.isAdult()) {
            if (player.hasPermission("horsetpwithme.require_adult")) {

                // TODO Replace with proper configurable message
                player.sendMessage(MessageCache.MUST_BE_ADULT.toString());
                return;
            }
        }

        if (!wTo.equals(wFrom)) {
            if (player.hasPermission("horsetpwithme.deny_cross_world")) {

                // TODO Replace with proper configurable message
                player.sendMessage(MessageCache.CANNOT_LEAVE_WORLD.toString());
                return;
            }
            if (player.hasPermission("horsetpwithme.disabled_world." + wTo.getName().toLowerCase())) {

                // TODO Replace with proper configurable message
                player.sendMessage(MessageCache.DESTINATION_WORLD_DISABLED.toString());
                return;
            }
            clearChests = isAuthed(player, "to." + wTo.getName().toLowerCase(),
                    "from." + wFrom.getName().toLowerCase());
        }

        VehicleTeleportEvent event = new VehicleTeleportEvent(vehicle, from, to, player, clearChests >= 0);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        if (event.isClearingChests() && clearChests < 0) clearChests = 0;

        int finalClearChests = clearChests;
        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(HorseTpWithMe.class), () -> {

            List<Entity> passengers = new ArrayList<>();
            passengers.add(player);

            clearChests(player, vehicle, finalClearChests);
            if (player.hasPermission("horsetpwithme.teleport_passengers")) {
                for (Entity passenger : vehicle.getPassengers()) {
                    if (passenger instanceof Player pl) {
                        if (pl.hasPermission("horsetpwithme.deny_passenger_teleport") || getTAPStatus(pl)) {
                            continue;
                        }
                    }
                    passengers.add(passenger);
                    if (finalClearChests >= 0) clearChests(player, passenger, 3);
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

    boolean getTAPStatus(Player player) {
        return player.getPersistentDataContainer().getOrDefault(namespace, PersistentDataType.INTEGER, 0) != 0;
    }

    private int isAuthed(Player player, String @NotNull ... keys) {
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            if (player.hasPermission(("horsetpwithme.empty_chests_" + key).toLowerCase())) return i;
        }
        return -1;
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