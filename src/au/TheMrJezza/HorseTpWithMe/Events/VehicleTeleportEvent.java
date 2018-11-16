package au.TheMrJezza.HorseTpWithMe.Events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class VehicleTeleportEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	private String permission = null;
	private Player player;
	private Entity vehicle;
	private Location from, to;
	private boolean clearEntityChests = false;

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return getHandlerList();
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public VehicleTeleportEvent(Location from, Location to, Entity vehicle, Player player, String permission, boolean clearChests) {
		this.from = from;
		this.to = to;
		this.player = player;
		this.vehicle = vehicle;
		this.permission = permission;
		clearEntityChests = clearChests;
	}

	public String getPermission() {
		return permission;
	}

	public Player getPlayer() {
		return player;
	}

	public Entity getVehicle() {
		return vehicle;
	}

	public Location getFrom() {
		return from;
	}

	public Location getTo() {
		return to;
	}

	public boolean clearEntityChests() {
		return clearEntityChests;
	}

	public void setClearEntityChests(boolean clearEntityChests) {
		this.clearEntityChests = clearEntityChests;
	}
}