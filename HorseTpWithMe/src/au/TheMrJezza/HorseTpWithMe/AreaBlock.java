package au.TheMrJezza.HorseTpWithMe;

import java.io.File;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.configuration.file.YamlConfiguration;

public class AreaBlock {
	public static File file = null;
	public static YamlConfiguration areaBlock = null;

	public static List<String> blockedRegions = null;
	public static List<Long> blockedClaims = null;

	private static void replace(List<String> strings) {
		ListIterator<String> iterator = strings.listIterator();
		while (iterator.hasNext()) {
			iterator.set(iterator.next().toLowerCase());
		}
	}

	public static void reload() {
		file = new File(Main.getInstance().getDataFolder(), "AreaBlock.yml");
		if (!file.exists())
			Main.getInstance().saveResource("AreaBlock.yml", true);
		areaBlock = YamlConfiguration.loadConfiguration(file);
		blockedRegions = areaBlock.getStringList("BlockedRegions");
		blockedClaims = areaBlock.getLongList("BlockedClaims");
		replace(blockedRegions);
	}
}