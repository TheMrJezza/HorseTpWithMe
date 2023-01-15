package com.jbouchier.horsetpwithme.cmd;

import com.jbouchier.horsetpwithme.Language;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.jetbrains.annotations.NotNull;

import static com.jbouchier.horsetpwithme.cmd.PlayerCommand.PLAYER_ONLY_MESSAGE;

public class MainCommand implements CommandExecutor {

    @Override
    public boolean onCommand(
            @NotNull CommandSender cs, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args
    ) {
        if (args.length < 1) return false;
        if (args[0].equalsIgnoreCase("reload")) {
            Language.reload();
            cs.sendMessage("HorseTpWithMe Files reloaded!");
        } else if (args[0].equalsIgnoreCase("getperm")) {
            if (cs instanceof Player player) {
                Entity vehicle = player.getVehicle();
                if (vehicle == null) {
                    cs.sendMessage("You must be in a vehicle to use this command!");
                } else {
                    EntityType type = vehicle.getType();
                    if (!type.isSpawnable()) {
                        cs.sendMessage("This vehicle isn't supported!");
                    } else {
                        cs.sendMessage("Single: horsetpwithme.teleport." + type.name().toLowerCase());
                        if (vehicle instanceof Vehicle) {
                            cs.sendMessage("Parent: horsetpwithme.teleport.vanilla");
                            cs.sendMessage("Master: horsetpwithme.teleport.*");
                        } else {
                            cs.sendMessage("Parent: horsetpwithme.teleport.*");
                        }
                    }
                }
            } else {
                cs.sendMessage(PLAYER_ONLY_MESSAGE);
            }
        } else return false;
        return true;
    }
}