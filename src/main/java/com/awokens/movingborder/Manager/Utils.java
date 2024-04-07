package com.awokens.movingborder.Manager;

import com.awokens.movingborder.MovingBorder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

public class Utils {

    public static Location randomRespawn() {

        World world = Bukkit.getWorld("world");

        assert world != null; // only possible if the world was delete or some bs
        WorldBorder border = world.getWorldBorder();

        int size = (int) Math.floor(border.getSize());
        int radius = size / 2;

        Location center = border.getCenter().clone();

        Location cornerA = center.clone().subtract(radius, 0, radius);
        Location cornerB = center.clone().add(radius, 0, radius);

        Random random = new Random();

        int minX = (int) cornerA.getX(); // Minimum number
        int maxX = (int) cornerB.getX(); // Maximum number

        int minZ = (int) cornerA.getZ();
        int maxZ = (int) cornerB.getZ();

        int ranX = random.nextInt(maxX - minX + 1) + minX;
        int ranZ = random.nextInt(maxZ - minZ + 1) + minZ;

        Location randomLocation = new Location(world, ranX, 0, ranZ);

        return randomLocation.toHighestLocation().add(0, 1, 0);
    }

    /**
     *
     * @param plugin the instance
     * @param player the player we're teleporting to the destination
     * @param destination the destination the player teleports to
     * @param countdown the countdown measure in seconds
     */
    public static void fancyTeleportAsync(MovingBorder plugin, Player player, Location destination, int countdown) {


        int[] atomicCooldown = new int[] { countdown };
        Location initialPos = player.getLocation().clone();

        String session_ID = UUID.randomUUID().toString();

        // initialize the teleport's session instance ID
        // incase another runs, we'll cancel this currently one to
        // prevent weird flickering, etc.
        MetadataValue value = new FixedMetadataValue(plugin, session_ID);
        player.setMetadata("session_id", value);
        new BukkitRunnable() {
            @Override
            public void run() {

                if (CombatTagManager.hasCombat(player)) {
                    this.cancel();
                    return;
                }

                if (!player.hasMetadata("session_id")) {

                    this.cancel();
                    return;
                }

                String current_ID = player.getMetadata("session_id").get(0).asString();

                if (!current_ID.equalsIgnoreCase(session_ID)) {
                    this.cancel();
                    return;
                }

                if (initialPos.distance(player.getLocation()) > 1) {
                    player.showTitle(Title.title(
                            MiniMessage.miniMessage().deserialize(
                                    "<red>You moved too much."
                            ),
                            Component.text("")
                    ));
                    this.cancel();
                    return;
                }

                if (atomicCooldown[0] <= 0) {
                    player.teleportAsync(destination).thenRun(() -> {
                        player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                    });
                    player.showTitle(
                            Title.title(
                                    MiniMessage.miniMessage().deserialize(
                                            "<green>Zoom!"
                                    ),
                                    Component.text(""),
                                    Title.Times.times(
                                            Duration.ZERO,
                                            Duration.ofSeconds(1),
                                            Duration.ofMillis(500)
                                    )
                            ));
                    this.cancel();
                    return;
                }

                player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0F, 1.3F);
                player.showTitle(
                        Title.title(MiniMessage.miniMessage().deserialize(
                                "<red>" + atomicCooldown[0]
                        ), Component.text(""))
                );

                atomicCooldown[0] -= 1;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
}
