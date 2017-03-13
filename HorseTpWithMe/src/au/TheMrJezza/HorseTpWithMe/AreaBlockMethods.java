package au.TheMrJezza.HorseTpWithMe;

import java.io.IOException;

import org.bukkit.Location;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

public class AreaBlockMethods {
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
		if (AreaBlock.worldGuard == false)
			return null;
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
	
	public static String cancelEvent(Location loc) { 
		if (AreaBlock.griefPrevention) {
			Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, null);
			if (claim != null) {
				if (AreaBlock.blockedClaims.contains(claim.getID())) {
					return "Animals cannot teleport into this Claim!";
				}
			}
		}
		
		if (getRegions(loc) != null) {
			for (ProtectedRegion region : getRegions(loc).getApplicableRegions(loc)) {
				if (contains(region.getId())) {
					return "Animals cannot teleport into this Region!";
				}
				region.setFlag(new StateFlag("ride", true), State.ALLOW);
			}
		} return null;
	}
}
