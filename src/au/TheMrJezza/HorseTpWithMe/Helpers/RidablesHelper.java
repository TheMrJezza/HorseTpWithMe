package au.TheMrJezza.HorseTpWithMe.Helpers;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import net.pl3x.bukkit.ridables.entity.RidableType;

public class RidablesHelper {
	
	public static String getRideablePerm(Entity entity) {
		if (RidableType.getRidable(entity) == null) return null;
		return "horsetpwithme.ridables." + RidableType.getRidableType(entity.getType()).getName().toLowerCase();
	}
	
	public static Set<String> getAllRidables() {
		Set<String> result = new HashSet<String>();
		for (EntityType type : EntityType.values()) {
			RidableType rType = RidableType.getRidableType(type);
			if (rType == null) continue;
			result.add(rType.getName().toLowerCase());
		}
		return result;
	}
}