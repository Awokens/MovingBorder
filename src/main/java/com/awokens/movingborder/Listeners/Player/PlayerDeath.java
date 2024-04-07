package com.awokens.movingborder.Listeners.Player;

import com.awokens.movingborder.Manager.Utils;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerDeath implements Listener {

    @EventHandler
    public void respawn(PlayerRespawnEvent event) {
        event.getPlayer().addPotionEffect(
                new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,
                        20*3,
                        3,
                        true,
                        true)
        );


        if (!event.getRespawnLocation().getWorld().getName()
                .equalsIgnoreCase("world")) return;

        Location randomLocation = Utils.randomRespawn();

        if (randomLocation == null) return;

        event.setRespawnLocation(randomLocation);
    }
}
