package com.awokens.movingborder.Manager;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class CombatTagManager {

    private int counter = 5; // measured in seconds
    private static final String COMBAT_KEY = "combat_timer";
    private final Player player;
    private final Plugin plugin;

    public CombatTagManager(Plugin plugin, Player player) {
        this.player = player;
        this.plugin = plugin;
        run();
    }

    private void run() {

        String session_id = createSessionId();
        new BukkitRunnable() {
            @Override
            public void run() {

                if (!session_id.equalsIgnoreCase(getSessionId())) {
                    // new combat in session
                    this.cancel();
                }
                if (counter < 1L) {
                    // combat over
                    getPlayer().sendActionBar(MiniMessage.miniMessage().deserialize(
                            "<green>You are safe now</green>"
                    ));
                    getPlayer().removeMetadata(COMBAT_KEY, plugin);
                    this.cancel();
                    return;
                }

                if (!getPlayer().isOnline() || !getPlayer().isConnected()) {
                    getPlayer().setHealth(0);
                    this.cancel();
                    return;
                }

                if (getPlayer().getWorld().getWorldBorder().isInside(player.getLocation())) {
                    getPlayer().sendActionBar(MiniMessage.miniMessage().deserialize(
                            "<red>Combat tag over in <white>" + counter
                    ));
                    counter--;
                } else {
                    getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(
                            "<red>You are outside the border (Combat Tag Frozen)"
                    ));
                }
            }
        }.runTaskTimer(getPlugin(), 0L, 20L);

    }

    public static boolean hasCombat(Player player) {
        return player.hasMetadata(COMBAT_KEY);
    }

    public static void inCombatMessage(Player player) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(
                "<red>You cannot quit or execute specific commands whilst in combat"
        ));
    }


    private String getSessionId() {
        if (!getPlayer().hasMetadata(COMBAT_KEY)) {
            return UUID.randomUUID().toString();
        }
        return player.getMetadata(COMBAT_KEY).get(0).asString();
    }

    private String createSessionId() {

        String session_id = UUID.randomUUID().toString();

        player.setMetadata(COMBAT_KEY, new FixedMetadataValue(getPlugin(), session_id));

        return session_id;
    }

    private Player getPlayer() {
        return this.player;
    }

    private Plugin getPlugin() {
        return this.plugin;
    }



}
