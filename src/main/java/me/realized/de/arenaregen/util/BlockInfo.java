package me.realized.de.arenaregen.util;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockInfo {

    @Getter
    private final Material type;
    @Getter
    private final byte data;

    public BlockInfo() {
        this.type = Material.AIR;
        this.data = 0;
    }

    @SuppressWarnings("deprecation")
    public BlockInfo(final Block block) {
        this.type = block.getType();
        this.data = block.getData();
    }
}
