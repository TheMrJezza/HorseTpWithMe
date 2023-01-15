package com.jbouchier.horsetpwithme.cmd;

import com.jbouchier.horsetpwithme.HorseTpWithMe;
import com.jbouchier.horsetpwithme.Language;
import com.jbouchier.horsetpwithme.Language.MessageKey;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import static com.jbouchier.horsetpwithme.Language.MessageKey.TELEPORT_AS_PASSENGER_DISABLED;
import static com.jbouchier.horsetpwithme.Language.MessageKey.TELEPORT_AS_PASSENGER_ENABLED;
import static com.jbouchier.horsetpwithme.util.GeneralUtil.hasFlag;
import static com.jbouchier.horsetpwithme.util.GeneralUtil.setFlag;

public class TeleportAsPassenger extends PlayerCommand {

    private static final NamespacedKey TAP_TOGGLE_KEY =
            new NamespacedKey(JavaPlugin.getProvidingPlugin(HorseTpWithMe.class), "tap_status");

    @Override
    public void execute(
            @NotNull Player player, @NotNull String[] args
    ) {
        // figure out the current T.A.P. status and assign the key
        MessageKey key = hasFlag(player, TAP_TOGGLE_KEY) ?
                TELEPORT_AS_PASSENGER_DISABLED : TELEPORT_AS_PASSENGER_ENABLED;

        setFlag(player, TAP_TOGGLE_KEY, key == TELEPORT_AS_PASSENGER_ENABLED);
        String message = Language.getMessage(key);
        if (message != null) player.sendMessage(message);
    }
}