package me.realized.de.arenaregen.nms;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public interface NMS {

    void sendChunkUpdate(final Player player, final Chunk chunk);

    void setBlockFast(final World world, final int x, final int y, final int z, final int data, final Material material);

    void updateLighting(final World world, final int x, final int y, final int z);

}
