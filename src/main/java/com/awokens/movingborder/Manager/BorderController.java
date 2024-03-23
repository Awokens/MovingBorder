package com.awokens.movingborder.Manager;

import de.tr7zw.changeme.nbtapi.NBTEntity;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Panda;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class BorderController {

    private Plugin plugin;

    private BukkitTask ControllerTask;

    private World world;

    private Entity entity;

    private Location positionTracker;

    private EntityType entityType;

    private Component customName;

    private int borderSize;

    private int borderDamage;

    private int borderBuffer;

    public BorderController(Plugin plugin) {
        this.plugin = plugin;
    }

    public BorderController start() {

        if (getControllerTask() != null && !getControllerTask().isCancelled()) getControllerTask().isCancelled();

        // remove previous controller mob if entity is set or alive
        if (getEntity() != null) getEntity().remove();

        Location safeLocation = getSafeBorderCenter();

        // spawn entity
        Entity entity = getWorld().spawnEntity(
                safeLocation, getEntityType(), CreatureSpawnEvent.SpawnReason.CUSTOM);
        entity.getChunk().load(true);
        // set entity info
        entity.customName(getCustomName());
        entity.setPersistent(true);
        entity.setInvulnerable(true);
        entity.setPortalCooldown(10 ^ 60);
        entity.setVisualFire(false);
        entity.setGlowing(true);
        entity.setCustomNameVisible(true);

        NBTEntity nbt = new NBTEntity(entity);
        nbt.setBoolean("Saddle", true);

        this.entity = entity; // set entity instance to new entity

        // set border info
        WorldBorder border = getWorld().getWorldBorder();;
        border.setSize(getBorderSize());
        border.setDamageAmount(getBorderDamage());
        border.setDamageBuffer(getBorderBuffer());


        positionTracker = entity.getLocation();
        /*
        run periodical event where the border is updated to the current position of the entity
         */
        this.ControllerTask = new BukkitRunnable() {
            @Override
            public void run() {

                // e.g. the task was restarted or stopped via command
                if (getControllerTask().isCancelled()) return;

                // In case no players are present in the controller mob's world,
                // like in the Nether, pausing the updates for the controller mob
                // prevents potential errors caused by the entity unloading and
                // appearing to not exist, which could lead to duplicate tasks.

                if (!getEntity().getWorld().getName().equalsIgnoreCase(getWorld().getName())) {
                    getEntity().teleport(getSafeBorderCenter());
                }

                if (getWorld().getPlayers().isEmpty()) {
                    return;
                }

                // checks for the controller mob
                if (getEntity() == null || !getEntity().getType().equals(getEntityType())) {
                    this.cancel();
                }
                if (getEntity().isDead()) {
                    this.cancel();
                }

                // if the task was cancelled internally, we will restart the task
                // this is because the task logically wasn't stopped via command
                // but by one of the 3 conditions above passed
                if (getControllerTask().isCancelled()) {
                    getEntity().remove();
                    start();
                    return;
                }

                if (getEntity().getTicksLived() > (20 * 60 * 5)) {
                    getEntity().setTicksLived(1);
                    getEntity().teleport(getSafeBorderCenter());
                }

                double distance = positionTracker.distance(entity.getLocation());

                if (distance > 15) {
                    positionTracker = entity.getLocation();
                    return; // skip updating because border is moving too fast
                }

                positionTracker = entity.getLocation();
                // set the world border position to the controller mob's current position
                getWorld().getWorldBorder().setCenter(getEntity().getLocation().clone());

            }
        }.runTaskTimer(getPlugin(), 20L, 20L);
        return this;
    }

    public BukkitTask getControllerTask() {
        return ControllerTask;
    }

    private Plugin getPlugin() {
        return plugin;
    }

    private int getBorderSize() {
        return borderSize;
    }

    public BorderController setBorderSize(int borderSize) {
        this.borderSize = borderSize;
        return this;
    }

    private int getBorderDamage() {
        return borderDamage;
    }

    public BorderController setBorderDamage(int borderDamage) {
        this.borderDamage = borderDamage;
        return this;
    }

    private int getBorderBuffer() {
        return borderBuffer;
    }

    public BorderController setBorderBuffer(int borderBuffer) {
        this.borderBuffer = borderBuffer;
        return this;
    }

    private EntityType getEntityType() {
        return entityType;
    }

    public BorderController setEntityType(EntityType entityType) {
        this.entityType = entityType;
        return this;
    }

    public World getWorld() {
        return world;
    }

    public BorderController setWorld(World world) {
        this.world = world;
        return this;
    }

    public Entity getEntity() {
        return entity;
    }

    private Component getCustomName() {
        return customName;
    }

    public BorderController setCustomName(Component customName) {
        this.customName = customName;
        return this;
    }

    public Location getSafeBorderCenter() {
        Location location = getWorld().getWorldBorder().getCenter().clone();

        int maxHeight = getWorld().getMaxHeight();
        int minHeight = getWorld().getMinHeight();
        if (getWorld().getLogicalHeight() == 256) { // cheat sheet to identify world as the nether
            maxHeight = 120; // few blocks lower to avoid the nether bedrock roof
        }

        Block safeBlock = null;

        for (int i = minHeight; i <= maxHeight; i++) {

            location.setY(i); // set y-level (from minHeight to maxHeight)
            Block block = location.getBlock();

            if (safeBlock != null && i > getWorld().getSeaLevel() - 1 && i < getWorld().getSeaLevel() + 25) {
                break;
            }

            if (block.getType().isSolid()) continue;
            if (block.getRelative(BlockFace.UP).getType().isSolid()) continue;

            safeBlock = block;
        }

        if (safeBlock == null) {
            // set to the sea/lava level of the world instead
            safeBlock = location.set(location.x(), (maxHeight > 120 ? 32 : 64), location.z()).getBlock();
        }
        return safeBlock.getLocation();
    }



}
