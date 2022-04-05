package me.realized.de.arenaregen.nms.v1_17_R1;

import me.realized.de.arenaregen.nms.NMS;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutMapChunk;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

public class NMSHandler implements NMS {

    @Override
    public void sendChunkUpdate(final Player player, final org.bukkit.Chunk chunk) {
        ((CraftPlayer) player).getHandle().b.sendPacket(new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle()));
    }

    @Override
    public void setBlockFast(final Block bukkitBlock, final Material material, final int data) {
        final int x = bukkitBlock.getX(), y = bukkitBlock.getY(), z = bukkitBlock.getZ();
        final BlockPosition position = new BlockPosition(x, y, z);
        final Chunk chunk = ((CraftChunk) bukkitBlock.getChunk()).getHandle();
        final net.minecraft.world.level.block.Block block = CraftMagicNumbers.getBlock(material);
        final IBlockData blockData = block.getBlockData();
        chunk.setType(position, blockData, true);
    }

    @Override
    public void updateLighting(Block bukkitBlock) {
        final int x = bukkitBlock.getX(), y = bukkitBlock.getY(), z = bukkitBlock.getZ();
        final BlockPosition position = new BlockPosition(x, y, z);
        final World world = ((CraftWorld) bukkitBlock.getWorld()).getHandle();
        world.getChunkProvider().getLightEngine().a(position);
    }
}
