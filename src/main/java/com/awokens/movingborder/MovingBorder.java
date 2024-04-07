package com.awokens.movingborder;

import com.awokens.movingborder.Commands.Admin.BorderCmd;
import com.awokens.movingborder.Commands.Default.*;
import com.awokens.movingborder.Listeners.Player.*;
import com.awokens.movingborder.Manager.BorderController;
import com.awokens.movingborder.Manager.TagManager;
import com.awokens.movingborder.Manager.WikiManager;
import com.samjakob.spigui.SpiGUI;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class MovingBorder extends JavaPlugin {

    private HashMap<Player, List<Player>> playerTeleportRequests;
    private HashMap<Player, List<Player>> playerBlockedTeleportRequests;
    private BorderController WorldController;
    private BorderController NetherController;

    private TagManager tagManager;

    private SpiGUI spiGUI;

    private LuckPerms luckPerms;

    private WikiManager wikiManager;

    public HashMap<Player, List<Player>> getPlayerTeleportRequests() {
        return this.playerTeleportRequests;
    }
    public HashMap<Player, List<Player>> getPlayerBlockedTeleportRequests() {
        return this.playerBlockedTeleportRequests;
    }
    public WikiManager getWikiManager() { return this.wikiManager; }

    public LuckPerms getLuckPermsProvider() {
        return this.luckPerms;
    }

    public BorderController getWorldController() { return WorldController; }
    public BorderController getNetherController() { return NetherController; }

    public SpiGUI GUIManager() { return spiGUI; }

    public TagManager getTagManager() { return tagManager; }


    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).shouldHookPaperReload(true));
        // register commands here

        new BorderCmd(this);
        new EnderChestCmd();
        new TagsCmd(this);
        new CommandsCmd();
        new DiscordCmd();
        new HomeCmd(this);
        new WikiCmd(this);
        new TpaCmd(this);
        new SpawnCmd(this);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        playerTeleportRequests = new HashMap<>();
        playerBlockedTeleportRequests = new HashMap<>();

        luckPerms = LuckPermsProvider.get();
        wikiManager = new WikiManager();
        tagManager = new TagManager(this);
        spiGUI = new SpiGUI(this);

        CommandAPI.onEnable();

        //Location loc = player.getWorld().locateNearestStructure(player.getLocation(), StructureType.NETHER_FORTRESS, 100, false);



        WorldController = new BorderController(this)
                .setWorld(getServer().getWorld("world"))
                .setBorderSize(100)
                .setBorderBuffer(64)
                .setBorderDamage(1)
                .setEntityType(EntityType.PIG)
                .setCustomName(MiniMessage.miniMessage().deserialize(
                        "<b><dark_aqua>World</dark_aqua><aqua> Border</aqua></b>"
                ))
                .start();

        NetherController = new BorderController(this)
                .setWorld(getServer().getWorld("world_nether"))
                .setBorderSize(50)
                .setBorderBuffer(128)
                .setBorderDamage(1)
                .setEntityType(EntityType.STRIDER)
                .setCustomName(MiniMessage.miniMessage().deserialize(
                        "<b><color:#ff3936>Nether</color> <red>Border</red></b>"
                ))
                .start();

        boolean result = registerListeners(this, List.of(
                new PlayerChat(),
                new PlayerQuit(),
                new PlayerJoin(this),
                new PlayerDamage(this),
                new PlayerEnderSignal(),
                new PlayerBlockBreak(),
                new PlayerDeath()
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
