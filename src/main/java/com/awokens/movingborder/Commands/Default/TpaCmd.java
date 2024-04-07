package com.awokens.movingborder.Commands.Default;

import com.awokens.movingborder.Manager.CombatTagManager;
import com.awokens.movingborder.MovingBorder;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.PlayerArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class TpaCmd {

    // -> players
    // --> player requests
    private final MovingBorder plugin;
    public TpaCmd(MovingBorder plugin) {
        this.plugin = plugin;

        new CommandAPICommand("tpa")
                .withArguments(new PlayerArgument("target").replaceSuggestions(
                        ArgumentSuggestions.stringsAsync(info -> CompletableFuture.supplyAsync(() -> {
                            Collection<String> playerNames = new ArrayList<>();
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                playerNames.add(player.getName());
                            }
                            return playerNames.toArray(new String[0]);
                        }))
                ))
                .executesPlayer((player, args) -> {

                    if (!plugin.getPlayerTeleportRequests().containsKey(player)) {
                        plugin.getPlayerTeleportRequests().put(player, new ArrayList<>());
                    }

                    Player target = (Player) args.get(0);

                    if (target == null) { // invalid player?
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<red>Unable to request a teleport to " + args.get(0)
                        ));
                        return;
                    }

                    if (plugin.getPlayerTeleportRequests().getOrDefault(target, new ArrayList<>()).contains(player)) { // already sent request
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<red>You've already sent a teleport request to " + target.getName()
                        ));
                        return;
                    }

                    // add player to list of teleport requests for target

                    if (!plugin.getPlayerTeleportRequests().containsKey(target)) {
                        plugin.getPlayerTeleportRequests().put(target, new ArrayList<>());
                    }

                    plugin.getPlayerTeleportRequests().get(target).add(player);

                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<green>Sent teleported request to " + target.getName()
                    ));
                    target.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<green>" + player.getName() + " has sent a teleport request to you"
                    ));
                }).register();

        new CommandAPICommand("tpaccept")
                .withOptionalArguments(new PlayerArgument("target").replaceSuggestions(
                        ArgumentSuggestions.stringsAsync(info -> CompletableFuture.supplyAsync(() -> {
                            Collection<String> playerNames = new ArrayList<>();
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                playerNames.add(player.getName());
                            }
                            return playerNames.toArray(new String[0]);
                        }))
                ))
                .executesPlayer((player, args) -> {

                    if (!plugin.getPlayerTeleportRequests().containsKey(player)) { // list isn't initialized for player
                        plugin.getPlayerTeleportRequests().put(player, new ArrayList<>());
                    }

                    if (plugin.getPlayerTeleportRequests().get(player).isEmpty()) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<red>You have no queued teleport requests"
                        ));
                        return;
                    }

                    Player optionalTarget = null;

                    if (args.get("target") != null) { // invalid player?
                        optionalTarget = (Player) args.get("target");
                    }

                    Player target;
                    if (optionalTarget != null) {
                        if (!plugin.getPlayerTeleportRequests().get(player).contains(optionalTarget)) {
                            player.sendMessage(MiniMessage.miniMessage().deserialize(
                                    "<red>This player has not requested a teleport to you"
                            ));
                            return;
                        }
                        target = optionalTarget;
                    } else {
                        target = plugin.getPlayerTeleportRequests().get(player).get(0);
                    }


                    plugin.getPlayerTeleportRequests().get(player).remove(target);
                    Location initialPos = target.getLocation();
                    int[] countdown = new int[]{5};
                    Player finalTarget = target;

                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<green>Accepted " + target.getName() + "'s teleport request"
                    ));

                    target.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<green>" + player.getName() + " has accepted your teleport request"
                    ));
                    new BukkitRunnable() {
                        @Override
                        public void run() {

                            if (CombatTagManager.hasCombat(finalTarget)) {
                                this.cancel();
                                return;
                            }

                            if (initialPos.distance(finalTarget.getLocation()) > 1) {
                                finalTarget.showTitle(Title.title(
                                        MiniMessage.miniMessage().deserialize(
                                                "<red>You moved too much."
                                        ),
                                        Component.text("")
                                ));
                                this.cancel();
                                return;
                            }

                            if (countdown[0] <= 0) {
                                finalTarget.teleportAsync(player.getLocation()).thenRun(() -> {
                                    finalTarget.playSound(finalTarget, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                                });
                                finalTarget.showTitle(
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

                            finalTarget.playSound(finalTarget, Sound.UI_BUTTON_CLICK, 1.0F, 1.3F);
                            finalTarget.showTitle(
                                    Title.title(MiniMessage.miniMessage().deserialize(
                                            "<red>" + countdown[0]
                                    ), Component.text(""))
                            );

                            countdown[0] -= 1;
                        }
                    }.runTaskTimer(plugin, 0L, 20L);

                }).register();

    }
}
