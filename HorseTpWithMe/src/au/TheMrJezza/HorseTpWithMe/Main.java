package au.TheMrJezza.HorseTpWithMe;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	private static Main instance;
	public void onEnable() {
		instance = this;
		reload();
		getServer().getPluginManager().registerEvents(new EventListeners(), this);
	}

	public void onDisable() {
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
			String region = AreaBlockMethods.toggleRegion(player.getLocation());
			String claim = AreaBlock.toggleClaim(player.getLocation());

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
		reload();
		cs.sendMessage("§7[§2HorseTpWithMe§7: §aFiles Reloaded!§7]");
		return true;
	}
	
	private void reload() {
		HorseEconomy.reload();
		Compatibility.load();
		ConfigUpdater.update();
		Configuration.loadConfig();
		AreaBlock.reload();
	}
}
