package net.horsetpwithme;

import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.net.URI;
import java.util.Scanner;
import java.util.function.BiConsumer;

public class UpdateCheckerHTWM {
    static final String API_URL = "https://api.spigotmc.org";
    static final String API_ENDPOINT = API_URL + "/legacy/update.php?resource=8186/~";
    static final URI FULL_API_URI = URI.create(API_ENDPOINT);

    public static void check(Plugin plugin, final BiConsumer<String, String> resultError) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (var is = FULL_API_URI.toURL().openStream(); var sc = new Scanner(is)) {
                if (sc.hasNext()) {
                    resultError.accept(sc.nextLine(), null);
                }
            } catch (IOException e) {
                resultError.accept(null, e.getMessage());
            }
        });
    }
}