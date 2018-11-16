package au.TheMrJezza.HorseTpWithMe.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.PluginManager;

import au.TheMrJezza.HorseTpWithMe.Events.PlayerDismountEvent;
import au.TheMrJezza.HorseTpWithMe.Events.PlayerMountEvent;

public class CraftBukkitListeners implements Listener {
	
	private PluginManager pm = Bukkit.getPluginManager();

	@EventHandler
	public void onVehicleExit(VehicleExitEvent evt) {
		if (evt.getExited() instanceof Player) {
			pm.callEvent(new PlayerDismountEvent(evt.getVehicle(), (Player) evt.getExited()));
		}
	}

	@EventHandler
	public void onVehicleEnter(VehicleEnterEvent evt) {
		if (evt.getEntered() instanceof Player) {
			pm.callEvent(new PlayerMountEvent(evt.getVehicle(), (Player) evt.getEntered()));
		}
	}
}