package com.jbouchier.horsetpwithme;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.LinkedList;

@SuppressWarnings("UnstableApiUsage")
class TeleportRecord {

    private final Player owner;
    private final Location destination;

    private final LinkedHashMap<LivingEntity, Entity> leashed;
    private final LinkedHashMap<Entity, Entity> stacked;
    private final LinkedList<Entity> tpRecord;
    private final JavaPlugin plugin;

    TeleportRecord(Player player, Location destination, JavaPlugin plugin) {
        this.owner = player;
        leashed = new LinkedHashMap<>();
        stacked = new LinkedHashMap<>();
        tpRecord = new LinkedList<>();
        this.destination = destination;
        this.plugin = plugin;
    }

    void addLeashPair(Entity holder, LivingEntity leashed) {
        this.leashed.put(leashed, holder);
        if (!tpRecord.contains(leashed)) tpRecord.add(leashed);
        if (!tpRecord.contains(holder)) tpRecord.add(holder);
    }

    void addStackPair(Entity bottom, Entity top) {
        if (top instanceof Player player && !player.equals(owner) && !MiscUtil.getTapStatus(player)) {
            if (DataState.getInstance().isDebugOn()) {
                Bukkit.broadcastMessage("§7[§aHTpWM Debug§7] §cPlayer Passenger Excluded§7: §a" + owner.getName());
                Bukkit.broadcastMessage("Info: " + player.getName() + " has T.A.P disabled.");
            }
            return;
        }
        this.stacked.put(top, bottom);
        if (!tpRecord.contains(bottom)) tpRecord.add(bottom);
        if (!tpRecord.contains(top)) tpRecord.add(top);
    }

    boolean hasWork() {
        return !tpRecord.isEmpty();
    }

    void teleport() {
        while (!tpRecord.isEmpty()) {
            var entity = tpRecord.removeLast();
            if (entity.equals(owner)) continue;
            entity.eject();
            owner.hideEntity(plugin, entity);
            if (entity instanceof LivingEntity living) {
                if (leashed.containsKey(living)) living.setLeashHolder(null);
            }
            entity.teleport(destination);
            entity.setFallDistance(-Float.MAX_VALUE);
        }
    }

    void reconstruct() {
        for (var rider : stacked.keySet()) {
            var vehicle = stacked.get(rider);
            owner.showEntity(plugin, rider);
            owner.showEntity(plugin, vehicle);
            vehicle.addPassenger(rider);
        }
        for (var leashed : this.leashed.keySet()) {
            var holder = this.leashed.get(leashed);
            owner.showEntity(plugin, leashed);
            owner.showEntity(plugin, holder);
        }
    }

    void fixLeads() {
        for (var leashed : this.leashed.keySet()) {
            var holder = this.leashed.get(leashed);
            leashed.teleport(holder);
            leashed.setFallDistance(-Float.MAX_VALUE);
            leashed.setLeashHolder(holder);
        }
    }
}