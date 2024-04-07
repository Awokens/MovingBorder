package com.awokens.movingborder.Manager;

import de.tr7zw.changeme.nbtapi.NBTEntity;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class BorderController {


    private int cooldown_penalty;
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

        for (Entity mob : getWorld().getEntities()) {
            if (mob.name().contains(getCustomName())){
                mob.remove();
            }
        }

        Location safeLocation = getSafeBorderCenter();

        // spawn entity
        LivingEntity entity = (LivingEntity) getWorld().spawnEntity(
                safeLocation, getEntityType(), CreatureSpawnEvent.SpawnReason.CUSTOM);

        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        boots.addEnchantment(Enchantment.FROST_WALKER, 2);
        ItemMeta meta = boots.getItemMeta();
        meta.setUnbreakable(true);

        boots.setItemMeta(meta);

        if (entity.getEquipment() != null) {
            entity.getEquipment().setBoots(boots);
        }

        entity.getChunk().load(true);
        // set entity info
        entity.customName(getCustomName());

        entity.setPersistent(true);
        entity.setPortalCooldown(10 ^ 60);
        entity.setVisualFire(false);
        entity.setGlowing(true);
        entity.setCustomNameVisible(true);
        entity.getEquipment().setBootsDropChance(0);
        entity.addPotionEffect(
                new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,
                        PotionEffect.INFINITE_DURATION,
                        5,
                        false,
                        false
                ));

        NBTEntity nbt = new NBTEntity(entity);
        nbt.setBoolean("Saddle", true);

        this.entity = entity; // set entity instance to new entity

        // set border info
        WorldBorder border = getWorld().getWorldBorder();;
        border.setSize(getBorderSize());
        border.setDamageAmount(getBorderDamage());
        border.setDamageBuffer(getBorderBuffer());


        positionTracker = entity.getLocation();

        // clear previous force loaded chunks

        for (Chunk forceLoadedChunk : getWorld().getForceLoadedChunks()) {
            if (forceLoadedChunk == positionTracker.getChunk()) {
                getWorld().getForceLoadedChunks().remove(positionTracker.getChunk());
            }
        }

//        this.cooldown_penalty = 20 * 5;
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
                if (getWorld().getPlayers().isEmpty()) {
                    getEntity().getLocation().getChunk().setForceLoaded(true);
                    return;
                }

                if (!getEntity().getWorld().getName().equalsIgnoreCase(getWorld().getName())) {
                    getEntity().teleport(getSafeBorderCenter());
                }

                // checks for the controller mob
                if (getEntity() == null || getEntity().getType() != getEntityType()) {
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

//                if (getEntity().getTicksLived() > (20 * 60 * 5)) {
//                    getEntity().setTicksLived(1);
//                    getEntity().teleport(getSafeBorderCenter());
//                }

                double distance = positionTracker.distance(getEntity().getLocation());

                if (cooldown_penalty > 0) {
                    cooldown_penalty -= 1;
                    return; // on cooldown due to border moving too faster
                }

                if (distance > 1.2) {
                    positionTracker = getEntity().getLocation();
                    cooldown_penalty = 20;
                    return; // skip updating because border is moving too fast
                }



                positionTracker = getEntity().getLocation();

                // set the world border position to the controller mob's current position
                getWorld().getWorldBorder().setCenter(getEntity().getLocation().clone());

            }
        }.runTaskTimer(getPlugin(), 1L, 1L);
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
        Block closestSafeBlock = null;
        double closestDistance = Double.MAX_VALUE;

        Location center = getWorld().getWorldBorder().getCenter();

        int maxHeight = getWorld().getMaxHeight();
        int minHeight = getWorld().getMinHeight();



        if (getWorld().getLogicalHeight() == 256) {
            maxHeight = 120; // Lower limit for Nether
        }

        for (int y = minHeight; y <= maxHeight; y++) {
            Block currentBlock = getWorld().getBlockAt(center.getBlockX(), y, center.getBlockZ());

            if (isSafeBlock(currentBlock)) {
                double distance = Math.abs(y - 64);

                if (distance < closestDistance) {
                    closestSafeBlock = currentBlock;
                    closestDistance = distance;
                }
            }
        }

        if (closestSafeBlock == null) {
            return center.set(center.x(), (maxHeight > 120 ? 32 : 64), center.z()).toCenterLocation();
        }
        return closestSafeBlock.getLocation().add(0, 2, 1).toCenterLocation();
    }

    private boolean isSafeBlock(Block block) {
        return block.getType().isSolid()
                && !block.getRelative(0, 1, 0).isSolid();
    }



}
