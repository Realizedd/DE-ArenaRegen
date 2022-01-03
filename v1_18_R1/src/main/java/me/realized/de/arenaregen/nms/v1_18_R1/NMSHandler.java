package me.realized.de.arenaregen.nms.v1_18_R1;

import me.realized.de.arenaregen.nms.NMS;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_18_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

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

        if (bukkitBlock.getType() == Material.AIR || isSurrounded(bukkitBlock)) {
            return;
        }

        final World world = ((CraftWorld) bukkitBlock.getWorld()).getHandle();
        world.L().m().a(position);
    }

    private boolean isSurrounded(final Block block) {
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
}
