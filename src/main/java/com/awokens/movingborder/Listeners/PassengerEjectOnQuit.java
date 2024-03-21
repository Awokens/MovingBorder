package com.awokens.movingborder.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PassengerEjectOnQuit implements Listener {

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Entity vehicle = player.getVehicle();

        // dismount player to prevent logging with vehicle
        // e.g. controller mob
        if (vehicle != null) vehicle.eject();

    }

}
