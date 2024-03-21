package com.awokens.movingborder.Commands.Default;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Sound;

public class EnderChestCmd {
    public EnderChestCmd() {
        new CommandAPICommand("enderchest")
                .withAliases("ec", "echest")
                .executesPlayer((player, args) -> {

                    player.openInventory(player.getEnderChest());
                    player.playSound(
                            player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.5F, 1F
                    );

                }).register();
    }
}
