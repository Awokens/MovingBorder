package com.awokens.movingborder.Commands.Admin;

import com.awokens.movingborder.MovingBorder;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class BorderCmd {


    private final MovingBorder plugin;

    public BorderCmd(MovingBorder plugin) {

        this.plugin = plugin;

        List<Argument<?>> arguments = new ArrayList<>();
        arguments.add(new StringArgument("world").replaceSuggestions(ArgumentSuggestions.strings(
                "nether", "world"
        )));
        CommandAPICommand reload = new CommandAPICommand("reload")
                .withArguments(new StringArgument("world")
                        .replaceSuggestions(ArgumentSuggestions.strings("world", "nether")))
                .executesPlayer((player, args) -> {
                    String world = (String) args.get("world");

                    if (world == null) return;

                    switch (world) {
                        case "world" -> plugin.getWorldController().start();
                        case "nether" -> plugin.getNetherController().start();
                        default -> {
                            player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<red>There is no controller mob for this world"
                            ));
                            return;
                        }
                    }

                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            "Reload <gray>" + world + "</gray> controller mob"
                    ));

                });

        CommandAPICommand teleport = new CommandAPICommand("teleport")
                .withArguments(new StringArgument("world")
                        .replaceSuggestions(ArgumentSuggestions.strings("world", "nether")))
                .executesPlayer((player, args) -> {
                    String world = (String) args.get("world");

                    if (world == null) return;

                    switch (world) {
                        case "world" -> plugin.getWorldController().getEntity().teleport(player);
                        case "nether" -> plugin.getNetherController().getEntity().teleport(player);
                        default -> {
                            player.sendMessage(MiniMessage.miniMessage().deserialize(
                                    "<red>There is no controller mob for this world"
                            ));
                            return;
                        }
                    }

                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            "Teleported <gray>" + world + "</gray> to you"
                    ));

                });

        // /border <reload|teleport> <world|nether>
        new CommandAPICommand("border")
                .withPermission("movingborder.border")
                .withUsage("<red>/border reload <world or nether></red>",
                        "<red>/border teleport <world or nether></red>")
                .withSubcommand(reload)
                .withSubcommand(teleport)
                .register();
    }
}
