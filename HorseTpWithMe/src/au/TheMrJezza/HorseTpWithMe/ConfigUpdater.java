package au.TheMrJezza.HorseTpWithMe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigUpdater {
	
	public static void update() {
		File file = new File(Main.getInstance().getDataFolder(), "config.yml");
			try {
				FileConfiguration config = YamlConfiguration.loadConfiguration(file);
				String[] values = new String[6];
				values[0] = (String) getValue("RequireSaddle", config);
				values[1] = (String) getValue("UsePermissions", config);
				@SuppressWarnings("unchecked")
				List<String> list = (List<String>) getValue("DisabledWorlds", config);
				values[3] = (String) getValue("ClearChests", config);
				values[4] = (String) getValue("BlockedWorldMessage", config);
				values[5] = (String) getValue("NoPermissionMessage", config);

				String[] order = new String[6];
				order[0] = "RequireSaddle";
				order[1] = "UsePermissions";
				order[2] = "DisabledWorlds";
				order[3] = "ClearChests";
				order[4] = "BlockedWorldMessage";
				order[5] = "NoPermissionMessage";

				YamlAPI yaml = new YamlAPI(file);
				// Set the header
				header(yaml);

				for (int i = 0; i < 6; i++) {
					if (i == 2) {
						comment(order[i], yaml);
						yaml.addList(order[i], list);
						yaml.comment("");
						continue;
					}
					if (i >= 4) {
						comment(order[i], yaml);
						yaml.addString(order[i], values[i]);
						yaml.comment("");
						continue;
					}
					comment(order[i], yaml);
					yaml.addBoolean(order[i], Boolean.valueOf(values[i]));
					yaml.comment("");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		file = null;
	}

	private static Object getValue(String string, FileConfiguration config) {
		if (string.equals("RequireSaddle")) {
			if (config.isSet("RequireSaddle")) {
				return config.getString("RequireSaddle");
			}
			return "false";
		}
		if (string.equals("UsePermissions")) {
			if (config.isSet("UsePermissions")) {
				return config.getString("UsePermissions");
			}
			if (config.isSet("Use-A-Permission")) {
				return config.getString("Use-A-Permission");
			}
			return "false";
		}
		if (string.equals("DisabledWorlds")) {
			if (config.isSet("DisabledWorlds")) {
				return config.getStringList("DisabledWorlds");
			}
			if (config.isSet("Blacklisted-Worlds")) {
				return config.getStringList("Blacklisted-Worlds");
			}
			List<String> list = new ArrayList<>();
			list.add("ExaMpLE_WorLd");
			return list;
		}
		if (string.equals("ClearChests")) {
			if (config.isSet("ClearChests")) {
				return config.getString("ClearChests");
			}
			return "false";
		}
		if (string.equals("BlockedWorldMessage")) {
			if (config.isSet("BlockedWorldMessage")) {
				return config.getString("BlockedWorldMessage");
			}
			if (config.isSet("Blacklisted-World-Message")) {
				return config.getString("Blacklisted-World-Message");
			}
			return String.valueOf("&4This world is blocked!");
		}
		if (string.equals("NoPermissionMessage")) {
			if (config.isSet("NoPermissionMessage")) {
				return config.getString("NoPermissionMessage");
			}
			if (config.isSet("NoPermMessage")) {
				return config.getString("NoPermMessage");
			}
			return "&4You do not have permission to do that!";
		}
		return null;
	}

	private static String comment(String string, YamlAPI yaml) throws IOException {
		switch (string) {
		case "RequireSaddle":
			yaml.comment("Do Horses need a Saddle to teleport?");
			break;
		case "UsePermissions":
			yaml.comment("When this is true, players will need permission to teleport");
			yaml.comment("Horses/Donkeys/Mules/Pigs/Llamas");
			break;
		case "DisabledWorlds":
			yaml.comment("Teleportation Into or Through a world on this list will");
			yaml.comment("be disabled if the player doesn't have the permission 'horsetpwithme.worldbypass'");
			yaml.comment("All names are " + '"' + "cAse_SenStiVE" + '"');
			break;
		case "ClearChests":
			yaml.comment("If a Horse/Mule/Donkey with a chest teleports out of a");
			yaml.comment("Disabled World, should the chest be cleared?");
			break;
		case "BlockedWorldMessage":
			yaml.comment("When a player attempts to teleport into a Disabled World");
			yaml.comment("without permission, they will recieve this Message.");
			break;
		case "NoPermissionMessage":
			yaml.comment("This message will be sent to a player when they try to use");
			yaml.comment("the command /htpreload without permission.");
			break;
		case "Version":
			yaml.comment("This is the config version. DO NOT CHANGE THIS!!!");
			break;
		}
		return null;
	}

	private static void header(YamlAPI yaml) {
		try {
			yaml.comment("#########################################################################", false);
			yaml.comment("#                                                                       #");
			yaml.comment("# -------------------  HorseTpWithMe Configuration  ------------------- #");
			yaml.comment("#                                                                       #");
			yaml.comment("#########################################################################");
			yaml.comment("");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}