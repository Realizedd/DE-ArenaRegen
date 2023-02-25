package me.realized.de.arenaregen.nms.v1_12_R1;

import me.realized.de.arenaregen.nms.NMS;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.EnumSkyBlock;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

public class NMSHandler implements NMS {

    @Override
    public void sendChunkUpdate(final Player player, final org.bukkit.Chunk chunk) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), 65535));
    }

    @Override
    public void setBlockFast(final org.bukkit.World world, final int x, final int y, final int z, final int data, final Material material) {
        final BlockPosition position = new BlockPosition(x, y, z);
        final Chunk chunk = ((CraftChunk) world.getChunkAt(x >> 4, z >> 4)).getHandle();
        final net.minecraft.server.v1_12_R1.Block block = CraftMagicNumbers.getBlock(material);
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
