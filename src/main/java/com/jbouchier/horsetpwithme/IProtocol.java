package com.jbouchier.horsetpwithme;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface IProtocol {
    void updateVehicle(Player rider, Entity vehicle);
}