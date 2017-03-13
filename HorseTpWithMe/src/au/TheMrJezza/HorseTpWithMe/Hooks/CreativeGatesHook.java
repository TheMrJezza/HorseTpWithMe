package au.TheMrJezza.HorseTpWithMe.Hooks;

import org.bukkit.Location;

import com.massivecraft.creativegates.EngineMain;
import com.massivecraft.creativegates.entity.UConfColls;
import com.massivecraft.massivecore.MassiveCore;

public class CreativeGatesHook {
	public static boolean isWaterGate(Location loc) {
		if (!UConfColls.get().getForWorld(loc.getWorld().getName()).get(MassiveCore.INSTANCE).isUsingWater())
			return false;
		return !EngineMain.isGateNearby(loc.getBlock());
	}
}
