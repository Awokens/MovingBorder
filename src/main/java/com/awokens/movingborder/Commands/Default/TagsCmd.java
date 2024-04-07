package com.awokens.movingborder.Commands.Default;

import com.awokens.movingborder.Manager.TagManager;
import com.awokens.movingborder.MovingBorder;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;

public class TagsCmd {

    private final MovingBorder plugin;

    public TagsCmd(MovingBorder plugin) {

        this.plugin = plugin;

        new CommandAPICommand("tags")
                .executesPlayer((player, args) -> {
                    player.openInventory(new TagManager.TagInventory(plugin, player).getTagInventory());
                }).register();
    }
}
