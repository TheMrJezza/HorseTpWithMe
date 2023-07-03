package com.jbouchier.horsetpwithme;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CmdHorseDebug implements CommandExecutor {

    @Override
    public boolean onCommand(
            @NotNull CommandSender cs, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args
    ) {
        final var isDebugOn = !DataState.getInstance().isDebugOn();
        DataState.getInstance().setDebugOn(isDebugOn);
        cs.sendMessage("§7[§cHTpWM§7] §6Debugging " + (isDebugOn ? "Enabled!" : "Disabled!"));
        return true;
    }
}