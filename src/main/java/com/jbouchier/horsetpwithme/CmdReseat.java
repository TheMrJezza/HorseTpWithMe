package com.jbouchier.horsetpwithme;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CmdReseat implements CommandExecutor {
    private final HorseTpWithMe plugin;

    CmdReseat(HorseTpWithMe plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean onCommand(
            @NotNull CommandSender cs, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args
    ) {
        final var target = MiscUtil.extractTarget(cs, args);
        if (target != null) {
            var vehicle = target.getVehicle();
            if (vehicle == null) cs.sendMessage("No vehicle detected!");
            else {
                if (target.canSee(vehicle)) {
                    target.hideEntity(plugin, vehicle);
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> target.showEntity(plugin, vehicle), 2);
                } else target.showEntity(plugin, vehicle);
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (target.canSee(vehicle)) cs.sendMessage("Reseat Complete!");
                    else cs.sendMessage("Reseat Prevented: A plugin is hiding the vehicle from the player!");
                }, 3);
            }
        }
        return true;
    }
}