package me.realized.de.arenaregen.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class NMSUtil {

    private static Method GET_HANDLE;
    private static Method FROM_LEGACY_DATA;
    private static Method GET_BLOCK_DATA;
    private static Method GET_BLOCK;
    private static Method SET_BLOCK_7;
    private static Method SET_BLOCK_12;
    private static Method SET_BLOCK;
    private static Constructor<?> BLOCK_POS_CONSTRUCTOR;

    static {
        try {
//            PLAYER_GET_HANDLE = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("entity.CraftPlayer"), "getHandle");
//            CHUNK_QUEUE = ReflectionUtil.getField(ReflectionUtil.getNMSClass("EntityPlayer"), "chunkCoordIntPairQueue");
//            CHUNK_PAIR_CONSTRUCTOR = ReflectionUtil.getConstructor(ReflectionUtil.getNMSClass("ChunkCoordIntPair"), Integer.TYPE, Integer.TYPE);
            GET_HANDLE = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("CraftChunk"), "getHandle");
            final Class<?> BLOCK = ReflectionUtil.getNMSClass("Block");
            FROM_LEGACY_DATA = ReflectionUtil.getMethod(BLOCK, "fromLegacyData", Integer.TYPE);
            GET_BLOCK_DATA = ReflectionUtil.getMethod(BLOCK, "getBlockData");
            final Class<?> BLOCK_POS = ReflectionUtil.getNMSClass("BlockPosition");
            BLOCK_POS_CONSTRUCTOR = ReflectionUtil.getConstructor(BLOCK_POS, Double.TYPE, Double.TYPE, Double.TYPE);
            GET_BLOCK = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("util.CraftMagicNumbers"), "getBlock", Material.class);
            final Class<?> CHUNK = ReflectionUtil.getNMSClass("Chunk");
            SET_BLOCK_7 = ReflectionUtil.getMethod(CHUNK, "a", Integer.TYPE, Integer.TYPE, Integer.TYPE, BLOCK, Integer.TYPE);
            final Class<?> BLOCK_DATA = ReflectionUtil.getNMSClass("IBlockData");
            SET_BLOCK_12 = ReflectionUtil.getMethod(CHUNK, "a", BLOCK_POS, BLOCK_DATA);
            SET_BLOCK = ReflectionUtil.getMethod(CHUNK, "a", BLOCK_POS, BLOCK_DATA, Boolean.TYPE);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    private NMSUtil() {}

    public static boolean setBlockFast(final Block block, final Material material, final int data) {
        return setBlockFast(block.getChunk(), block.getX(), block.getY(), block.getZ(), material, data);
    }

    public static boolean setBlockFast(final Chunk chunk, final int x, final int y, final int z, final Material material, final int data) {
        try {
            final Object chunkHandle = GET_HANDLE.invoke(chunk);
            Object nmsBlock = GET_BLOCK.invoke(null, material);

            if (CompatUtil.isPre1_8()) {
                SET_BLOCK_7.invoke(chunkHandle, x & 0x0f, y, z & 0x0f, nmsBlock, data);
            } else if (CompatUtil.isPre1_13()) {
                nmsBlock = FROM_LEGACY_DATA.invoke(nmsBlock, data);
                SET_BLOCK_12.invoke(chunkHandle, BLOCK_POS_CONSTRUCTOR.newInstance(x & 0xF, y, z & 0xF), nmsBlock);
            } else {
                SET_BLOCK.invoke(chunkHandle, BLOCK_POS_CONSTRUCTOR.newInstance(x, y, z), GET_BLOCK_DATA.invoke(nmsBlock), true);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public static void update(final Player player, final Collection<Chunk> chunks) {
        chunks.forEach(chunk -> player.getWorld().refreshChunk(chunk.getX(), chunk.getZ()));
//
//        try {
//            final Object handle = PLAYER_GET_HANDLE.invoke(player);
//            final List queue = (List) CHUNK_QUEUE.get(handle);
//
//            for (final Chunk chunk : chunks) {
//                queue.add(CHUNK_PAIR_CONSTRUCTOR.newInstance(chunk.getX(), chunk.getZ()));
//            }
//        } catch (Throwable ex) {
//            ex.printStackTrace();
//        }
    }
}
