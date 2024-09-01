package unused.com.jbouchier.horsetpwithme;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.Arrays;
import java.util.Objects;

public class PermissionHandler {
    private final static String base = "horsetpwithme.";
    private final static String cmd = base + "command.";
    private final static String tp = base + "teleport.";
    private final static String msg = base + "messages.";
    private static boolean built = false;

    private PermissionHandler() {
        // nope
    }

    public static void build() {
        if (built) return;
        built = true;
        // top-level wild card
        Permission mainPerm = new Permission(base + '*', PermissionDefault.OP);
        registerPerm(mainPerm);

        make("notify_updates", mainPerm, true);
        // global perms
        bulk(mainPerm, false, base, "require_saddle", "require_tamed", "require_adult",
                "deny_passenger_teleport", "match_vehicle_teleports");

        // messages
        Permission foo = make(msg + '*', mainPerm, true);
        bulk(foo, true, msg, Arrays.stream(Language.MessageKey.values())
                .map(s -> s.name().toLowerCase()).toList().toArray(new String[0]));

        // world perms
        foo = make(base + "deny_cross_world", mainPerm, false);
        {
            // disposable helper
            Permission bar = make(base + "empty_chests_cross_world", mainPerm, false);
            for (World w : Bukkit.getWorlds()) {
                bulk(base, w.getName(), foo, true, "disabled_world.", "disabled_world_from.");
                bulk(base, w.getName(), bar, true, "empty_chests_to.", "empty_chests_from.");
            }

            // teleport
            foo = make(tp + '*', mainPerm, true);

            // vanilla-teleport
            bar = make(tp + "vanilla", foo, true);

            Location uber = new Location(Bukkit.getWorlds().get(0), 0, -100, 0);
            for (EntityType type : EntityType.values()) {
                if (!type.isSpawnable()) continue;
                boolean isVehicle;

                try {
                    Entity entity = Objects.requireNonNull(uber.getWorld()).spawnEntity(uber, type);
                    isVehicle = entity instanceof Vehicle;
                    entity.remove();
                } catch (IllegalArgumentException | NullPointerException ignored) {
                    continue;
                }
                make(tp + type.name().toLowerCase(), isVehicle ? bar : foo, true);
            }
        }

        // commands
        foo = make(cmd + '*', mainPerm, true);
        bulk(foo, true, cmd, "taptoggle", "net/horsetpwithme", "reseat");

        foo = make("horsetpwithme.default", null, true);
        String[] defaultPerms = {tp + "vanilla", base + "require_tamed",
                base + "require_saddle", base + "require_adult", cmd + "taptoggle", base + "match_vehicle_teleports"};

        for (String p : defaultPerms)
            Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPermission(p)).addParent(foo, true);
    }

    private static void bulk(String base, String suffix, Permission parent, boolean def, String... perms) {
        for (String perm : perms) make(base + perm + suffix, parent, def);
    }

    private static void bulk(Permission parent, boolean def, String base, String... perms) {
        bulk(base, "", parent, def, perms);
    }

    private static Permission make(String name, Permission parent, boolean def) {
        Permission perm = new Permission(name, parent == null && def ? PermissionDefault.TRUE : PermissionDefault.FALSE);
        if (parent != null) perm.addParent(parent, def);
        registerPerm(perm);
        return perm;
    }

    private static void registerPerm(Permission permission) {
        Bukkit.getServer().getPluginManager().addPermission(permission);
    }
}