package au.TheMrJezza.HorseTpWithMe;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class EventListeners implements Listener {

	private HashMap<Player, LivingEntity> map = new HashMap<Player, LivingEntity>();
	private HashSet<UUID> uuidSet = new HashSet<UUID>();
	private HashSet<UUID> crossWorld = new HashSet<UUID>();

	@EventHandler(priority = EventPriority.MONITOR)
	public void onVehicleExit(VehicleExitEvent evt) {
		Vehicle vehicle = (Vehicle) evt.getVehicle();
		if (!(evt.getExited() instanceof Player))
			return;
		Player player = (Player) evt.getExited();
		if (uuidSet.contains(player.getUniqueId())) {
			uuidSet.remove(player.getUniqueId());
			return;
		}
		if (player.isSneaking())
			return;
		if (!Compatibility.canTeleport(vehicle, player))
			return;
		map.put(player, (LivingEntity) vehicle);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCreatureSpawn(CreatureSpawnEvent evt) {
		if (crossWorld.contains(evt.getEntity().getUniqueId())) {
			evt.setCancelled(false);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onCreatureSpawnFinal(CreatureSpawnEvent evt) {
		if (crossWorld.contains(evt.getEntity().getUniqueId())) {
			crossWorld.remove(evt.getEntity().getUniqueId());
			evt.setCancelled(false);
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent evt) {
		if (evt.getEntity().isInsideVehicle()) {
			UUID uuid = evt.getEntity().getUniqueId();
			uuidSet.add(uuid);
			evt.getEntity().leaveVehicle();
		}
	}

	public void onPlayerLeave(PlayerQuitEvent evt) {
		if (uuidSet.contains(evt.getPlayer().getUniqueId()))
			uuidSet.remove(evt.getPlayer().getUniqueId());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerLeave(PlayerKickEvent evt) {
		if (uuidSet.contains(evt.getPlayer().getUniqueId()))
			uuidSet.remove(evt.getPlayer().getUniqueId());
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onAnimalTeleport(AnimalTeleportEvent evt) {
		String response = AreaBlockMethods.cancelEvent(evt.getDestination());
		if (response != null) {
			evt.setCancelled();
			evt.getRider().sendMessage(response);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent evt) {
		if (evt.getPlayer().isInsideVehicle()&&evt.getPlayer().getVehicle() instanceof LivingEntity) {
			crossWorld.add(evt.getPlayer().getVehicle().getUniqueId());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onVehicleEnter(VehicleEnterEvent evt) {
		if (evt.isCancelled() && evt.getEntered() instanceof Player) {
			UUID uuid = evt.getEntered().getUniqueId();
			uuidSet.add(uuid);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onPlayerTeleport(PlayerTeleportEvent evt) {
		final Player player = evt.getPlayer();
		if (map.containsKey(player)) {
			final LivingEntity entity = map.get(player);
			map.remove(player);
			new BukkitRunnable() {
				public void run() {
					Location loc = player.getLocation();
					if (Configuration.isBlocked(loc.getWorld().getName())) {
						if (!player.hasPermission("horsetpwithme.worldbypass")) {
							player.sendMessage("Animal Teleportation is Disabled for this world!");
							return;
						}
					}
					if (player.isInsideVehicle())
						return;
					AnimalTeleportEvent event = new AnimalTeleportEvent(entity, player);
					Bukkit.getPluginManager().callEvent(event);
					if (!event.isCancelled()) {
						if (Configuration.isBlocked(entity.getWorld().getName()))
							Compatibility.clearChest(entity);
						if (!loc.getChunk().isLoaded())
							loc.getChunk().load();
						entity.setFallDistance(-2000000f);
						loc.setY(loc.getY() + 0.5);
						if (HorseEconomy.chargePlayer(entity, player)) {
							if (loc.getWorld() != entity.getWorld())
								crossWorld.add(entity.getUniqueId());
							entity.teleport(loc);
						}
						new BukkitRunnable() {
							public void run() {
								Compatibility.setPassanger(entity, player);
							}
						}.runTaskLater(Main.getInstance(), 5L);
					}
				}
			}.runTaskLater(Main.getInstance(), 2L);
		}
	}
}