package com.jbouchier.horsetpwithme;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class CmdTapToggle implements CommandExecutor {

    @Override
    public boolean onCommand(
            @NotNull CommandSender cs, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args
    ) {
        final var target = MiscUtil.extractTarget(cs, args);
        if (target != null) {
            var val = MiscUtil.isTapDisabled(target);
            target.getPersistentDataContainer().set(MiscUtil.getTapKey(), PersistentDataType.BOOLEAN, val);

            target.sendMessage("§eT.A.P Status§7: " + (val ? "§aEnabled!" : "§cDisabled!"));
            if (!target.equals(cs)) {
                cs.sendMessage("§eT.A.P Status§7: %s §efor %s!".formatted(
                        val ? "§aEnabled" : "§cDisabled", target.getName()
                ));
            }
        }
        return true;
    }
}