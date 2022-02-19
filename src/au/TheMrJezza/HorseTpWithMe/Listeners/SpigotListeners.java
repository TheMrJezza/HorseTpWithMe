package au.TheMrJezza.HorseTpWithMe.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import au.TheMrJezza.HorseTpWithMe.Events.PlayerDismountEvent;
import au.TheMrJezza.HorseTpWithMe.Events.PlayerMountEvent;

public class SpigotListeners implements Listener {

	private PluginManager pm = Bukkit.getPluginManager();

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityMount(EntityMountEvent evt) {
		if (evt.getEntity() instanceof Player) {
			pm.callEvent(new PlayerMountEvent(evt.getMount(), (Player) evt.getEntity()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDismount(EntityDismountEvent evt) {
		if (evt.getEntity() instanceof Player) {
			pm.callEvent(new PlayerDismountEvent(evt.getDismounted(), (Player) evt.getEntity()));
		}
	}
}
