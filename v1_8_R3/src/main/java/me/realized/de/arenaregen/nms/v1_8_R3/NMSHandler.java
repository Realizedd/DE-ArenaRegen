package me.realized.de.arenaregen.nms.v1_8_R3;

import me.realized.de.arenaregen.nms.NMS;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.EnumSkyBlock;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

public class NMSHandler implements NMS {

    @Override
    public void sendChunkUpdate(final Player player, final org.bukkit.Chunk chunk) {
        ((CraftPlayer) player).getHandle().chunkCoordIntPairQueue.add(new ChunkCoordIntPair(chunk.getX(), chunk.getZ()));
    }

    @Override
    public void setBlockFast(final org.bukkit.World world, final int x, final int y, final int z, final int data, final Material material) {
        final BlockPosition position = new BlockPosition(x, y, z);
        final Chunk chunk = ((CraftChunk) world.getChunkAt(x >> 4, z >> 4)).getHandle();
        final net.minecraft.server.v1_8_R3.Block block = CraftMagicNumbers.getBlock(material);
        final IBlockData blockData = block.fromLegacyData(data);
        chunk.a(position, blockData);
    }

    @Override
    public void updateLighting(final org.bukkit.World world, final int x, final int y, final int z) {
        final BlockPosition position = new BlockPosition(x, y, z);
        final World nmsWorld = ((CraftWorld) world).getHandle();
        nmsWorld.c(EnumSkyBlock.BLOCK, position);
    }
}
