package com.awokens.movingborder;

import com.awokens.movingborder.Commands.Admin.BorderCmd;
import com.awokens.movingborder.Commands.Default.EnderChestCmd;
import com.awokens.movingborder.Commands.Default.TagsCmd;
import com.awokens.movingborder.Listeners.PassengerEjectOnQuit;
import com.awokens.movingborder.Listeners.PlayerChat;
import com.awokens.movingborder.Manager.BorderController;
import com.awokens.movingborder.Manager.TagManager;
import com.samjakob.spigui.SpiGUI;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;

public class MovingBorder extends JavaPlugin {

    private Plugin plugin;
    private static BorderController WorldController;
    private static BorderController NetherController;

    private static TagManager tagManager;

    private static SpiGUI spiGUI;

    public static BorderController getWorldController() { return WorldController; }
    public static BorderController getNetherController() { return NetherController; }
    public Plugin getPlugin() {
        return this.plugin;
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

        this.plugin = this;
        this.tagManager = new TagManager(this);
        this.spiGUI = new SpiGUI(this);

        CommandAPI.onEnable();

        WorldController = new BorderController(this)
                .setWorld(getServer().getWorld("world"))
                .setBorderSize(100)
                .setBorderBuffer(5)
                .setBorderDamage(1)
                .setEntityType(EntityType.PIG)
                .setCustomName(Component.text("World Border"))
                .start();

        NetherController = new BorderController(this)
                .setWorld(getServer().getWorld("world_nether"))
                .setBorderSize(50)
                .setBorderBuffer(10)
                .setBorderDamage(1)
                .setEntityType(EntityType.STRIDER)
                .setCustomName(Component.text("Nether Border"))
                .start();

        boolean result = registerListeners(this, List.of(
                new PlayerChat(),
                new PassengerEjectOnQuit())
        );

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
