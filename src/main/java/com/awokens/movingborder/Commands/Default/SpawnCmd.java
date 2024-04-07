package com.awokens.movingborder.Commands.Default;

import com.awokens.movingborder.Manager.CombatTagManager;
import com.awokens.movingborder.Manager.Utils;
import com.awokens.movingborder.MovingBorder;
import dev.jorel.commandapi.CommandAPICommand;

public class SpawnCmd {

    private final MovingBorder plugin;
    public SpawnCmd(MovingBorder plugin) {

        this.plugin = plugin;

        new CommandAPICommand("spawn")
                .executesPlayer((player, args) -> {
                    if (CombatTagManager.hasCombat(player)) {
                        CombatTagManager.inCombatMessage(player);
                        return;
                    }
                    Utils.fancyTeleportAsync(plugin, player, Utils.randomRespawn(), 5);
                }).register();
    }
}
