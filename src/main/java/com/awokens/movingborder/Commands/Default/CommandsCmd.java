package com.awokens.movingborder.Commands.Default;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.RegisteredCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.HashMap;

public class CommandsCmd {

    public CommandsCmd() {

        new CommandAPICommand("commands")
                .withFullDescription("Provides a list of commands accessible to you")
                .executesPlayer((player, args) -> {

                    final Component title = MiniMessage.miniMessage().deserialize(
                            "<newline><yellow>Commands:<newline>");

                    player.sendMessage(title);


                    HashMap<String, RegisteredCommand> commands = new HashMap<>();

                    for (RegisteredCommand command : CommandAPI.getRegisteredCommands()) {

                        CommandPermission permission = command.permission();
                        if (permission != CommandPermission.NONE) continue;

                        if (!commands.containsKey(command.commandName())) {
                            commands.put(command.commandName(), command);
                        }
                    }

                    for (RegisteredCommand command : commands.values()) {


                        String description = command.fullDescription()
                                .orElse("No description provided");

                        String name = command.commandName();

                        final Component component = MiniMessage.miniMessage().deserialize(
                                "→ <hover:show_text:'"+ description + "'>/" + name +"</hover>"
                        );

                        player.sendMessage(component);
                    }

                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<newline><gray>Hover over text for command description<newline>"
                    ).decoration(TextDecoration.ITALIC, true));

                }).register();
    }
}
