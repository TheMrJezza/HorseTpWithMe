package com.jbouchier.horsetpwithme;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

record TapToggleCommand(TeleportLogic logic) implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender cs, @NotNull Command cmd,
                             @NotNull String alias, @NotNull String[] args) {
        if (cs instanceof Player player) {
            logic.setTapStatus(player, logic.getTAPStatus(player));
        } else cs.sendMessage("§cThis command is for in-game players only!");
        return true;
    }
}
