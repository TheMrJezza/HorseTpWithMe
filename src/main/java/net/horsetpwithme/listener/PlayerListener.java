package net.horsetpwithme.listener;

import net.horsetpwithme.HorseTpWithMe;
import net.horsetpwithme.api.ReasonHTWM;
import net.horsetpwithme.reason.PassengerReason;
import net.horsetpwithme.reason.VehicleReason;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlayerListener implements Listener {
    static final double NEARBY_SEARCH_RADIUS = 30D;
    static final long AFTER_TELEPORT_REASON_LOGIC_DELAY = 1L;
    static final long AFTER_TELEPORT_CLEANUP_DELAY = 2L;

    static final boolean USE_PERMISSIONS = true;

    static final Set<TeleportCause> BANNED_TELEPORT_CAUSES = Set.of(TeleportCause.UNKNOWN,
            TeleportCause.SPECTATE, TeleportCause.EXIT_BED, TeleportCause.DISMOUNT);

    static final BukkitScheduler SCHEDULER = Bukkit.getScheduler();

    private final Plugin plugin;

    public PlayerListener(@NotNull Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    static Map<Entity, Set<ReasonHTWM>> mapReasons(
            @NotNull Player player,
            @NotNull Location posF,
            @NotNull Location posT) {
        final var reasonMap = new LinkedHashMap<Entity, Set<ReasonHTWM>>();
        final var worldF = Objects.requireNonNull(posF.getWorld());
        final var worldT = Objects.requireNonNull(posT.getWorld());
        final var nearbyVehicle = new ArrayList<Entity>();
        final var nearby = player.getNearbyEntities(NEARBY_SEARCH_RADIUS,
                NEARBY_SEARCH_RADIUS, NEARBY_SEARCH_RADIUS);
        HorseTpWithMe.API.DATA_STORE.reasons().forEach(reason -> {
            final var near = reason instanceof PassengerReason ? nearbyVehicle : nearby;
            final var entities = reason.getHandledEntities(player, near, posF, posT);
            entities.forEach(entity -> {
                final var entityType = entity.getType();
                final var requiredTeleportPermission = HorseTpWithMe.API.PERMS
                        .getRequiredPermission(worldF, worldT, reason, entityType);
                if (!USE_PERMISSIONS || player.hasPermission(requiredTeleportPermission)) {
                    reasonMap.computeIfAbsent(entity, k -> new LinkedHashSet<>()).add(reason);
                    if (reason instanceof VehicleReason && nearbyVehicle.isEmpty()) {
                        nearbyVehicle.add(entity);
                    }
                }
            });
        });
        return reasonMap;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerTeleport(@NotNull PlayerTeleportEvent event) {
        if (!BANNED_TELEPORT_CAUSES.contains(event.getCause()) && event.getTo() != null) {
            final var player = event.getPlayer();
            mapReasons(player, event.getFrom(), event.getTo()).forEach((entity, reasons) -> {
                if (!reasons.isEmpty()) {
                    final var chunk = entity.getLocation().getChunk();
                    chunk.addPluginChunkTicket(plugin);
                    reasons.forEach(type -> type.handleEntityBeforeTeleport(player,
                            entity, event.getFrom(), event.getTo()));
                    entity.eject();
                    entity.leaveVehicle();
                    player.hideEntity(plugin, entity);
                    SCHEDULER.runTask(plugin, () -> {
                        entity.teleport(event.getTo());
                        entity.setFallDistance(-Float.MAX_VALUE);
                    });
                    SCHEDULER.runTaskLater(plugin, () -> {
                        reasons.forEach(type -> type.handleEntityAfterTeleport(player,
                                entity, event.getFrom(), event.getTo()));
                        entity.setFallDistance(-Float.MAX_VALUE);
                    }, AFTER_TELEPORT_REASON_LOGIC_DELAY);
                    SCHEDULER.runTaskLater(plugin, () -> {
                        player.showEntity(plugin, entity);
                        chunk.removePluginChunkTicket(plugin);
                    }, AFTER_TELEPORT_CLEANUP_DELAY);
                }
                event.getTo().getChunk().addPluginChunkTicket(plugin);
                SCHEDULER.runTaskLater(plugin, () -> {
                    event.getTo().getChunk().removePluginChunkTicket(plugin);
                }, AFTER_TELEPORT_CLEANUP_DELAY);
            });
        }
    }
}