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

    @SuppressWarnings("deprecation")
    public BlockInfo(final BlockState state) {
        this.type = state.getType();
        this.data = state.getRawData();
    }
}
