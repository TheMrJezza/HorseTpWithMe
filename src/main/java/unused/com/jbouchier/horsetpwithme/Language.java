package unused.com.jbouchier.horsetpwithme;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public final class Language {

    private static final HorseTpWithMe PLUGIN = JavaPlugin.getPlugin(HorseTpWithMe.class);
    private static final String FILE_NAME = "Language.yml";
    private static final Path FILE_PATH = PLUGIN.getDataFolder().toPath().resolve(FILE_NAME);

    public enum MessageKey {
        NO_TELEPORT_PERMISSION,
        MUST_BE_TAMED,
        MUST_BE_ADULT,
        MUST_HAVE_SADDLE,
        DESTINATION_WORLD_DISABLED,
        CANNOT_LEAVE_WORLD,
        TELEPORT_AS_PASSENGER_ENABLED,
        TELEPORT_AS_PASSENGER_DISABLED,
        CHEST_EMPTIED_FROM_WORLD,
        CHEST_EMPTIED_TO_WORLD,
        PASSENGERS_FILTERED
    }

    private static final HashMap<MessageKey, String> messages = new HashMap<>(MessageKey.values().length);

    private Language() {
        // no need to construct this
    }

    public static void reload() {
        if (Files.notExists(FILE_PATH)) {
            PLUGIN.saveResource(FILE_NAME, true);
        }
        YamlConfiguration lang;
        try (BufferedReader reader = Files.newBufferedReader(FILE_PATH)) {
            lang = YamlConfiguration.loadConfiguration(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (MessageKey key : MessageKey.values()) {
            String value = ChatColor.translateAlternateColorCodes('&', lang.getString(key.name(), ""));
            if (ChatColor.stripColor(value).isBlank()) continue;
            messages.put(key, value);
        }
    }

    public static String getMessage(MessageKey key) {
        return messages.get(key);
    }
}