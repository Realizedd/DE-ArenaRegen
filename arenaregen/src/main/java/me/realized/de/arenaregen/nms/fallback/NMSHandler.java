package me.realized.de.arenaregen.nms.fallback;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import me.realized.de.arenaregen.nms.NMS;
import me.realized.de.arenaregen.util.CompatUtil;
import me.realized.de.arenaregen.util.ReflectionUtil;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class NMSHandler implements NMS {

    private Constructor<?> BLOCK_POS_CONSTRUCTOR;
    private Object BLOCK_ENUM;
    private Method WORLD_GET_HANDLE;
    private Method WORLD_C;
    private Method WORLD_UPDATE_LIGHTING;
    private Method CHUNK_GET_HANDLE;
    private Method BLOCK_FROM_LEGACY_DATA;
    private Method BLOCK_GET_DATA;
    private Method MAGIC_GET_BLOCK;
    private Method CHUNK_SET_BLOCK_12;
    private Method CHUNK_SET_BLOCK;

    private Method GET_CHUNK_PROVIDER;
    private Method GET_LIGHT_ENGINE;
    private Method LIGHT_ENGINE_A;

    private Method GET_HANDLE;
    private Field PLAYER_CONNECTION;
    private Method SEND_PACKET;

    private Constructor<?> PACKET_MAP_CHUNK_CONSTRUCTOR;

    public NMSHandler() {
        try {
            final Class<?> BLOCK_POS = ReflectionUtil.getNMSClass("BlockPosition");
            BLOCK_POS_CONSTRUCTOR = ReflectionUtil.getConstructor(BLOCK_POS, Double.TYPE, Double.TYPE, Double.TYPE);
            WORLD_GET_HANDLE = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("CraftWorld"), "getHandle");
            final Class<?> WORLD = ReflectionUtil.getNMSClass("World");
            final Class<?> ENUM_SKY_BLOCK = ReflectionUtil.getNMSClass("EnumSkyBlock");
            WORLD_C = ReflectionUtil.getMethod(WORLD, "c", ENUM_SKY_BLOCK, BLOCK_POS);
            GET_CHUNK_PROVIDER = ReflectionUtil.getMethod(WORLD, "getChunkProvider");
            final Class<?> CHUNK_PROVIDER = ReflectionUtil.getNMSClass("IChunkProvider");
            GET_LIGHT_ENGINE = ReflectionUtil.getMethod(CHUNK_PROVIDER, "getLightEngine");
            final Class<?> LIGHT_ENGINE = ReflectionUtil.getNMSClass("LightEngine");
            LIGHT_ENGINE_A = ReflectionUtil.getMethod(LIGHT_ENGINE, "a", BLOCK_POS);

            if (CompatUtil.isPaper()) {
                WORLD_UPDATE_LIGHTING = ReflectionUtil.getMethod(WORLD, "updateLight", ENUM_SKY_BLOCK, BLOCK_POS);
            }

            BLOCK_ENUM = ReflectionUtil.getEnumConstant(ENUM_SKY_BLOCK, 1);
            CHUNK_GET_HANDLE = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("CraftChunk"), "getHandle");
            final Class<?> BLOCK = ReflectionUtil.getNMSClass("Block");
            BLOCK_FROM_LEGACY_DATA = ReflectionUtil.getMethod(BLOCK, "fromLegacyData", Integer.TYPE);
            BLOCK_GET_DATA = ReflectionUtil.getMethod(BLOCK, "getBlockData");
            MAGIC_GET_BLOCK = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("util.CraftMagicNumbers"), "getBlock", Material.class);
            final Class<?> CHUNK = ReflectionUtil.getNMSClass("Chunk");
            final Class<?> BLOCK_DATA = ReflectionUtil.getNMSClass("IBlockData");
            CHUNK_SET_BLOCK_12 = ReflectionUtil.getMethod(CHUNK, "a", BLOCK_POS, BLOCK_DATA);
            CHUNK_SET_BLOCK = ReflectionUtil.getMethod(CHUNK, "a", BLOCK_POS, BLOCK_DATA, Boolean.TYPE);

            if (CHUNK_SET_BLOCK == null) {
                CHUNK_SET_BLOCK = ReflectionUtil.getMethod(CHUNK, "setType", BLOCK_POS, BLOCK_DATA, Boolean.TYPE);
            }

            final Class<?> CB_PLAYER = ReflectionUtil.getCBClass("entity.CraftPlayer");
            GET_HANDLE = ReflectionUtil.getMethod(CB_PLAYER, "getHandle");

            final Class<?> NMS_PLAYER = ReflectionUtil.getNMSClass("EntityPlayer");
            PLAYER_CONNECTION = ReflectionUtil.getField(NMS_PLAYER, "playerConnection");
            SEND_PACKET = ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("PlayerConnection"), "sendPacket", ReflectionUtil.getNMSClass("Packet"));

            final Class<?> PACKET_MAP_CHUNK = ReflectionUtil.getNMSClass("PacketPlayOutMapChunk");
            PACKET_MAP_CHUNK_CONSTRUCTOR = ReflectionUtil.getConstructor(PACKET_MAP_CHUNK, CHUNK, boolean.class, int.class);

            if (PACKET_MAP_CHUNK_CONSTRUCTOR == null) {
                PACKET_MAP_CHUNK_CONSTRUCTOR = ReflectionUtil.getConstructor(PACKET_MAP_CHUNK, CHUNK, int.class);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void sendChunkUpdate(final Player player, final Chunk chunk) {
        try {
            final Object chunkHandle = CHUNK_GET_HANDLE.invoke(chunk);
            final Object connection = PLAYER_CONNECTION.get(GET_HANDLE.invoke(player));

            if (CompatUtil.isPre1_12()) {
                SEND_PACKET.invoke(connection, PACKET_MAP_CHUNK_CONSTRUCTOR.newInstance(chunkHandle, true, 65535));
            } else {
                SEND_PACKET.invoke(connection, PACKET_MAP_CHUNK_CONSTRUCTOR.newInstance(chunkHandle, 65535));
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setBlockFast(final Block block, final Material material, final int data) {
        final Chunk chunk = block.getChunk();
        final int x = block.getX();
        final int y = block.getY();
        final int z = block.getZ();

        try {
            final Object chunkHandle = CHUNK_GET_HANDLE.invoke(chunk);
            Object nmsBlock = MAGIC_GET_BLOCK.invoke(null, material);

            final Object blockPos = BLOCK_POS_CONSTRUCTOR.newInstance(x, y, z);

            if (CompatUtil.isPre1_13()) {
                nmsBlock = BLOCK_FROM_LEGACY_DATA.invoke(nmsBlock, data);
                CHUNK_SET_BLOCK_12.invoke(chunkHandle, blockPos, nmsBlock);
            } else {
                CHUNK_SET_BLOCK.invoke(chunkHandle, blockPos, BLOCK_GET_DATA.invoke(nmsBlock), true);
            }

            if (block.getType() == Material.AIR || isSurrounded(block.getWorld(), x, y, z)) {
                return;
            }

            final Object worldHandle = WORLD_GET_HANDLE.invoke(block.getWorld());

            if (CompatUtil.isPre1_14()) {
                if (CompatUtil.isPaper() && WORLD_UPDATE_LIGHTING != null) {
                    WORLD_UPDATE_LIGHTING.invoke(worldHandle, BLOCK_ENUM, blockPos);
                } else {
                    WORLD_C.invoke(worldHandle, BLOCK_ENUM, blockPos);
                }
            } else {
                LIGHT_ENGINE_A.invoke(GET_LIGHT_ENGINE.invoke(GET_CHUNK_PROVIDER.invoke(worldHandle)), blockPos);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    private static boolean isSurrounded(final World world, final int x, final int y, final int z) {
        final Block base = world.getBlockAt(x, y, z);
        final Block east = base.getRelative(BlockFace.EAST);
        final Block west = base.getRelative(BlockFace.WEST);
        final Block south = base.getRelative(BlockFace.SOUTH);
        final Block north = base.getRelative(BlockFace.NORTH);
        final Block up = base.getRelative(BlockFace.UP);
        final Block down = base.getRelative(BlockFace.DOWN);
        return !east.getType().isTransparent()
            && !west.getType().isTransparent()
            && !up.getType().isTransparent()
            && !down.getType().isTransparent()
            && !south.getType().isTransparent()
            && !north.getType().isTransparent();
    }
}
