package au.TheMrJezza.HorseTpWithMe;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Llama;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import au.TheMrJezza.HorseTpWithMe.Helpers.HelperBase;
import au.TheMrJezza.HorseTpWithMe.Helpers.HelperBase.Ridables;
import au.TheMrJezza.HorseTpWithMe.Listeners.CoreEventListeners;

public class Main extends JavaPlugin {

	private static Main instance;

	private PluginManager pm = getServer().getPluginManager();
	private boolean isSpigot;

	public void onEnable() {
		instance = this;
		setup();
		getLogger().info("\033[32;1mHorseTpWithMe v" + this.getDescription().getVersion()
				+ " for Bukkit 1.11+ is Enabled and working!\033[0;m");
	}

	public void onDisable() {
		instance = null;
	}

	private void setup() {
		checkIfSpigot();
		new HelperBase();
		setupWorldPermissions();
		Ridables.registerRidablePerms();
		pm.registerEvents(new CoreEventListeners(), this);
		new MetricsLite(this);
	}

	private void setupWorldPermissions() {
		for (World world : Bukkit.getWorlds()) {
			String name = world.getName().toLowerCase();
			pm.addPermission(new Permission("horsetpwithme.disabledworld." + name, PermissionDefault.FALSE));
			pm.addPermission(new Permission("horsetpwithme.emptychestsfrom." + name, PermissionDefault.FALSE));
			pm.addPermission(new Permission("horsetpwithme.emptycheststo." + name, PermissionDefault.FALSE));
		}
	}

	public static Main getMainInstance() {
		return instance;
	}

	public boolean isSpigot() {
		return isSpigot;
	}

	private void checkIfSpigot() {
		try {
			Class.forName("org.spigotmc.SpigotConfig", false, ClassLoader.getSystemClassLoader());
			isSpigot = true;
		} catch (ClassNotFoundException e) {
			isSpigot = false;
		}
	}
	
	public List<Llama> getCaravanContents(List<Llama> llamas) {
		List<Llama> result = new ArrayList<Llama>();
		for (Llama living : llamas) {
			Llama original = (Llama) living;
			result.add(original);
			for (Entity e : original.getNearbyEntities(10, 10, 10)) {
				if (!(e instanceof Llama)) continue;
				Llama llama = (Llama) e;
				if (llama.getTarget() == null) continue;
				if (llama.getTarget().equals(original)) {
					result.add(llama);
					original = llama;
				}
			}
		}
		return result;
	}
}