package au.TheMrJezza.HorseTpWithMe.Hooks;

import java.io.IOException;

import org.bukkit.Location;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import au.TheMrJezza.HorseTpWithMe.AreaBlock;

public class WorldGuardHook {

	public static String toggleRegion(Location loc) {
		if (getRegions(loc) != null) {
			StringBuilder sb = new StringBuilder("Affected Regions:");
			for (ProtectedRegion region : getRegions(loc).getApplicableRegions(loc)) {
				if (contains(region.getId().trim())) {
					sb.append("\n - " + region.getId() + " : Unblocked");
					AreaBlock.blockedRegions.remove(region.getId().toLowerCase());
				} else {
					sb.append("\n - " + region.getId() + " : Blocked");
					AreaBlock.blockedRegions.add(region.getId().toLowerCase());
				}
			}

			if (sb.toString().equals("Affected Regions:")) {
				return null;
			}
			AreaBlock.areaBlock.set("BlockedRegions", AreaBlock.blockedRegions);
			try {
				AreaBlock.areaBlock.save(AreaBlock.file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return sb.toString();
		}
		return null;
	}

	private static RegionManager getRegions(Location loc) {
		RegionContainer container = WGBukkit.getPlugin().getRegionContainer();
		return container.get(loc.getWorld());
	}

	private static boolean contains(String str) {
		for (String string : AreaBlock.blockedRegions) {
			if (str.equalsIgnoreCase(string)) {
				return true;
			}
		}
		return false;
	}

	public static String cancelEventWorldGuard(Location loc) {
		if (getRegions(loc) != null) {
			for (ProtectedRegion region : getRegions(loc).getApplicableRegions(loc)) {
				if (contains(region.getId())) {
					return "Animals cannot teleport into this Region!";
				}
				region.setFlag(new StateFlag("ride", true), State.ALLOW);
			}
		}
		return null;
	}
	
}
