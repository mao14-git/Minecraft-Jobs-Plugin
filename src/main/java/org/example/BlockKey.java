package org.example;

import org.bukkit.Location;
import java.util.Objects;

public class BlockKey {
    private final String world;
    private final int x, y, z;

    public BlockKey(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static BlockKey fromLocation(Location loc) {
        return new BlockKey(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof BlockKey)) return false;
        BlockKey blockKey = (BlockKey) o;
        return x == blockKey.x && y == blockKey.y && z == blockKey.z;
    }

    @Override
    public int hashCode(){
        return Objects.hash(world, x, y, z);
    }

    @Override
    public String toString(){
        return world + ";" + x + ";" + y + ";" + z;
    }

    public static BlockKey fromString(String s){
        String[] parts = s.split(";");
        if(parts.length != 4) return null;
        try{
            String world = parts[0];
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            return new BlockKey(world, x, y, z);
        } catch(NumberFormatException e){
            return null;
        }
    }
}
