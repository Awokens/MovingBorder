package com.awokens.movingborder.Commands.Default;

import com.awokens.movingborder.Manager.CombatTagManager;
import com.awokens.movingborder.MovingBorder;
import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.codehaus.plexus.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;

public class HomeCmd implements Listener {

    private final MovingBorder plugin;
    public HomeCmd(MovingBorder plugin) {
        this.plugin = plugin;

        new CommandAPICommand("home")
                .executesPlayer((player, args) -> {

                    Location home = getHome(player);

                    if (home == null) { // no home set
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<red>You do not have a home set"
                        ));
                        return;
                    }

                    int[] countdown = new int[]{5};

                    Location initialPos = player.getLocation();

                    new BukkitRunnable() {
                        @Override
                        public void run() {

                            if (CombatTagManager.hasCombat(player)) {
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

                            if (countdown[0] <= 0) {
                                player.teleportAsync(home).thenRun(() -> {
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
                                            "<red>" + countdown[0]
                                    ), Component.text(""))
                            );

                            countdown[0] -= 1;
                        }
                    }.runTaskTimer(plugin, 0L, 20L);

                }).register();

        new CommandAPICommand("sethome")
                .executesPlayer((player, args) -> {

                    Location location = player.getLocation().toCenterLocation();

                    setHome(player, location);

                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<newline><green>Saved your new home location here<newline>"
                    ));

                }).register();
    }

    private void setHome(Player player, Location location) {
        User user = plugin.getLuckPermsProvider()
                .getPlayerAdapter(Player.class).getUser(player);
        MetaNode node = MetaNode.builder("home", serialize(location)).build();

        user.data().clear(NodeType.META.predicate(mn ->
                mn.getMetaKey().equalsIgnoreCase("home")));
        user.data().add(node);

        plugin.getLuckPermsProvider().getUserManager().saveUser(user);

    }

    private Location getHome(Player player) {
        CachedMetaData metaData = plugin.getLuckPermsProvider()
                .getPlayerAdapter(Player.class).getMetaData(player);

        String encoded = metaData.getMetaValue("home");

        if (encoded == null) return null;

        return deserialize(encoded);

    }

    private String serialize(Location location) {

        String encoded = "";
        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);
            os.writeObject(location);
            os.flush();
            byte[] serializedObject = io.toByteArray();
            encoded = new String(Base64.encodeBase64(serializedObject));
        } catch (IOException e) {

            e.printStackTrace();
        }
        return encoded;
    }

    private Location deserialize(String encoded) {
        Location location = null;
        try {
            byte[] serializedObject = Base64.decodeBase64(encoded.getBytes());
            ByteArrayInputStream io = new ByteArrayInputStream(serializedObject);
            BukkitObjectInputStream is = new BukkitObjectInputStream(io);
            location = (Location) is.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return location;
    }

}
