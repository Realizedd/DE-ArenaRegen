package me.realized.de.arenaregen.util;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class BlockInfo {

    @Getter
    private final Material type;
    @Getter
    private final byte data;

    public BlockInfo(final Material type, final byte data) {
        this.type = type;
        this.data = data;
    }

    public BlockInfo() {
        this(Material.AIR, (byte) 0);
    }

    @SuppressWarnings("deprecation")
    public BlockInfo(final BlockState state) {
        this(state.getType(), state.getRawData());
    }

    public boolean matches(final Block block) {
        return block.getType() == type && block.getData() == data;
    }

    @Override
    public String toString() {
        return type + ";" + data;
    }
}
