package au.TheMrJezza.HorseTpWithMe;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import au.TheMrJezza.HorseTpWithMe.Hooks.CreativeGatesHook;
import au.TheMrJezza.HorseTpWithMe.Hooks.GriefPreventionHook;
import au.TheMrJezza.HorseTpWithMe.Hooks.IGatesHook;
import au.TheMrJezza.HorseTpWithMe.Hooks.WorldGuardHook;

public class HTpWMAPI {
	private static boolean griefPrevention = false;
	private static boolean worldGuard = false;
	private static boolean creativeGates = false;
	private static boolean iGates = false;
	//private static boolean fBasics = false;
	private PluginManager pm = Bukkit.getPluginManager();

	public HTpWMAPI() {
		pm.registerEvents(new EventListeners(), Main.getInstance());
		reload();
	}
	
	public void reload() {
		griefPrevention = checkPlugin("GriefPrevention");
		worldGuard = checkPlugin("WorldGuard");
		creativeGates = checkPlugin("CreativeGates");
		iGates = checkPlugin("iGates");
		//fBasics = checkPlugin("fBasics");
		
		HorseEconomy.reload();
		ConfigUpdater.update();
		Configuration.loadConfig();
		AreaBlock.reload();
	}

	private boolean checkPlugin(String name) {
		Plugin plugin = pm.getPlugin(name);
		return plugin != null && plugin.isEnabled();
	}
	
	public String cancelEvent(Location loc) {
		if (griefPrevention) return GriefPreventionHook.cancelEventGriefPrevention(loc);
		if (worldGuard) return WorldGuardHook.cancelEventWorldGuard(loc);
		return null;
	}

	public String toggleRegion(Location loc) {
		if (worldGuard) return WorldGuardHook.toggleRegion(loc);
		return null;
	}
	
	public String toggleClaim(Location loc) {
		if (griefPrevention) return GriefPreventionHook.toggleClaim(loc);
		return null;
	}

	public boolean isWaterGate(Location loc) {
		if (creativeGates) return CreativeGatesHook.isWaterGate(loc);
		if (iGates) return IGatesHook.isWaterGate(loc);
		return false;
	}
}