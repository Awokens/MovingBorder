package com.awokens.movingborder.Listeners.Player;

import com.awokens.movingborder.MovingBorder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.Random;

public class Join implements Listener {

    @EventHandler
    public void joinRTP(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        if (!MovingBorder.getWorldController().getWorld().getName().equalsIgnoreCase(player.getWorld().getName()) &&
                !MovingBorder.getNetherController().getWorld().getName().equalsIgnoreCase(player.getWorld().getName())) return;

        player.setInvulnerable(true);

        World world = MovingBorder.getWorldController().getWorld();

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

        randomLocation = randomLocation.toHighestLocation().add(0, 1, 0);

        player.teleportAsync(randomLocation).thenRun(new BukkitRunnable() {
            @Override
            public void run() {
                player.setInvulnerable(false);
            }
        });
    }

    @EventHandler
    public void welcome(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        final Component header = MiniMessage.miniMessage().deserialize(
                "<newline><white><b>MOVING BORDER</b></white><newline>"
        );
        final Component footer = MiniMessage.miniMessage().deserialize(
                "<newline><white>leap.minehut.gg</white><newline>"
        );

        player.sendPlayerListHeader(header);
        player.sendPlayerListFooter(footer);
        player.playerListName(Component.text(player.getName()));

        String welcome_message;
        if (player.hasPlayedBefore()) {
            welcome_message = "<color:#44e971><b>WELCOME BACK";
        } else {
            welcome_message = "<color:#44e971><b>WELCOME";
        }

        long letterDelay = 20L;

        String[] letters = player.getName().split("");

        StringBuilder subtitle = new StringBuilder();

        for (String letter : letters) {
            subtitle.append(letter);
            Title formatter = Title.title(
                    MiniMessage.miniMessage().deserialize(welcome_message),
                    Component.text(subtitle.toString()),
                    Title.Times.times(
                            Duration.ZERO,
                            Duration.ofSeconds(2),
                            Duration.ZERO
                    )
            );

            new BukkitRunnable() {
                @Override
                public void run() {
                    player.showTitle(formatter);
                    player.playSound(player, Sound.BLOCK_DEEPSLATE_TILES_STEP, 1.0F, 1.0F);
                }
            }.runTaskLater(MovingBorder.getPlugin(), letterDelay);
            letterDelay += 2L;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                player.playSound(player, Sound.BLOCK_STONE_PLACE, 1F, 1F);
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 1F);
            }
        }.runTaskLater(MovingBorder.getPlugin(), letterDelay);

        letterDelay += 17L;

        new BukkitRunnable() {
            @Override
            public void run() {
                Title officialTitle = Title.title(
                        MiniMessage.miniMessage().deserialize("<bold>MOVING BORDER"),
                        Component.text("leap.minehut.gg"),
                        Title.Times.times(
                                Duration.ZERO,
                                Duration.ofSeconds(1),
                                Duration.ofSeconds(1)
                        )
                );
                player.showTitle(officialTitle);
                player.playSound(player, Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1F, 1F);
            }
        }.runTaskLater(MovingBorder.getPlugin(), letterDelay);

        letterDelay += 1L;

        new BukkitRunnable() {
            @Override
            public void run() {
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 0.2F, 1F);
                player.playSound(player, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 0.2F , 1F);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_IMITATE_ENDER_DRAGON, 0.5F, 1F);

                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        "<newline><b>MOVING BORDER</b><newline><newline>"
//                                + "→ To view all commands <green>/commands</green><newline>"
//                                + "→ To toggle specifics <green>/toggle <type></green><newline><newline>"
                                + "→ This server is underdevelopment<newline>"
                                + "→ But core features are present.<newline>"
                                + "<newline>Haven't join our Discord yet?<newline>"
                                + "→ <color:#308aff><click:open_url:'https://discord.gg/q3BRbWqHgx'>Click this message to join today</click></color><newline>"
                ));


            }
        }.runTaskLater(MovingBorder.getPlugin(), letterDelay);
    }
}
