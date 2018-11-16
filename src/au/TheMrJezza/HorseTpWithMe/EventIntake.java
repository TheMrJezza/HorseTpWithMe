package au.TheMrJezza.HorseTpWithMe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

import au.TheMrJezza.HorseTpWithMe.Events.PlayerDismountEvent;
import au.TheMrJezza.HorseTpWithMe.Events.PlayerMountEvent;
import au.TheMrJezza.HorseTpWithMe.Events.VehicleTeleportEvent;
import au.TheMrJezza.HorseTpWithMe.Helpers.HelperBase.Ridables;

public class EventIntake {

	private static Main instance = Main.getMainInstance();

	private boolean isSneaking = false;
	private List<Event> savedEvents = new ArrayList<>();
	private Player player = null;
	private static Map<UUID, EventIntake> intakes = new HashMap<>();
	private List<Llama> allLlamas = new ArrayList<>();

	public static void add(Player player, Event evt, List<Llama> list) {
		EventIntake intake;
		if (intakes.containsKey(player.getUniqueId())) {
			intake = intakes.get(player.getUniqueId());
		} else {
			intake = new EventIntake();
			new BukkitRunnable() {
				@Override
				public void run() {
					intake.process();
					intake.kill();
				}
			}.runTaskLater(instance, 1L);
		}
		if (intake.allLlamas.isEmpty()) {
			intake.allLlamas = list;
		}
		if (player.isSneaking()) {
			intake.isSneaking = true;
		}
		if (intake.player == null) {
			intake.player = player;
		}
		intake.savedEvents.add(evt);
		intakes.put(player.getUniqueId(), intake);
	}

	protected void kill() {
		intakes.remove(player.getUniqueId());
	}

