package unused.com.jbouchier.horsetpwithme.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class PlayerCommand implements CommandExecutor {

    static String PLAYER_ONLY_MESSAGE = "Only in-game players can do that!";

    @Override
    public boolean onCommand(
            @NotNull CommandSender cs, @NotNull Command command, @NotNull String alias, @NotNull String[] args
    ) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(PLAYER_ONLY_MESSAGE);
        } else execute(((Player) cs), args);
        return false;
    }

    public abstract void execute(@NotNull Player player, @NotNull String[] args);
}