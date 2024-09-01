package net.horsetpwithme;

import net.horsetpwithme.api.PermissionHTWM;
import net.horsetpwithme.api.ReasonHTWM;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PermissionCalcHTWM implements PermissionHTWM {
    static final String WILD_SELECTOR = "*";
    static final String TP_PERM_PREFIX = "horsetpwithme.teleport.";

    private final HorseTpWithMe plugin;

    PermissionCalcHTWM(HorseTpWithMe plugin) {
        this.plugin = plugin;
    }

    public void rebuild() {
        final var worlds = plugin.getServer().getWorlds();
        final var reasons = HorseTpWithMe.API.DATA_STORE.reasons();
        EnumSet.allOf(EntityType.class).forEach(eT -> worlds.forEach(wF -> worlds.forEach(wT -> {
            reasons.stream().filter(reason -> reason.appliesTo(eT, wF, wT)).forEach(reason -> {
                final var rootNode = makeTeleportNode(wF, wT, reason, eT);
                registerNodeAndParents(rootNode);
            });
        })));
    }

    static Permission findOrCreate(@NotNull PermNode node) {
        var permission = Bukkit.getPluginManager().getPermission(node.toString());
        if (permission == null) {
            permission = new Permission(node.toString(), PermissionDefault.FALSE);
            Bukkit.getPluginManager().addPermission(permission);
        }
        return permission;
    }

    private void registerNodeAndParents(@NotNull PermNode node) {
        final var permission = findOrCreate(node);
        node.getDirectParents().forEach(parentNode -> {
            final var parentPermission = findOrCreate(parentNode);
            permission.addParent(parentPermission, true);
            registerNodeAndParents(parentNode);
        });
    }

    @Override
    public @NotNull String getRequiredPermission(
            @NotNull World worldFrom,
            @NotNull World worldTo,
            @NotNull ReasonHTWM reasonHTWM,
            @NotNull EntityType entityType) {
        return makeTeleportNode(worldFrom, worldTo, reasonHTWM, entityType).toString();
    }

    // PermNode is public, in-case someone else finds it useful.
    public record PermNode(String prefix, String... parts) {
        @NotNull Stream<PermNode> getDirectParents() {
            return IntStream.range(0, parts.length).mapToObj(i -> {
                final var clone = parts.clone();
                clone[i] = WILD_SELECTOR;
                return new PermNode(prefix, clone);
            }).filter(next -> !this.equals(next));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PermNode that)) return false;
            return Objects.deepEquals(parts, that.parts);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(parts);
        }

        @Override
        public @NotNull String toString() {
            return (prefix == null ? "" : prefix) + Stream.of(parts)
                    .reduce((a, b) -> a + "." + b).orElse("").toLowerCase();
        }
    }

    public static PermNode makeTeleportNode(
            @NotNull World worldF,
            @NotNull World worldT,
            @NotNull ReasonHTWM reason,
            @NotNull EntityType entityType) {
        final var wF = worldF.getName();
        final var wT = worldT.getName();
        final var rN = reason.getReasonName();
        final var eN = entityType.name();
        return new PermNode(TP_PERM_PREFIX, wF, wT, rN, eN);
    }
}