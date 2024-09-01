package net.horsetpwithme.listener;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.BiConsumer;

public class EntityUnmountListener implements Listener {
    static final String VEHICLE_EXIT_EVENT = "org.bukkit.event.vehicle.VehicleExitEvent";
    static final String VEE_GET_VEHICLE = "getVehicle";
    static final String VEE_GET_EXITED = "getExited";

    static final String B_ENTITY_DISMOUNT_EVENT = "org.bukkit.event.entity.EntityDismountEvent";
    static final String S_ENTITY_DISMOUNT_EVENT = "org.spigotmc.event.entity.EntityDismountEvent";
    static final String EDE_GET_DISMOUNTED = "getDismounted";
    static final String EDE_GET_ENTITY = "getEntity";

    static final MethodHandle READ_VEHICLE_METHOD;
    static final MethodHandle READ_EJECTED_METHOD;
    static final Class<? extends Event> UNMOUNT_EVENT;

    static {
        try {
            var event = (Class<?>) null;
            var getVehicle = (MethodHandle) null;
            var getEjected = (MethodHandle) null;
            final var lk = MethodHandles.lookup();
            try {
                try {
                    event = Class.forName(B_ENTITY_DISMOUNT_EVENT);
                } catch (ClassNotFoundException handled) {
                    event = Class.forName(S_ENTITY_DISMOUNT_EVENT);
                }
                getVehicle = lk.unreflect(event.getDeclaredMethod(EDE_GET_DISMOUNTED));
                getEjected = lk.unreflect(event.getMethod(EDE_GET_ENTITY));
            } catch (ClassNotFoundException handled) {
                event = Class.forName(VEHICLE_EXIT_EVENT);
                getVehicle = lk.unreflect(event.getMethod(VEE_GET_VEHICLE));
                getEjected = lk.unreflect(event.getDeclaredMethod(VEE_GET_EXITED));
            }
            final var methodType = MethodType.methodType(Entity.class, Event.class);
            READ_EJECTED_METHOD = getEjected.asType(methodType);
            READ_VEHICLE_METHOD = getVehicle.asType(methodType);
            UNMOUNT_EVENT = event.asSubclass(Event.class);
        } catch (ReflectiveOperationException unexpected) {
            throw new ExceptionInInitializerError(unexpected);
        }
    }

    static @NotNull Entity readVehicle(@NotNull Event event) {
        try {
            return (Entity) READ_VEHICLE_METHOD.invokeExact(event);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static @NotNull Entity readEjected(@NotNull Event event) {
        try {
            return (Entity) READ_EJECTED_METHOD.invokeExact(event);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public EntityUnmountListener(
            @NotNull Plugin plugin,
            @NotNull final BiConsumer<Entity, Entity> biConsumer) {
        final var pm = plugin.getServer().getPluginManager();
        pm.registerEvent(UNMOUNT_EVENT, this, EventPriority.MONITOR, (listener, event) ->
                biConsumer.accept(readEjected(event), readVehicle(event)), plugin, true);
    }
}