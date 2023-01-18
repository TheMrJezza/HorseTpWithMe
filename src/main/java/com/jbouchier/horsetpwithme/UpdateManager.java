package com.jbouchier.horsetpwithme;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jbouchier.horsetpwithme.util.DetailLogger;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.jbouchier.horsetpwithme.util.GeneralUtil.runAsync;
import static com.jbouchier.horsetpwithme.util.GeneralUtil.runTask;

public class UpdateManager {

    private static String LATEST = null;
    private static final Version LOCAL = new Version(
            JavaPlugin.getPlugin(HorseTpWithMe.class).getDescription().getVersion()
    );

    static void grabLatestAndNotify() {
        if (LATEST != null) return;
        final String USER_AGENT = "HtpwmChecker";
        final String REQUEST_URL = "https://api.spigotmc.org/simple/0.2/index.php?action=getResource&id=8186";
        runAsync(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(REQUEST_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.addRequestProperty("User-Agent", USER_AGENT);

                try (
                        InputStream inputStream = connection.getInputStream();
                        InputStreamReader reader = new InputStreamReader(inputStream)
                ) {
                    JsonObject element = JsonParser.parseReader(reader).getAsJsonObject();
                    final Version latest = new Version(element.get("current_version").getAsString());
                    if (latest.compareTo(LOCAL) > 0) {
                        LATEST = latest.toString();
                        runTask(() -> {
                            DetailLogger.forceLog("§eA new version of HorseTpWithMe is available from SpigotMC!");
                            DetailLogger.forceLog("§eCurrent Version§7: §6%s §eNew Version§7: §a%s", LOCAL, latest);
                        }, 0);
                    }
                }
            } catch (IOException e) {
                runTask(() -> DetailLogger.forceLog("§cFailed to GET latest version!"), 0);
                e.printStackTrace();
            } finally {
                if (connection != null) connection.disconnect();
            }
        }, 5);
    }

    static void notify(CommandSender cs) {
        if (isPluginUpdateAvailable()) {
            DetailLogger.forceLog(cs, "§eA new version of HorseTpWithMe is available from SpigotMC!");
            DetailLogger.forceLog(cs, "§eCurrent Version§7: §6%s §eNew Version§7: §a%s", LOCAL, LATEST);
        }
    }

    private UpdateManager() {
        // nope
    }

    public static boolean isPluginUpdateAvailable() {
        return LATEST != null;
    }

    static void tryUpgradeLanguageFile() {
        // TODO implement this
    }
}