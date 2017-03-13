package au.TheMrJezza.HorseTpWithMe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.massivecraft.creativegates.EngineMain;
import com.massivecraft.creativegates.entity.UConfColls;
import com.massivecraft.massivecore.MassiveCore;

public class Compatibility {

	private static String ver = Bukkit.getVersion();
	private static boolean old = (ver.contains("1.7") || ver.contains("1.8") || ver.contains("1.9")
			|| ver.contains("1.10"));
	private static boolean useCreativeGates = false;

	// Called once in the onEnable()
	public static void load() {
		Plugin cGates = Bukkit.getPluginManager().getPlugin("CreativeGates");
		useCreativeGates = (cGates != null && cGates.isEnabled());
	}

	public static boolean canTeleport(Entity entity, Player player) {
		if (!entity.isOnGround()) {
			// Creative Gates Support
			if (entity.getLocation().getBlock().getType() != Material.AIR) {
				if (useCreativeGates)
					return false;
				if (!UConfColls.get().getForWorld(entity.getWorld().getName()).get(MassiveCore.INSTANCE).isUsingWater())
					return false;
				if (!EngineMain.isGateNearby(entity.getLocation().getBlock()))
					return false;
			}
		}

		// Pigs
		if (entity instanceof Pig)
			return (player.hasPermission("horsetpwithme.pig") || !Configuration.usingPermission());

		// Game Version >= 1.11
		if (!old) {
			if (entity instanceof AbstractHorse) {
				AbstractHorse horse = (AbstractHorse) entity;
				if (!horse.isTamed())
					return false;
				if (player.hasPermission("horsetpwithme.horse") || !Configuration.usingPermission())
					return !(!((AbstractHorse) entity).getInventory().contains(new ItemStack(Material.SADDLE))
							&& Configuration.requireSaddle());
			}

			if (entity instanceof Llama)
				return (player.hasPermission("horsetpwithme.llama") || !Configuration.usingPermission());
			return false;
		}

		// Game Version < 1.11
		if (entity instanceof Horse) {
			Horse horse = (Horse) entity;
			if (!horse.isTamed())
				return false;
			if (player.hasPermission("horsetpwithme.horse") || !Configuration.usingPermission()) {
				return !(horse.getInventory().getSaddle() == null && Configuration.requireSaddle());
			}
		}
		return false;
	}

	public static void setPassanger(Entity entity, Entity player) {
		if (old) {
			entity.setPassenger(player);
			return;
		}
		entity.addPassenger(player);
	}

	public static void clearChest(Entity entity) {
		if (Configuration.clearChests())
			return;
		if (entity instanceof ChestedHorse) {
			ChestedHorse chested = (ChestedHorse) entity;
			if (chested.isCarryingChest()) {
				int exclude = 0;
				if (!old) {
					if (entity instanceof Llama)
						exclude = 1;
				}
				for (int i = 0; i < chested.getInventory().getSize(); i++) {
					if (i <= exclude)
						continue;
					ItemStack item = chested.getInventory().getItem(i);
					if (item == null)
						continue;
					item.setType(Material.AIR);
					item.setAmount(1);
				}
			}
		}
	}
}