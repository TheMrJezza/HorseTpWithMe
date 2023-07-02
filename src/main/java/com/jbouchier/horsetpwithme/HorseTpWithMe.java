package com.jbouchier.horsetpwithme;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class HorseTpWithMe extends JavaPlugin {

    boolean verboseDebugging = false;

    public final void onEnable() {
        PermissionState.buildPerms(this);
        new Listeners(this);
    }

    public boolean onCommand(@NotNull CommandSender cs, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        switch (cmd.getName()) {
            case "HorseDebug" -> {
                verboseDebugging = !verboseDebugging;
                cs.sendMessage("Verbose Debugging: %s".formatted(verboseDebugging ? "ENABLED" : "DISABLED"));
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
}