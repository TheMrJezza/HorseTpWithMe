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
        switch (evt.getCause()) { case UNKNOWN, CHORUS_FRUIT, ENDER_PEARL -> { return; }}

        final Location from = evt.getFrom(), to = evt.getTo();
        final Player player = evt.getPlayer();
        Entity vehicle = player.getVehicle();

        if (vehicle != null) {
            if (!tpLogic.isController(vehicle, player)) vehicle = null;
        } else vehicle = tpLogic.retrieveVehicle(player);

        if (vehicle != null) tpLogic.processTeleport(vehicle, player, from, to);
    }
}