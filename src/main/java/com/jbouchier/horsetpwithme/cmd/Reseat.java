package com.jbouchier.horsetpwithme.cmd;

import com.jbouchier.horsetpwithme.util.EntityNMS;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Reseat extends PlayerCommand {
    @Override
    public void execute(@NotNull Player player, @NotNull String[] args) {
        Entity vehicle = player.getVehicle();
        if (vehicle != null) {
            EntityNMS.sendPassengers(player, vehicle);
        } else player.sendMessage("No need to reseat you!");
    }
}