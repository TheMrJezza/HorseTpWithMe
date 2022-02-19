package au.TheMrJezza.HorseTpWithMe.Events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class PlayerMountEvent extends CustomEntityEvent implements Cancellable {

	private boolean cancelled = false;

	public PlayerMountEvent(Entity vehicle, Player player) {
		super(vehicle, player);
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}
}