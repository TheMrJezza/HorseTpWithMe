package au.TheMrJezza.HorseTpWithMe;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class HorseEconomy {
	private static File file = null;
	private static YamlConfiguration economy = null;

	private static boolean useEconomy;
	private static double fee;
	private static Economy econ = null;
	private static String success;

	public static double freeTeleportDistance() {
		return 20;
	}

	public static File getFile() {
		return file;
	}
	
	public static void reload() {
		file = new File(Main.getInstance().getDataFolder(), "Economy.yml");
		if (!file.exists())
			Main.getInstance().saveResource("Economy.yml", true);
		economy = YamlConfiguration.loadConfiguration(file);
		useEconomy = economy.getBoolean("UseEconomy");
		fee = economy.getDouble("TeleportFee");
		success = ChatColor.translateAlternateColorCodes('&', economy.getString("Message"));
		if (fee <= 0)
			useEconomy = false;
		if (useEconomy)
			useEconomy = setupEconomy();
	}

	private static boolean setupEconomy() {
		if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	public static boolean chargePlayer(Entity entity, Player player) {
		if (!useEconomy)
			return true;
		if (entity.getWorld() != player.getWorld() || player.getLocation().distance(entity.getLocation()) >= 10) {
			EconomyResponse r = econ.withdrawPlayer(player, fee);
			if (r.transactionSuccess()) {
				player.sendMessage(success.replace("{FEE}", r.amount + "").replace("{BALANCE}", r.balance + "")
						.replace("{ANIMAL}",
								StringUtils.capitalize(entity.getType().name().toLowerCase().replace("_", " "))));
				return true;
			}
			player.sendMessage("§4" + r.errorMessage);
			return false;
		}
		return true;
	}
}
