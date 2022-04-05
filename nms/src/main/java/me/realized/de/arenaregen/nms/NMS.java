package me.realized.de.arenaregen.nms;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface NMS {

    void sendChunkUpdate(final Player player, final Chunk chunk);

    void setBlockFast(final Block bukkitBlock, final Material material, final int data);

    void updateLighting(final Block bukkitBlock);

}
