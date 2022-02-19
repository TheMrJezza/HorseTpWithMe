package com.jbouchier.horsetpwithme;

import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityDismountEvent;

record SpigotListeners(TeleportLogic tpLogic) implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onEntityExit(EntityDismountEvent evt) {
        if (evt.getDismounted() instanceof Vehicle) return;
        tpLogic.storeVehicle(evt.getDismounted(), evt.getEntity());
    }
}