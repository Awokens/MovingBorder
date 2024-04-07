package com.awokens.movingborder.Listeners.Player;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class PlayerBlockBreak implements Listener {

    @EventHandler
    public void carrots(BlockBreakEvent event) {

        Block block = event.getBlock();


        if (block.getType() != Material.SHORT_GRASS) return;

        Random random = new Random();

        if (random.nextInt(100) <= 85) {
            return;
        }

        event.setDropItems(false);
        block.getLocation().getWorld().dropItem(block.getLocation().toBlockLocation(),
                new ItemStack(Material.CARROT, random.nextInt(3)));
    }

    @EventHandler
    public void bonemeal(BlockBreakEvent event) {


        Block block = event.getBlock();
        if (block.getType() != Material.GRASS_BLOCK) return;

        Random random = new Random();

        if (random.nextInt(100) <= 75) {
            return;
        }
        event.setDropItems(false);
        block.getLocation().getWorld().dropItemNaturally(block.getLocation().toBlockLocation(),
                new ItemStack(Material.BONE_MEAL, random.nextInt(1,3)));

    }

    @EventHandler
    public void apple(BlockBreakEvent event) {

        Block block = event.getBlock();

        if (!(block.getBlockData() instanceof Leaves)) return;

        Random random = new Random();

        if (random.nextInt(100) <= 92) {
            return;
        }

        event.setDropItems(false);
        block.getLocation().getWorld().dropItemNaturally(block.getLocation().toBlockLocation(),
                new ItemStack(Material.APPLE));

    }

}
