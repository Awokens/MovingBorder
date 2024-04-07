package com.awokens.movingborder.Manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;

import java.util.Random;

public class Utils {

    public static Location randomRespawn() {

        World world = Bukkit.getWorld("world");

        if (world == null) return null;
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
}
