package com.jbouchier.horsetpwithme;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

record CoreListeners(TeleportLogic tpLogic) implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onVehicleExit(VehicleExitEvent evt) {
        tpLogic.storeVehicle(evt.getVehicle(), evt.getExited());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerTeleport(PlayerTeleportEvent evt) {

        // Ignore useless Teleport causes.
        // UNKNOWN: Getting bucked off, falling off in water, your vehicle dying, changing vehicles etc...
        // CHORUS_FRUIT & ENDER_PEARL: Short range teleporting is graphically buggy. Can be fixed with packets.
        switch (evt.getCause()) {
            case UNKNOWN, CHORUS_FRUIT, ENDER_PEARL -> {
                return;
            }
        }

        final Location from = evt.getFrom(), to = evt.getTo();
        final Player player = evt.getPlayer();

        // Perform check for vehicle.
        Entity vehicle = player.getVehicle();
        if (vehicle != null) {
            // We need to make sure that the player is controlling this vehicle.
            // Passengers other than the controlling player cannot invoke a teleport.
            if (!tpLogic.isController(vehicle, player)) {
                vehicle = null;
            }
        } else {
            // Only vehicles that the player was controlling are added to the lookup.
            // No need to check.
            vehicle = tpLogic.retrieveVehicle(player);
        }

        if (vehicle != null) tpLogic.processTeleport(vehicle, player, from, to);
    }
}