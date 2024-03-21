package com.awokens.movingborder.Listeners;

import com.awokens.movingborder.Manager.TagManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerChat implements Listener {

    @EventHandler
    public void event(AsyncChatEvent event) {

        if (event.isCancelled()) return;;

        event.setCancelled(true);

        String message = TagManager.getTag(event.getPlayer())
                        + " <gray>" + event.getPlayer().getName()
                        + ": <white>" + event.signedMessage().message();

        final Component component = MiniMessage.miniMessage().deserialize(message.trim());

        Bukkit.broadcast(component);
    }
}
