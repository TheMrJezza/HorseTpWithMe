package net.horsetpwithme;

import net.horsetpwithme.api.DataStoreHTWM;
import net.horsetpwithme.api.PermissionHTWM;
import net.horsetpwithme.listener.PlayerListener;
import net.horsetpwithme.reason.LeashedReason;
import net.horsetpwithme.reason.PassengerReason;
import net.horsetpwithme.reason.SittableReason;
import net.horsetpwithme.reason.VehicleReason;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class HorseTpWithMe extends JavaPlugin {
    static final int B_STATS_PLUGIN_ID = 3502;
    static final String FAILED_UPDATE_CHECK = "Failed Update Check: ";
    static final String NEW_UPDATE = "A new version of HorseTpWithMe is available from SpigotMC";
    static final String CHECKING_FOR_UPDATES = "Checking for updates...";

    static final String TEST_FORMAT = ChatColor.DARK_GRAY + "[ %s " + ChatColor.DARK_GRAY + "] %s";
    static final String TEST_FAILED = ChatColor.RED + "FAIL";
    static final String TEST_PASSED = ChatColor.GOLD + "PASS";
    static final String PLUGIN_WORKING = ChatColor.GREEN + "HorseTpWithMe is enabled and working!";
    static final String UNSUPPORTED = "Unsupported Server! HTWM will be disabled!";

    public static final class API {
        static final HorseTpWithMe INSTANCE = JavaPlugin.getPlugin(HorseTpWithMe.class);
        public static final DataStoreHTWM DATA_STORE = new DataHTWM(INSTANCE);
        public static final PermissionHTWM PERMS = new PermissionCalcHTWM(INSTANCE);

        private API() {}
    }

    @Override
    public void onEnable() {
        final var ccs = getServer().getConsoleSender();

        var failed = false;
        var outcome = (String) null;
        try {
            Player.class.getDeclaredMethod("showEntity", Plugin.class, Entity.class);
            Player.class.getDeclaredMethod("hideEntity", Plugin.class, Entity.class);
            outcome = TEST_PASSED;
        } catch (NoSuchMethodException unexpected) {
            failed = true;
            outcome = TEST_FAILED;
        }
        ccs.sendMessage("");
        ccs.sendMessage(TEST_FORMAT.formatted(outcome, "Bukkit Show/Hide API detected."));

        try {
            Chunk.class.getDeclaredMethod("removePluginChunkTicket", Plugin.class);
            Chunk.class.getDeclaredMethod("addPluginChunkTicket", Plugin.class);
            outcome = TEST_PASSED;
        } catch (NoSuchMethodException unexpected) {
            failed = true;
            outcome = TEST_FAILED;
        }
        ccs.sendMessage(TEST_FORMAT.formatted(outcome, "Chunk Plugin Ticket API detected."));

        try {
            Entity.class.getDeclaredMethod("addPassenger", Entity.class);
            Entity.class.getDeclaredMethod("getPassengers");
            outcome = TEST_PASSED;
        } catch (NoSuchMethodException unexpected) {
            failed = true;
            outcome = TEST_FAILED;
        }
        ccs.sendMessage(TEST_FORMAT.formatted(outcome, "Multi-Passenger Support detected."));

        try {
            Entity.class.getMethod("getPersistentDataContainer");
            outcome = TEST_PASSED;
        } catch (NoSuchMethodException unexpected) {
            failed = true;
            outcome = TEST_FAILED;
        }
        ccs.sendMessage(TEST_FORMAT.formatted(outcome, "PersistentDataContainer API detected."));

        if (failed) {
            getServer().getPluginManager().disablePlugin(this);
            getLogger().severe(UNSUPPORTED);
            return;
        }
        ccs.sendMessage("");

        API.DATA_STORE.registerReason(new VehicleReason());
        API.DATA_STORE.registerReason(new PassengerReason());
        API.DATA_STORE.registerReason(new LeashedReason());
        API.DATA_STORE.registerReason(new SittableReason());

        final var scheduler = getServer().getScheduler();
        scheduler.runTask(this, ((PermissionCalcHTWM) API.PERMS)::rebuild);

        new PlayerListener(this);
        new Metrics(this, B_STATS_PLUGIN_ID);

        ccs.sendMessage(PLUGIN_WORKING);

        getLogger().info(CHECKING_FOR_UPDATES);
        UpdateCheckerHTWM.check(this, (result, error) -> scheduler.runTask(this, () -> {
            if (error != null) {
                getLogger().severe(FAILED_UPDATE_CHECK + error);
            } else {
                if (!this.getDescription().getVersion().equals(result)) {
                    getLogger().warning(NEW_UPDATE);
                    getLogger().warning("New Version: " + result);
                }
            }
        }));
    }
}