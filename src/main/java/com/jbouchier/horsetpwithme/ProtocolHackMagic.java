package com.jbouchier.horsetpwithme;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.protocol.game.PacketPlayOutUpdateAttributes;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.attributes.AttributeMapBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifiable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

public class ProtocolHackMagic implements IProtocol {

    private final Method dataWatcher = getMethod(net.minecraft.world.entity.Entity.class, DataWatcher.class);
    private final Method attributeMapBase = getMethod(EntityLiving.class, AttributeMapBase.class);
    private final Method attributeMod = getMethod(AttributeMapBase.class, Collection.class);
    private final Field playerConnection = getConnection();
    private final Method sendPacket = getPacketSender();

    public ProtocolHackMagic() {
        if (dataWatcher == null || attributeMapBase == null || attributeMod == null || playerConnection == null || sendPacket == null) {
            JavaPlugin.getPlugin(HorseTpWithMe.class).getLogger().warning("Server Protocol Unsupported!");
            StringBuilder sb = new StringBuilder("Bad Protocol(s): [ ");
            if (dataWatcher == null) sb.append("dataWatcher, ");
            if (attributeMapBase == null) sb.append("attributeMapBase, ");
            if (attributeMod == null) sb.append("attributeMod, ");
            if (playerConnection == null) sb.append("playerConnection, ");
            if (sendPacket == null) sb.append("sendPacket, ");
            sb.replace(sb.length() - 2, sb.length(), " ]");
            JavaPlugin.getPlugin(HorseTpWithMe.class).getLogger().warning(sb.toString());
        }
    }

    private static Method getPacketSender() {
        for (Method method : PlayerConnection.class.getDeclaredMethods()) {
            Class<?>[] params = method.getParameterTypes();
            if (params.length == 1 && params[0].equals(Packet.class))
                return method;
        }
        return null;
    }

    private static Method getMethod(Class<?> clazz, Class<?> ret) {
        for (Method m : clazz.getMethods()) {
            if (m.getReturnType().equals(ret)) return m;
        }
        return null;
    }

    private static Field getConnection() {
        for (Field f : EntityPlayer.class.getFields()) {
            if (f.getType().equals(PlayerConnection.class)) return f;
        }
        return null;
    }

    @Override
    public void updateVehicle(Player rider, Entity vehicle) {
        if (dataWatcher == null || attributeMapBase == null || attributeMod == null || playerConnection == null || sendPacket == null) {
            return;
        }
        try {
            final net.minecraft.world.entity.Entity handle = (net.minecraft.world.entity.Entity)
                    vehicle.getClass().getMethod("getHandle").invoke(vehicle);
            sendPackets(rider,
                    new PacketPlayOutSpawnEntity(handle),
                    new PacketPlayOutEntityMetadata(vehicle.getEntityId(), (DataWatcher) dataWatcher.invoke(handle), true)
            );

            if (handle instanceof EntityLiving living) {
                AttributeMapBase base = (AttributeMapBase) attributeMapBase.invoke(living);
                sendPackets(rider, new PacketPlayOutUpdateAttributes(vehicle.getEntityId(),
                        (Collection<AttributeModifiable>) attributeMod.invoke(base)
                ));
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private void sendPackets(Player player, Packet<?>... packets) {
        try {
            EntityPlayer craftPlayer = (EntityPlayer) player.getClass().getMethod("getHandle").invoke(player);
            for (Packet<?> packet : packets) {
                assert playerConnection != null;
                PlayerConnection pc = (PlayerConnection) playerConnection.get(craftPlayer);
                assert sendPacket != null;
                sendPacket.invoke(pc, packet);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}