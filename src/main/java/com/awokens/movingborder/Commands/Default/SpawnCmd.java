package com.awokens.movingborder.Commands.Default;

import com.awokens.movingborder.Manager.CombatTagManager;
import com.awokens.movingborder.Manager.Utils;
import com.awokens.movingborder.MovingBorder;
import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

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
