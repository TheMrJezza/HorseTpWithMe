package au.TheMrJezza.HorseTpWithMe.Helpers;

import org.bukkit.Bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import au.TheMrJezza.HorseTpWithMe.Main;
import au.TheMrJezza.HorseTpWithMe.Events.PlayerDismountEvent;
import au.TheMrJezza.HorseTpWithMe.Events.PlayerMountEvent;

public class ProtocolLibHelper {
	public static void tryProtocol() {
		ProtocolLibrary.getProtocolManager().addPacketListener(
				new PacketAdapter(Main.getMainInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.MOUNT) {
					@Override
					public void onPacketSending(PacketEvent event) {
						if (event.getPlayer().isInsideVehicle()) {
							Bukkit.getPluginManager()
									.callEvent(new PlayerMountEvent(
											event.getPacket().getEntityModifier(event.getPlayer().getWorld()).read(0),
											event.getPlayer()));
						} else {
							Bukkit.getPluginManager()
									.callEvent(new PlayerDismountEvent(
											event.getPacket().getEntityModifier(event.getPlayer().getWorld()).read(0),
											event.getPlayer()));
						}
					}
				});
	}
}