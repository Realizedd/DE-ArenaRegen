package me.realized.de.arenaregen.nms.v1_18_R2;

import me.realized.de.arenaregen.nms.NMS;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.Chunk;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

public class NMSHandler implements NMS {

    @Override
    public void sendChunkUpdate(final Player player, final org.bukkit.Chunk chunk) {
        final Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
        ((CraftPlayer) player).getHandle().b.a(new ClientboundLevelChunkWithLightPacket(nmsChunk, nmsChunk.q.l_(), null, null, true));
    }

    @Override
    public void setBlockFast(final org.bukkit.World world, final int x, final int y, final int z, final int data, final Material material) {
        final BlockPosition position = new BlockPosition(x, y, z);
        final Chunk chunk = ((CraftChunk) world.getChunkAt(x >> 4, z >> 4)).getHandle();
        final net.minecraft.world.level.block.Block block = CraftMagicNumbers.getBlock(material);
        final IBlockData blockData = block.n();
        chunk.a(position, blockData, true);
    }

    @Override
    public void updateLighting(final org.bukkit.World world, final int x, final int y, final int z) {
        final BlockPosition position = new BlockPosition(x, y, z);
        final World nmsWorld = ((CraftWorld) world).getHandle();
        nmsWorld.K().n().a(position);
    }
}
