package com.jbouchier.horsetpwithme;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public class HorseTpWithMe extends JavaPlugin {

    public final void onEnable() {
        PermissionState.buildPerms(this);
        new Listeners(this);
        setExec("HorseDebug", new CmdHorseDebug());
        setExec("Reseat", new CmdReseat(this));
        setExec("TeleportAsPassenger", new CmdTapToggle());
    }

    private void setExec(String cmd, CommandExecutor ce) {
        var c = getCommand(cmd); if (c != null) c.setExecutor(ce);
    }
}