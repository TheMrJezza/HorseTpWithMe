package au.TheMrJezza.HorseTpWithMe.Hooks;

import java.io.IOException;

import org.bukkit.Location;

import au.TheMrJezza.HorseTpWithMe.AreaBlock;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

public class GriefPreventionHook {
	public static String cancelEventGriefPrevention(Location loc) {
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, null);
		if (claim != null) {
			if (AreaBlock.blockedClaims.contains(claim.getID())) {
				return "Animals cannot teleport into this Claim!";
			}
		}
		return null;
	}
	
	public static String toggleClaim(Location loc) {
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, null);
		if (claim == null)
			return null;
		String string;
		if (AreaBlock.blockedClaims.contains(claim.getID())) {
			AreaBlock.blockedClaims.remove(claim.getID());
			string = "This claim is no longer blocked";
		} else {
			AreaBlock.blockedClaims.add(claim.getID());
			string = "This claim is now blocked";
		}
		AreaBlock.areaBlock.set("BlockedClaims", AreaBlock.blockedClaims);
		try {
			AreaBlock.areaBlock.save(AreaBlock.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return string;
	}
}
