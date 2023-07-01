package com.jbouchier.horsetpwithme;

import org.bstats.bukkit.Metrics;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.*;
import java.util.stream.Collectors;

public class HorseTpWithMe extends JavaPlugin implements Listener {

    private boolean verboseDebugging = false;
    private boolean ex_LeashAll = false;

    public final void onEnable() {
        new Metrics(this, 3502);
        getServer().getPluginManager().registerEvents(this, this);
    }

    private final HashSet<Player> exemptPlayers = new HashSet<>();
    private final HashMap<Player, Entity> lastVehicleExit = new HashMap<>();

    private void addExemptPlayer(Player player) {
        if (!exemptPlayers.isEmpty()) getServer().getScheduler().runTask(this, exemptPlayers::clear);
        exemptPlayers.add(player);
    }

    private void storeLastVehicle(Player player, Entity entity) {
        if (!lastVehicleExit.isEmpty()) getServer().getScheduler().runTask(this, lastVehicleExit::clear);
        lastVehicleExit.put(player, entity);
    }

    private Location makeSafe(Location original) {
        // TODO Make this location "safe", or find a suitable safe location close by.
        return original;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerDismount(EntityDismountEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (exemptPlayers.contains(player)) return;
            storeLastVehicle(player, event.getDismounted());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerTeleport(PlayerTeleportEvent event) {

        // Ignore teleports caused by the following reasons:
        switch (event.getCause()) {
            case DISMOUNT, UNKNOWN, SPECTATE -> {
                return;
            }
        }

        var destination = makeSafe(event.getTo());
        var player = event.getPlayer();
        var tpRecord = new TeleportRecord(player, destination, verboseDebugging);

        // LEASH TELEPORTING
        if (!tpRecord.nearbyLeashed.isEmpty()) {
            tpRecord.nearbyLeashed.removeIf(leashed -> {
                if (!player.equals(leashed.getLeashHolder())) return true;
                if (preventLeashTeleport(leashed, tpRecord)) return true;
                tpRecord.storeLeashEntity(leashed, player);
                recordStack(tpRecord, leashed, false);
                return false;
            });
        }

        // VEHICLE AND PLAYER STACK TELEPORTING
        if (!exemptPlayers.contains(player)) {
            boolean fromCache = true;
            Entity vehicle;
            if (player.isInsideVehicle()) {
                fromCache = false;
                vehicle = player.getVehicle();
                addExemptPlayer(player);
            } else vehicle = lastVehicleExit.get(player);
            if (vehicle != null) {
                // fromCache == false : Player IS included in vehicle.getPassengers()
                // fromCache == true : Player IS NOT included in vehicle.getPassengers()
                if (preventVehicleTeleport(vehicle, tpRecord)) {
                    recordStack(tpRecord, player, true);
                } else {
                    if (fromCache) tpRecord.storeStackPair(vehicle, player);
                    recordStack(tpRecord, vehicle, true);
                }
            }
        }

        tpRecord.initialise();
        getServer().getScheduler().runTaskLater(this, tpRecord::execute, 1);
        getServer().getScheduler().runTaskLater(this, tpRecord::finalise, 2);
    }

    private void recordStack(TeleportRecord tpRecord, Entity base, boolean playerStack) {
        for (var pass : base.getPassengers()) {
            if (pass.equals(tpRecord._owner) || !permissionDenied(playerStack ?
                    PermissionType.TP_PLAYER_STACK : PermissionType.TP_LEASH_STACK, pass, tpRecord)
            ) {
                tpRecord.storeStackPair(base, pass);
                if (pass instanceof Player player) addExemptPlayer(player);

                // THIS PART IS EXPERIMENTAL - ITS IMPLEMENTED POORLY
                if (ex_LeashAll && pass instanceof LivingEntity living) {
                    tpRecord.nearbyLeashed.removeIf(leashed -> {
                        if (pass.equals(leashed.getLeashHolder())) {
                            if (!(pass instanceof Player) || !preventLeashTeleport(leashed, tpRecord)) {
                                tpRecord.storeLeashEntity(leashed, living);
                            }
                            return true;
                        }
                        return false;
                    });
                }

                recordStack(tpRecord, pass, playerStack);
            }
        }
    }

    public boolean onCommand(@NotNull CommandSender cs, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        switch (cmd.getName()) {
            case "HorseDebug" -> {
                verboseDebugging = !verboseDebugging;
                cs.sendMessage("Verbose Debugging: %s".formatted(verboseDebugging ? "ENABLED" : "DISABLED"));
            }
            case "LeashExperiment" -> {
                ex_LeashAll = !ex_LeashAll;
                cs.sendMessage("Experimental Leash Teleporting: %s".formatted(ex_LeashAll ? "ENABLED" : "DISABLED"));
            }
            case "Reseat" -> {
                Player target;
                if (cs instanceof Player player) target = player;
                else {
                    if (args.length >= 1) {
                        var match = getServer().matchPlayer(args[0]);
                        if (match.isEmpty()) {
                            cs.sendMessage("Player not found!");
                            return true;
                        }
                        target = getServer().matchPlayer(args[0]).get(0);
                    } else {
                        cs.sendMessage("Must specify a player when used from the console!");
                        return true;
                    }
                }
                var vehicle = target.getVehicle();
                if (vehicle == null) {
                    cs.sendMessage("No vehicle detected!");
                } else {
                    if (target.canSee(vehicle)) {
                        target.hideEntity(this, vehicle);
                        getServer().getScheduler().runTaskLater(this, () -> target.showEntity(this, vehicle), 2);
                    } else {
                        target.showEntity(this, vehicle);
                    }
                    getServer().getScheduler().runTaskLater(this, () -> {
                        if (target.canSee(vehicle)) {
                            cs.sendMessage("Reseat Complete!");
                        } else {
                            cs.sendMessage("Reseat Prevented: A plugin is hiding the vehicle from the player!");
                        }
                    }, 3);
                }
            }
            case "TeleportAsPassenger" -> cs.sendMessage("Sorry, but this isn't currently implemented.");
        }
        return true;
    }

    private boolean preventLeashTeleport(Entity leashed, TeleportRecord tpRecord) {
        if (!(leashed instanceof LivingEntity living)) return true;
        if (tpRecord._owner.equals(living.getLeashHolder())) return true;
        return permissionDenied(PermissionType.TP_LEASH, leashed, tpRecord);
    }

    private boolean preventVehicleTeleport(Entity vehicle, TeleportRecord tpRecord) {
        // TODO perhaps implement a global config variable?
        return permissionDenied(PermissionType.TP_VEHICLE, vehicle, tpRecord);
    }

    private static final class TeleportRecord {
        private static final HorseTpWithMe plugin = JavaPlugin.getPlugin(HorseTpWithMe.class);
        private final Player _owner;
        private final Location _destination;
        private final LinkedHashMap<Entity, Entity> _stackMap = new LinkedHashMap<>();
        private final LinkedHashMap<Entity, Entity> _leashMap = new LinkedHashMap<>();
        private final LinkedList<String> _debugLog;

        private final Set<LivingEntity> nearbyLeashed;

        private TeleportRecord(Player owner, Location destination, boolean verbose) {
            nearbyLeashed = owner.getNearbyEntities(20, 20, 20).stream().filter(entity ->
                            entity instanceof LivingEntity living && living.isLeashed())
                    .map(e -> ((LivingEntity) e)).collect(Collectors.toSet());
            this._owner = owner;
            this._destination = destination;
            _debugLog = verbose ? new LinkedList<>() : null;
        }

        private void log(String text) {
            log(JavaPlugin.getPlugin(HorseTpWithMe.class), text);
        }

        public void log(JavaPlugin plugin, String text) {
            if (_debugLog != null) _debugLog.add("[%s] %s".formatted(plugin.getName(), text.trim()));
        }

        private void storeStackPair(Entity base, Entity rider) {
            _stackMap.put(rider, base);
            log("StackPair Mapped: [TOP: %s %s ] [BOTTOM: %s %s]"
                    .formatted(rider.getType().name(), rider.getEntityId(), base.getType().name(), base.getEntityId()));
        }

        private void storeLeashEntity(Entity leashed, LivingEntity holder) {
            _leashMap.put(leashed, holder);
            log("LeashPair Mapped: %s %s HOLDER: %s"
                    .formatted(leashed.getType().name(), leashed.getEntityId(), _owner.getName()));
        }

        public void initialise() {
            var reversed = new LinkedList<>(_stackMap.keySet());
            Collections.reverse(reversed);

            for (var entity : _leashMap.keySet()) {
                if (!reversed.contains(entity)) reversed.add(entity);
            }

            for (Entity entity : reversed) {
                entity.eject();
                _owner.hideEntity(plugin, entity);
                entity.setFallDistance(-Float.MAX_VALUE);
                if (_leashMap.containsKey(entity)) ((LivingEntity) entity).setLeashHolder(null);
                entity.teleport(_destination);
            }
        }

        public void execute() {
            for (var entity : _stackMap.keySet()) {
                var valid = plugin.tryValidate(entity);
                if (valid == null) continue;
                var vehicle = plugin.tryValidate(_stackMap.get(entity));
                if (vehicle == null) continue;
                _owner.showEntity(plugin, valid);
                _owner.showEntity(plugin, vehicle);
                vehicle.addPassenger(valid);
            }
            for (var entity : _leashMap.keySet()) {
                var valid = plugin.tryValidate(entity);
                var leashed = plugin.tryValidate(_leashMap.get(entity));
                if (leashed != null) if (valid != null) valid.teleport(leashed);
            }
        }

        public void finalise() {
            for (var entity : _leashMap.keySet()) {
                var valid = plugin.tryValidate(entity);
                if (valid != null) {
                    ((LivingEntity) valid).setLeashHolder(_leashMap.get(entity));
                    valid.setFallDistance(-Float.MAX_VALUE);
                }
            }
        }
    }

    private enum PermissionType {
        TP_VEHICLE, // Teleporting your vehicle
        TP_VEHICLE_PASSENGER, // Teleporting other passengers in your vehicle
        TP_LEASH, // Teleporting entities leashed to you
        TP_LEASH_STACK, // Teleporting entities "stacked" (i.e. passengers on passengers) on leashed entities
        TP_PLAYER_STACK // Teleporting entities stacked on the player.
    }

    private @Nullable Entity tryValidate(@NotNull Entity entity) {
        return entity.isValid() ? entity : getServer().getEntity(entity.getUniqueId());
    }

    private boolean permissionDenied(@NotNull PermissionType pType, @NotNull Entity entity, TeleportRecord tpr) {
        var format = switch (pType) {
            case TP_VEHICLE -> "vehicle";
            case TP_VEHICLE_PASSENGER -> "passenger";
            case TP_LEASH -> "leash";
            case TP_LEASH_STACK -> "stack";
            case TP_PLAYER_STACK -> "player";
        };
        format = "horsetpwithme.%s%s." + format + entity.getType().name();

        final var teleportNode = format.formatted("teleport", "");
        final var worldNode = format.formatted("world.", tpr._destination.getWorld().getName());

        final var requiredPerms = new ArrayList<String>();
        requiredPerms.add(teleportNode);
        requiredPerms.add(worldNode);

        {
            if (entity instanceof Steerable steerable) {
                if (!steerable.hasSaddle()) {
                    requiredPerms.add(format.formatted("setting.", "nosaddle"));
                }
            } else if (entity instanceof AbstractHorse horse) {
                if (horse.getInventory().getSaddle() == null) {
                    requiredPerms.add(format.formatted("setting.", "nosaddle"));
                }
            }
            if (entity instanceof Tameable tameable) {
                if (!tameable.isTamed()) {
                    requiredPerms.add(format.formatted("setting.", "untamed"));
                }
            }
            if (entity instanceof Ageable ageable) {
                if (!ageable.isAdult()) {
                    requiredPerms.add(format.formatted("setting.", "notadult"));
                }
            }
        }

        final boolean[] result = {false, true};
        requiredPerms.replaceAll(perm -> {
            result[1] = !tpr._owner.hasPermission(perm);
            if (result[1] && !result[0]) result[0] = true;
            return "  - %s:%s".formatted(perm, result[1] ? "denied" : "authorised");
        });

        if (verboseDebugging) {
            tpr.log("""
                    [PermissionQuery]
                      PermType: %s
                      EntityType: %s
                      PlayerToCheck: %s
                      Destination:
                        %s
                      NodeCheck:
                      """.formatted(
                    pType.name(),
                    entity.getType().name(),
                    tpr._owner.getName() + " " + tpr._owner.getUniqueId(),
                    "x%.2f y%.1f z%.2f [world: %s]".formatted(
                            tpr._destination.getX(),
                            tpr._destination.getY(),
                            tpr._destination.getZ(),
                            tpr._destination.getWorld().getName()
                    )
            ));
            requiredPerms.forEach(tpr::log);
            tpr.log("""
                      Conclusion:
                        Teleport is %sAuthorised.
                    [EndPermissionQuery]
                    """.formatted(result[0] ? "NOT " : ""));
        }
        return result[0];
    }
}