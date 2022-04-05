package me.realized.de.arenaregen.nms.v1_18_R1;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

import me.realized.de.arenaregen.nms.NMS;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.World;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.block.state.IBlockData;

public class NMSHandler implements NMS {

    @Override
    public void sendChunkUpdate(final Player player, final org.bukkit.Chunk chunk) {
        final Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
        ((CraftPlayer) player).getHandle().b.a(new ClientboundLevelChunkWithLightPacket(nmsChunk, nmsChunk.q.l_(), null, null, true));
    }

    @Override
    public void setBlockFast(final Block bukkitBlock, final Material material, final int data) {
        final int x = bukkitBlock.getX(), y = bukkitBlock.getY(), z = bukkitBlock.getZ();
        final BlockPosition position = new BlockPosition(x, y, z);
        final Chunk chunk = ((CraftChunk) bukkitBlock.getChunk()).getHandle();
        final net.minecraft.world.level.block.Block block = CraftMagicNumbers.getBlock(material);
        final IBlockData blockData = block.n();
        chunk.a(position, blockData, true);
    }

    @Override
    public void updateLighting(Block bukkitBlock) {
        final int x = bukkitBlock.getX(), y = bukkitBlock.getY(), z = bukkitBlock.getZ();
        final BlockPosition position = new BlockPosition(x, y, z);
        final World world = ((CraftWorld) bukkitBlock.getWorld()).getHandle();
        world.L().m().a(position);
    }
}
