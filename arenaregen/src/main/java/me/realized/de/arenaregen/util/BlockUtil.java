package me.realized.de.arenaregen.util;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public final class BlockUtil {
    
    public static boolean isSurrounded(final Block block) {
        final Block east = block.getRelative(BlockFace.EAST);
        final Block west = block.getRelative(BlockFace.WEST);
        final Block south = block.getRelative(BlockFace.SOUTH);
        final Block north = block.getRelative(BlockFace.NORTH);
        final Block up = block.getRelative(BlockFace.UP);
        final Block down = block.getRelative(BlockFace.DOWN);
        return !east.getType().isTransparent()
            && !west.getType().isTransparent()
            && !up.getType().isTransparent()
            && !down.getType().isTransparent()
            && !south.getType().isTransparent()
            && !north.getType().isTransparent();
    }

    private BlockUtil() {}
    
}
