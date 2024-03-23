package com.awokens.movingborder;

import com.awokens.movingborder.Commands.Admin.BorderCmd;
import com.awokens.movingborder.Commands.Default.EnderChestCmd;
import com.awokens.movingborder.Commands.Default.TagsCmd;
import com.awokens.movingborder.Listeners.Player.Damage;
import com.awokens.movingborder.Listeners.Player.Join;
import com.awokens.movingborder.Listeners.Player.Quit;
import com.awokens.movingborder.Listeners.Player.Chat;
import com.awokens.movingborder.Manager.BorderController;
import com.awokens.movingborder.Manager.TagManager;
import com.samjakob.spigui.SpiGUI;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;

public class MovingBorder extends JavaPlugin {

    private static Plugin plugin;
    private static BorderController WorldController;
    private static BorderController NetherController;

    private static TagManager tagManager;

    private static SpiGUI spiGUI;

    public static BorderController getWorldController() { return WorldController; }
    public static BorderController getNetherController() { return NetherController; }
    public static Plugin getPlugin() {
        return plugin;
    }

    public static SpiGUI GUIManager() { return spiGUI; }

    public static TagManager getTagManager() { return tagManager; }

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).shouldHookPaperReload(true));
        // register commands here

        new BorderCmd();
        new EnderChestCmd();
        new TagsCmd();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        plugin = this;
        tagManager = new TagManager(this);
        spiGUI = new SpiGUI(this);

        CommandAPI.onEnable();

        //Location loc = player.getWorld().locateNearestStructure(player.getLocation(), StructureType.NETHER_FORTRESS, 100, false);



        WorldController = new BorderController(this)
                .setWorld(getServer().getWorld("world"))
                .setBorderSize(100)
                .setBorderBuffer(5)
                .setBorderDamage(1)
                .setEntityType(EntityType.PIG)
                .setCustomName(MiniMessage.miniMessage().deserialize(
                        "<b><dark_aqua>World</dark_aqua><aqua> Border</aqua></b>"
                ))
                .start();

        NetherController = new BorderController(this)
                .setWorld(getServer().getWorld("world_nether"))
                .setBorderSize(50)
                .setBorderBuffer(10)
                .setBorderDamage(1)
                .setEntityType(EntityType.STRIDER)
                .setCustomName(MiniMessage.miniMessage().deserialize(
                        "<b><color:#ff3936>Nether</color> <red>Border</red></b>"
                ))
                .start();

        boolean result = registerListeners(this, List.of(
                new Chat(),
                new Quit(),
                new Join(),
                new Damage()
        ));

        if (!result) {
            getLogger().log(Level.SEVERE, "Failed to load events");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().log(Level.INFO, "Successfully loaded events");
        getLogger().log(Level.INFO, "Started World Controller tasks");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        CommandAPI.onDisable();

        getNetherController().getEntity().remove();
        getWorldController().getEntity().remove();

        getNetherController().getControllerTask().cancel();
        getWorldController().getControllerTask().cancel();
        getLogger().log(Level.INFO, "Cancelled World Controller tasks");

    }

    private boolean registerListeners(Plugin plugin, List<Listener> listeners) {
        for (Listener listener : listeners) {
            try {
                plugin.getServer().getPluginManager().registerEvents(listener, plugin);
            } catch (NullPointerException e) {
                return false;
            }
        }
        return true;
    }
}
