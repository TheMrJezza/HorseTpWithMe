package com.jbouchier.horsetpwithme.nms;

import com.jbouchier.horsetpwithme.IProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.protocol.game.PacketPlayOutUpdateAttributes;
import net.minecraft.world.entity.EntityLiving;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Protocol_v1_17_R1 implements IProtocol {

    @Override
    public void updateVehicle(Player rider, Entity vehicle) {
        final net.minecraft.world.entity.Entity handle = ((CraftEntity) vehicle).getHandle();

        sendPackets(rider,
                new PacketPlayOutSpawnEntity(handle),
                new PacketPlayOutEntityMetadata(vehicle.getEntityId(), handle.getDataWatcher(), true)
        );

        if (handle instanceof EntityLiving living)
            sendPackets(rider, new PacketPlayOutUpdateAttributes(vehicle.getEntityId(), living.getAttributeMap().b()));
    }

    private void sendPackets(Player player, Packet<?>... packets) {
        for (Packet<?> packet : packets) ((CraftPlayer) player).getHandle().b.sendPacket(packet);
    }
}