	private void process() {
		TeleportCause cause = TeleportCause.UNKNOWN;
		Location from = null, to = null;
		Entity vehicle = null;
		String commandMessage = null;
		for (Event evt : savedEvents) {
			if (evt instanceof PlayerMountEvent) {
				return;
			}
			if (evt instanceof PlayerDismountEvent) {
				PlayerDismountEvent temp = (PlayerDismountEvent) evt;
				if (vehicle == null) {
					vehicle = temp.getVehicle();
				}
				continue;
			}
			if (evt instanceof PlayerCommandPreprocessEvent) {
				commandMessage = ((PlayerCommandPreprocessEvent) evt).getMessage();
			}
			if (evt instanceof PlayerTeleportEvent) {
				PlayerTeleportEvent temp = (PlayerTeleportEvent) evt;
				if (!temp.getFrom().equals(temp.getTo()) && to == null) {
					from = temp.getFrom();
					to = temp.getTo();
				}
				if (temp.getCause() != TeleportCause.UNKNOWN) {
					cause = temp.getCause();
				}
				continue;
			}
		}

		if (isSneaking || from == null || to == null) {
			return;
		}

		World fromWorld = from.getWorld(), toWorld = to.getWorld();

		if (vehicle == null ) {
			return;
		}
		
		vehicle = validate(vehicle);
		
		if (!vehicle.isValid() || !player.isValid()) {
			return;
		}

		String permission = getFinalPermission(vehicle);

		if (permission == null || !player.hasPermission(permission)) {
			return;
		}

		if (vehicle instanceof Tameable) {
			if (player.hasPermission("horsetpwithme.requiretamed")) {
				Tameable tame = (Tameable) vehicle;
				if (!tame.isTamed()) {
					return;
				}
			}
		}

		if (vehicle instanceof AbstractHorse) {
			if (player.hasPermission("horsetpwithme.requiresaddle")) {
				AbstractHorse aHorse = (AbstractHorse) vehicle;
				if (aHorse.getInventory().getSaddle() == null)
					return;
			}
		}

		// This looks out of place being here, but I promise it's not.
		long dud = 5l;
		boolean emptyChests = false;
		if (!fromWorld.equals(toWorld)) {
			if (player.hasPermission("horsetpwithme.denycrossworld")) {
				return;
			}
			String clearFrom = "horsetpwithme.emptychestsfrom." + fromWorld.getName().toLowerCase();
			String clearTo = "horsetpwithme.emptycheststo." + toWorld.getName().toLowerCase();
			emptyChests = player.hasPermission(clearFrom) || player.hasPermission(clearTo);

		} else {
			if (cause == TeleportCause.UNKNOWN) {
				Location loc0 = from.clone(), loc1 = to.clone();
				if (commandMessage == null) {
					loc0.setY(0);
					loc1.setY(0);
					if (loc0.distance(loc1) < 0.5) {
						return;
					}
				}
			}
			double distance = from.distance(to);
			if (distance < 25) {
				dud = (long) Math.floor(Math.max(distance * 0.4, 4.0));
			}
		}
		if (cause == TeleportCause.CHORUS_FRUIT || cause == TeleportCause.SPECTATE) {
			return;
		}

		{// The Key to all HTpWM Extensions..
			VehicleTeleportEvent animalTpEvt = new VehicleTeleportEvent(from, to, vehicle, player, permission,
					emptyChests);
			instance.getServer().getPluginManager().callEvent(animalTpEvt);
			if (animalTpEvt.isCancelled()) {
				return;
			}
			emptyChests = animalTpEvt.clearEntityChests();
		}

		List<Entity> passengers = new ArrayList<>();
		for (Entity e : vehicle.getPassengers()) {
			if (e.equals(player))
				continue;
			if (!(e instanceof Player) || e.getScoreboardTags().contains("HTPWM_TELEPORT_AS_PASSENGER")) {
				teleportTo(e, to, emptyChests);
				passengers.add(e);
			}
		}

		if (vehicle instanceof Llama) {
			for (Llama ll : this.allLlamas) {
				teleportTo(ll, to, emptyChests);
				ll.teleport(to);
			}
		}
		
		teleportTo(vehicle, to, emptyChests);
		final Entity veh = vehicle;
		new BukkitRunnable() {
			public void run() {
				veh.addPassenger(player);
				for (Entity e : passengers) {
					if (!(e instanceof Player)) {
						veh.addPassenger(e);
					}
				}
			}
		}.runTaskLater(instance, dud);
	}

	private void teleportTo(Entity e, Location to, boolean emptyChests) {
		e = validate(e);
		e.setFallDistance(-2000000f);
		e.teleport(to);
		if (e instanceof ChestedHorse) {
			if (emptyChests) {
				ChestedHorse chested = (ChestedHorse) e;
				if (chested.isCarryingChest()) {
					chested.setCarryingChest(false);
					chested.setCarryingChest(true);
				}
			}
		}
	}

	public String getPermission(Entity vehicle) {
		switch (vehicle.getType()) {
		case DONKEY:
		case MULE:
			return "horsetpwithme.teleport.donkey";
		case HORSE:
		case ZOMBIE_HORSE:
		case SKELETON_HORSE:
			return "horsetpwithme.teleport.horse";
		case LLAMA:
			return "horsetpwithme.teleport.llama";
		case BOAT:
			return "horsetpwithme.teleport.boat";
		case MINECART:
			return "horsetpwithme.teleport.minecart";
		case PIG:
			return "horsetpwithme.teleport.pig";
		default:
			return null;
		}
	}

	public String getFinalPermission(Entity vehicle) {
		String result = Ridables.getRidablePerm(vehicle);
		if (result == null) {
			result = getPermission(vehicle);
		}
		return result;
	}

	private Entity validate(Entity entity) {
		Location loc = entity.getLocation();
		if (loc != null) {
			Chunk chunk = loc.getChunk();
			if (!chunk.isLoaded()) {
				chunk.load();
			}
			for (Entity e : chunk.getEntities()) {
				if (e.getUniqueId().equals(entity.getUniqueId())) {
					entity = e;
					break;
				}
			}
		}
		return entity;
	}
}