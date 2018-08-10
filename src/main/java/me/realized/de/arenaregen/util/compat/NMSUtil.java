package me.realized.de.arenaregen.util.compat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import me.realized.de.arenaregen.util.ReflectionUtil;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public final class NMSUtil {

    private static Constructor<?> BLOCK_POS_CONSTRUCTOR;
    private static Object SKY_BLOCK_ENUM;
    private static Method GET_HANDLE_WORLD;
    private static Method C_17;
    private static Method C;
    private static Method GET_HANDLE;
    private static Method FROM_LEGACY_DATA;
    private static Method GET_BLOCK_DATA;
    private static Method GET_BLOCK;
    private static Method SET_BLOCK_7;
    private static Method SET_BLOCK_12;
    private static Method SET_BLOCK;

    static {
        try {
            final Class<?> BLOCK_POS = ReflectionUtil.getNMSClass("BlockPosition");
            BLOCK_POS_CONSTRUCTOR = ReflectionUtil.getConstructor(BLOCK_POS, Double.TYPE, Double.TYPE, Double.TYPE);
            GET_HANDLE_WORLD = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("CraftWorld"), "getHandle");
            final Class<?> WORLD = ReflectionUtil.getNMSClass("World");
            final Class<?> ENUM_SKY_BLOCK = ReflectionUtil.getNMSClass("EnumSkyBlock");
            C = ReflectionUtil.getMethod(WORLD, "c", ENUM_SKY_BLOCK, BLOCK_POS);
            C_17 = ReflectionUtil.getMethod(WORLD, "c", ENUM_SKY_BLOCK, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            SKY_BLOCK_ENUM = ReflectionUtil.getEnumConstant(ENUM_SKY_BLOCK, 1);
            GET_HANDLE = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("CraftChunk"), "getHandle");
            final Class<?> BLOCK = ReflectionUtil.getNMSClass("Block");
            FROM_LEGACY_DATA = ReflectionUtil.getMethod(BLOCK, "fromLegacyData", Integer.TYPE);
            GET_BLOCK_DATA = ReflectionUtil.getMethod(BLOCK, "getBlockData");
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

    public static void updateLighting(final Block block) {
        final int x = block.getX();
        final int y = block.getY();
        final int z = block.getZ();

        if (block.getType().name().contains("AIR") || !canAffectLighting(block.getWorld(), x, y, z)) {
            return;
        }

        try {
            if (CompatUtil.isPre1_8()) {
                C_17.invoke(GET_HANDLE_WORLD.invoke(block.getWorld()), SKY_BLOCK_ENUM, x, y, z);
            } else {
                C.invoke(GET_HANDLE_WORLD.invoke(block.getWorld()), SKY_BLOCK_ENUM, BLOCK_POS_CONSTRUCTOR.newInstance(x, y, z));
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public static void setBlockFast(final Block block, final Material material, final int data) {
        final Chunk chunk = block.getChunk();
        final int x = block.getX();
        final int y = block.getY();
        final int z = block.getZ();

        try {
            final Object chunkHandle = GET_HANDLE.invoke(chunk);
            Object nmsBlock = GET_BLOCK.invoke(null, material);

            if (CompatUtil.isPre1_8()) {
                SET_BLOCK_7.invoke(chunkHandle, x & 0x0F, y, z & 0x0F, nmsBlock, data);
            } else {
                final Object blockPos = BLOCK_POS_CONSTRUCTOR.newInstance(x, y, z);

                if (CompatUtil.isPre1_13()) {
                    nmsBlock = FROM_LEGACY_DATA.invoke(nmsBlock, data);
                    SET_BLOCK_12.invoke(chunkHandle, blockPos, nmsBlock);
                } else {
                    SET_BLOCK.invoke(chunkHandle, blockPos, GET_BLOCK_DATA.invoke(nmsBlock), true);
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }



    private static boolean canAffectLighting(final World world, final int x, final int y, final int z) {
        final Block base = world.getBlockAt(x, y, z);
        final Block east = base.getRelative(BlockFace.EAST);
        final Block west = base.getRelative(BlockFace.WEST);
        final Block south = base.getRelative(BlockFace.SOUTH);
        final Block north = base.getRelative(BlockFace.NORTH);
        final Block up = base.getRelative(BlockFace.UP);
        final Block down = base.getRelative(BlockFace.DOWN);

        return east.getType().isTransparent()
            || west.getType().isTransparent()
            || up.getType().isTransparent()
            || down.getType().isTransparent()
            || south.getType().isTransparent()
            || north.getType().isTransparent();
    }

    private NMSUtil() {}
}
