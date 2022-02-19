package au.TheMrJezza.HorseTpWithMe.Helpers;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import au.TheMrJezza.HorseTpWithMe.Main;

public class HelperBase {
	private static boolean protocolLib = false, ridables = false, griefPrevention = false, worldGuard = false,
			vault = false;

	public HelperBase() {
		protocolLib = checkPlugin("ProtocolLib");
		if (Main.getMainInstance().isSpigot() || protocolLib) {
			ridables = checkPlugin("Ridables");
		}
		griefPrevention = checkPlugin("GriefPrevention");
		worldGuard = checkPlugin("WorldGuard");
		vault = checkPlugin("Vault");
	}

	private boolean checkPlugin(String pluginName) {
		Plugin plug = Bukkit.getPluginManager().getPlugin(pluginName);
		return plug != null && plug.isEnabled();
	}

	public static class ProtocolLib {
		public static void setupPacketListener() {
			if (protocolLib)
				ProtocolLibHelper.tryProtocol();
		}
	}

	public static class Ridables {
		public static String getRidablePerm(Entity entity) {
			if (ridables) {
				return RidablesHelper.getRideablePerm(entity);
			}
			return null;
		}

		public static void registerRidablePerms() {
			if (!ridables)
				return;
			Set<String> output = RidablesHelper.getAllRidables();
			if (output.isEmpty())
				return;
			String perm = "horsetpwithme.ridables.";
			for (String string : output) {
				Permission permission = new Permission(perm + string, PermissionDefault.FALSE);
				permission.addParent("horsetpwithme.ridables.*", true);
				Bukkit.getPluginManager().addPermission(permission);
				Main.getMainInstance().getLogger().info("Registered Permission: " + perm + string);
			}
		}
	}
}