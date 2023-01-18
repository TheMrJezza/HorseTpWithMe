package com.jbouchier.horsetpwithme;

import com.jbouchier.horsetpwithme.util.BlinkTeleportUtil;
import com.jbouchier.horsetpwithme.util.GeneralUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.HashMap;

public class EventListeners implements Listener {

    public final static EventListeners INSTANCE = new EventListeners();
    private final HashMap<Player, Entity> vehicleCache = new HashMap<>();

    private EventListeners() {
        // private for reasons
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onEntityDismount(EntityDismountEvent event) {
        if (event.getEntity() instanceof Player player) {
            // schedule the cache to be cleared at the end of the tick
            if (vehicleCache.isEmpty()) GeneralUtil.runTask(vehicleCache::clear, 0);

            // map vehicle to player for the rest of the tick
            vehicleCache.put(player, event.getDismounted());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerTeleport(
            @NotNull PlayerTeleportEvent event
    ) {
        Location destination = event.getTo();

        // ensure destination is valid
        if (destination == null || destination.getWorld() == null)
            return;

        // ensure the teleport cause is valid
        {
            PlayerTeleportEvent.TeleportCause cause = event.getCause();

            // UNKNOWN: vehicle dies / player is "kicked" or "falls" off etc...
            // DISMOUNT: player is dismounted either intentionally or via a plugin
            // SPECTATE: player is in spectator mode and starts/stops "spectating" an entity

            // Yes, we have to use the name. "DISMOUNT" wasn't present before 1.19.3.
            switch (cause.name()) {
                case "UNKNOWN", "DISMOUNT", "SPECTATE" -> {
                    return;
                }
            }
        }

        Player player = event.getPlayer();

        // Some plugins manually dismount entities before teleporting them.
        // while I disagree with that approach, the following line of code
        // ensures every player is manually dismounted before going further.
        // doing so allows every teleport to be handled in the same way.
        if (player.isInsideVehicle()) player.leaveVehicle();

        // get the cached vehicle, if any
        Entity vehicle = vehicleCache.get(player);
        if (vehicle != null) {
            try {
                BlinkTeleportUtil.teleport(player, vehicle, destination);
            } catch (ScreenerException e) {
                GeneralUtil.sendKeyedMessage(e.getErrorKey(), player);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerJoin(
            @NotNull PlayerJoinEvent event
    ) {
        if (GeneralUtil.has(event.getPlayer(), "notify_updates")) {
            UpdateManager.notify(event.getPlayer());
        }
    }
}