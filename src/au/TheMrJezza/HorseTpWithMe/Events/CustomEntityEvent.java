package au.TheMrJezza.HorseTpWithMe.Events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomEntityEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private Player player;
	private Entity vehicle;

	public CustomEntityEvent(Entity vehicle, Player player) {
		this.player = player;
		this.vehicle = vehicle;
	}

	@Override
	public HandlerList getHandlers() {
		return getHandlerList();
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public Player getPlayer() {
		return player;
	}
	
	public Entity getVehicle() {
		return vehicle;
	}
}