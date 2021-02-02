package me.realized.de.arenaregen.config;

import lombok.Getter;
import me.realized.de.arenaregen.ArenaRegen;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    @Getter
    private final Material selectingTool;
    @Getter
    private final int blocksPerTick;
    @Getter
    private final boolean allowArenaBlockBreak;
    @Getter
    private final boolean removeDroppedItems;
    @Getter
    private final boolean preventBlockBurn;
    @Getter
    private final boolean preventBlockMelt;
    @Getter
    private final boolean preventBlockExplode;
    @Getter
    private final boolean preventFireSpread;
    @Getter
    private final boolean preventLeafDecay;

    public Config(final ArenaRegen extension) {
        final FileConfiguration config = extension.getConfig();
        this.selectingTool = Material.getMaterial(config.getString("selecting-tool", "IRON_AXE"));
        this.allowArenaBlockBreak = config.getBoolean("allow-arena-block-break", false);
        this.blocksPerTick = config.getInt("blocks-per-tick", 25);
        this.removeDroppedItems = config.getBoolean("remove-dropped-items", true);
        this.preventBlockBurn = config.getBoolean("prevent-block-burn", true);
        this.preventBlockMelt = config.getBoolean("prevent-block-melt", true);
        this.preventBlockExplode = config.getBoolean("prevent-block-explode", true);
        this.preventFireSpread = config.getBoolean("prevent-fire-spread", true);
        this.preventLeafDecay = config.getBoolean("prevent-leaf-decay", true);
    }
}
