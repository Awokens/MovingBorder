package com.awokens.movingborder.Commands.Default;

import com.awokens.movingborder.Manager.TagManager;
import dev.jorel.commandapi.CommandAPICommand;

public class TagsCmd {

    public TagsCmd() {
        new CommandAPICommand("tags")
                .executesPlayer((player, args) -> {
                    player.openInventory(new TagManager.TagInventory(player).getTagInventory());
                }).register();
    }
}
