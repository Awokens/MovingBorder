package com.awokens.movingborder.Listeners.Player;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerEnderSignal implements Listener {

    @EventHandler
    public void eye(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        if (event.getClickedBlock() != null) return;

        ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (heldItem.isEmpty() || heldItem.getType() != Material.ENDER_EYE) return;

        event.setCancelled(true);

        heldItem.subtract(1);

        Location stronghold = new Location(player.getWorld(), -3852, 0, -2604);

        Location signalLocation = player.getEyeLocation();
        EnderSignal eye = player.getWorld().spawn(signalLocation, EnderSignal.class);

        eye.setTargetLocation(stronghold);

        eye.getWorld().playSound(eye, Sound.ENTITY_ENDER_EYE_LAUNCH, 1.0F, 1.0F);

    }
}
