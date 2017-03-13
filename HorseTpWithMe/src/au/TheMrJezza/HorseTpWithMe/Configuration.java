package au.TheMrJezza.HorseTpWithMe;

import java.util.List;

import org.bukkit.ChatColor;

public class Configuration {

	private static List<String> disabledWorlds;

	private static boolean requireSaddle;
	private static boolean usePermissions;
	private static boolean clearChests;

	private static String blockedWorld;
	private static String noPerm;

	public static void loadConfig() {
		Main.getInstance().reloadConfig();
		requireSaddle = Main.getInstance().getConfig().getBoolean("RequireSaddle");
		usePermissions = Main.getInstance().getConfig().getBoolean("UsePermissions");
		disabledWorlds = Main.getInstance().getConfig().getStringList("DisabledWorlds");
		clearChests = Main.getInstance().getConfig().getBoolean("ClearChests");
		noPerm = ChatColor.translateAlternateColorCodes('&',
				Main.getInstance().getConfig().getString("NoPermissionMessage"));
		blockedWorld = ChatColor.translateAlternateColorCodes('&',
				Main.getInstance().getConfig().getString("BlockedWorldMessage"));
	}

	public static boolean isBlocked(String worldName) {
		return disabledWorlds.contains(worldName.trim());
	}

	public static boolean requireSaddle() {
		return requireSaddle;
	}

	public static boolean usingPermission() {
		return usePermissions;
	}

	public static boolean clearChests() {
		return clearChests;
	}

	public static String blockedWorldMessage() {
		return blockedWorld;
	}

	public static String noPermMessage() {
		return noPerm;
	}
}
