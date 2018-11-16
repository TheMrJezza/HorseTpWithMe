package au.TheMrJezza.HorseTpWithMe.Listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Llama;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.PluginManager;

import au.TheMrJezza.HorseTpWithMe.EventIntake;
import au.TheMrJezza.HorseTpWithMe.Main;
import au.TheMrJezza.HorseTpWithMe.Events.PlayerDismountEvent;
import au.TheMrJezza.HorseTpWithMe.Events.PlayerMountEvent;
import au.TheMrJezza.HorseTpWithMe.Helpers.HelperBase.ProtocolLib;

public class CoreEventListeners implements Listener {

	private PluginManager pm = Bukkit.getPluginManager();

	public CoreEventListeners() {
		if (Main.getMainInstance().isSpigot()) {
			pm.registerEvents(new SpigotListeners(), Main.getMainInstance());
			return;
		}
		ProtocolLib.setupPacketListener();
		pm.registerEvents(new CraftBukkitListeners(), Main.getMainInstance());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerTeleport(PlayerTeleportEvent evt) {
		EventIntake.add(evt.getPlayer(), evt, null);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDoCommand(PlayerCommandPreprocessEvent evt) {
		EventIntake.add(evt.getPlayer(), evt, null);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityMount(PlayerMountEvent evt) {
		EventIntake.add(evt.getPlayer(), evt, null);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDismount(PlayerDismountEvent evt) {
		List<Llama> llamas = new ArrayList<Llama>();
		for (Entity e : evt.getPlayer().getNearbyEntities(10, 10, 10)) {
			if (!(e instanceof Llama)) {
				continue;
			}
			Llama llama = (Llama) e;
			if (llama.getLeashHolder() == null || !llama.getLeashHolder().equals(evt.getPlayer())) {
				continue;
			}
			llamas.add(llama);
		}
		EventIntake.add(evt.getPlayer(), evt, Main.getMainInstance().getCaravanContents(llamas));
	}
}