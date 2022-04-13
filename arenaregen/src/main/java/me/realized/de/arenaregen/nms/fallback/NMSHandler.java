package me.realized.de.arenaregen.nms.fallback;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import me.realized.de.arenaregen.nms.NMS;
import me.realized.de.arenaregen.util.ReflectionUtil;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class NMSHandler implements NMS {

    private Method SET_DATA;

    public NMSHandler() {
        SET_DATA = ReflectionUtil.getMethod(Block.class, "setData", byte.class);
    }

    @Override
    public void sendChunkUpdate(final Player player, final Chunk chunk) {}

    @Override
    public void setBlockFast(final Block block, final Material material, final int data) {
        block.setType(material);

        if (SET_DATA != null) {
            try {
                SET_DATA.invoke(block, (byte) data);
            } catch (IllegalAccessException | InvocationTargetException ignored) {}
        }
    }

    @Override
    public void updateLighting(Block bukkitBlock) {}
}
