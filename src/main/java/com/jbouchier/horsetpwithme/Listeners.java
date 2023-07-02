package com.jbouchier.horsetpwithme;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.Set;

public class Listeners implements Listener {
    private final HorseTpWithMe plugin;
    private final DataState dataState = DataState.getInstance();
    private final Set<PlayerTeleportEvent.TeleportCause> banned;

    Listeners(HorseTpWithMe plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        banned = Set.of(
                PlayerTeleportEvent.TeleportCause.UNKNOWN,
                PlayerTeleportEvent.TeleportCause.SPECTATE,
                PlayerTeleportEvent.TeleportCause.DISMOUNT
        );
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onEntityDismount(EntityDismountEvent event) {
        if (event.getEntity() instanceof Player player) {
            dataState.saveVehicle(player, event.getDismounted());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerTeleport(PlayerTeleportEvent event) {
        if (banned.contains(event.getCause())) return;
        final var player = event.getPlayer();
        final var destination = MiscUtil.getNearbySafeLocation(event.getTo());

        final var tpRecord = new TeleportRecord(player, destination, plugin);
        final var leashedAll = MiscUtil.getNearbyLeashedEntities(event.getFrom());

        // Leash Teleporting
        if (!leashedAll.isEmpty()) {
            final var playerLeashed = MiscUtil.getLeashed(leashedAll, player);
            playerLeashed.forEach(leashed -> {
                if (!PermissionState.preventLeash(player, leashed, destination.getWorld())) {
                    tpRecord.addLeashPair(player, leashed);
                    leashed.getPassengers().forEach(pass -> {
                        if (PermissionState.allowPassenger(player, pass, destination.getWorld())) {
                            tpRecord.addStackPair(leashed, pass);
                        }
                    });
                }
            });
        }

        if (player.isInsideVehicle()) player.leaveVehicle();
        var vehicle = dataState.getVehicle(player);

        if (vehicle != null) {
            if (!PermissionState.preventVehicle(player, vehicle, destination.getWorld())) {
                tpRecord.addStackPair(vehicle, player);
                vehicle.getPassengers().forEach(pass -> {
                    if (PermissionState.allowPassenger(player, pass, destination.getWorld())) {
                        tpRecord.addStackPair(vehicle, pass);
                    }
                });
            }
        }

        if (tpRecord.hasWork()) {
            plugin.getServer().getScheduler().runTask(plugin, tpRecord::teleport);
            plugin.getServer().getScheduler().runTaskLater(plugin, tpRecord::reconstruct, 2);
            plugin.getServer().getScheduler().runTaskLater(plugin, tpRecord::fixLeads, 4);
        }
    }
}