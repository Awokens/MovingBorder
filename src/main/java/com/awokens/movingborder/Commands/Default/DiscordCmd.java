package com.awokens.movingborder.Commands.Default;

import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class DiscordCmd {

    public DiscordCmd() {

        new CommandAPICommand("discord")
                .executesPlayer((player, args) -> {
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<newline><click:open_url:'https://discord.gg/q3BRbWqHgx'>Click to join our Discord</click><newline>"
                    ));
                }).register();


    }
}
