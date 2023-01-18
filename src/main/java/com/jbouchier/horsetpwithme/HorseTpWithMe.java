package com.jbouchier.horsetpwithme;

import com.jbouchier.horsetpwithme.cmd.MainCommand;
import com.jbouchier.horsetpwithme.cmd.Reseat;
import com.jbouchier.horsetpwithme.cmd.TeleportAsPassenger;
import com.jbouchier.horsetpwithme.util.DetailLogger;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import static com.jbouchier.horsetpwithme.util.GeneralUtil.runTask;
import static com.jbouchier.horsetpwithme.util.GeneralUtil.setExecutor;

public class HorseTpWithMe extends JavaPlugin {

    public void onEnable() {
        new Metrics(this, 3502);
        getServer().getPluginManager().registerEvents(EventListeners.INSTANCE, this);
        runTask(PermissionHandler::build, 0);
        DetailLogger.forceLog("§aThe plugin is enabled and working!");

        setExecutor("TeleportAsPassenger", new TeleportAsPassenger());
        setExecutor("Reseat", new Reseat());
        setExecutor("HorseTpWithMe", new MainCommand());
        Language.reload();
        UpdateManager.grabLatestAndNotify();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender cs, Command cmd, @NotNull String alias, String[] args) {
        if (cmd.getName().equals("PermCheck")) {
            if (args.length > 0) cs.sendMessage(args[0] + ": " + cs.hasPermission(args[0]));
            return true;
        }
        return true;
    }
}