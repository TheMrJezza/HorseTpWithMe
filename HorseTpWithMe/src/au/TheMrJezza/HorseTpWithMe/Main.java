package au.TheMrJezza.HorseTpWithMe;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	private static Main instance;
	
	public HTpWMAPI API;
	public void onEnable() {
		instance = this;
		API = new HTpWMAPI();
		getLogger().info("\033[32;1mHorseTpWithMe v" + this.getDescription().getVersion() + " is Enabled and working.\033[0;m");
	}

	public void onDisable() {
		API = null;
		instance = null;
	}
	
	public static Main getInstance() {
		return instance;
	}
	
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("toggleBlock")) {
			if (!(cs instanceof Player)) {
				cs.sendMessage("Only in-game players can use this command.");
				return true;
			}
			if (!cs.hasPermission("horsetpwithme.toggleBlock")) {
				cs.sendMessage(Configuration.noPermMessage());
				return true;
			}
			Player player = (Player) cs;
			String region = API.toggleRegion(player.getLocation());
			String claim = API.toggleClaim(player.getLocation());

			if (region == null && claim == null) {
				player.sendMessage("There are no Claims or Regions here.");
				return true;
			}

			if (region != null)
				player.sendMessage(region);
			if (claim != null)
				player.sendMessage(claim);
			return true;
		}
		if (!cs.hasPermission("horsetpwithme.reload")) {
			cs.sendMessage(Configuration.noPermMessage());
			return true;
		}
		API.reload();
		cs.sendMessage("§7[§2HorseTpWithMe§7: §aFiles Reloaded!§7]");
		return true;
	}
}