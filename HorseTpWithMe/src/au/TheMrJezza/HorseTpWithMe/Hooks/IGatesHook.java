package au.TheMrJezza.HorseTpWithMe.Hooks;

import org.bukkit.Location;

import com.ptibiscuit.igates.Plugin;

public class IGatesHook {
	public static boolean isWaterGate(Location loc) {
		return Plugin.instance.getPortalByPosition(loc, 3) != null;
	}
}