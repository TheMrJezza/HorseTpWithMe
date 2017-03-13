package au.TheMrJezza.HorseTpWithMe;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

public class AreaBlock {
	public static File file = null;
	public static YamlConfiguration areaBlock = null;

	public static boolean worldGuard = false;
	public static boolean griefPrevention = false;
	public static List<String> blockedRegions = null;
	public static List<Long> blockedClaims = null;

	public static String toggleClaim(Location loc) {
		if (!griefPrevention)
			return null;
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, null);
		if (claim == null)
			return null;
		String string;
		if (blockedClaims.contains(claim.getID())) {
			blockedClaims.remove(claim.getID());
			string = "This claim is no longer blocked";
		} else {
			blockedClaims.add(claim.getID());
			string = "This claim is now blocked";
		}
		areaBlock.set("BlockedClaims", blockedClaims);
		try {
			areaBlock.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return string;
	}

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
		Plugin WG = Bukkit.getPluginManager().getPlugin("WorldGuard");
		worldGuard = WG != null && WG.isEnabled();
		Plugin GP = Bukkit.getPluginManager().getPlugin("GriefPrevention");
		griefPrevention = GP != null && GP.isEnabled();
		blockedRegions = areaBlock.getStringList("BlockedRegions");
		blockedClaims = areaBlock.getLongList("BlockedClaims");
		replace(blockedRegions);
	}
}
