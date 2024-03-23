package com.awokens.movingborder.Listeners.Player;

import com.awokens.movingborder.Manager.CombatTagManager;
import com.awokens.movingborder.MovingBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class Damage implements Listener {

    @EventHandler
    public void damage(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        if (victim.getUniqueId().compareTo(attacker.getUniqueId()) == 0) return; // same player

        new CombatTagManager(MovingBorder.getPlugin(), victim);
        new CombatTagManager(MovingBorder.getPlugin(), attacker);
    }
}
