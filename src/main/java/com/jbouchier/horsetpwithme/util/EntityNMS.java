package com.jbouchier.horsetpwithme.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// SPOOKY NMS STUFF -> Current Version -> 1.19.3
public class EntityNMS {

    public static void refreshEntity(
            @NotNull Player player, @NotNull org.bukkit.entity.Entity toBeRefreshed
    ) {
        Entity handle = ((CraftEntity) toBeRefreshed).getHandle();
        Collection<Packet<?>> packets = new ArrayList<>();

        // Spawn Packet
        packets.add(new ClientboundAddEntityPacket(handle));

        // Metadata
        {
            List<SynchedEntityData.DataValue<?>> values = handle.getEntityData().getNonDefaultValues();
            if (values != null)
                packets.add(new ClientboundSetEntityDataPacket(toBeRefreshed.getEntityId(), values));
        }

        if (handle instanceof LivingEntity living) {

            // Equipment
            {
                List<Pair<EquipmentSlot, ItemStack>> list = new ArrayList<>();
                for (EquipmentSlot slot : EquipmentSlot.values())
                    list.add(new Pair<>(slot, living.getItemBySlot(slot)));

                packets.add(new ClientboundSetEquipmentPacket(
                        toBeRefreshed.getEntityId(), list
                ));
            }

            // Attributes Packet (Health, Hunger, Potions etc)
            packets.add(new ClientboundUpdateAttributesPacket(
                    toBeRefreshed.getEntityId(),
                    living.getAttributes().getSyncableAttributes()
            ));
        }

        for (Packet<?> packet : packets) ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    public static void sendPassengers(@NotNull Player player, @NotNull org.bukkit.entity.Entity entity) {
        sendPassengers(player, ((CraftEntity) entity).getHandle());
    }

    public static void sendPassengers(@NotNull Player player, @NotNull Entity entity) {
        if (!entity.getPassengers().isEmpty()) {
            ((CraftPlayer) player).getHandle().connection.send(new ClientboundSetPassengersPacket(entity));
        }
    }
}