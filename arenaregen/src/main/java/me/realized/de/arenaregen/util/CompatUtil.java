package me.realized.de.arenaregen.util;

import lombok.Getter;
import org.bukkit.Bukkit;

public final class CompatUtil {

    private static final int SUB_VERSION;
    private static final boolean PAPER_SPIGOT;

    @Getter
    private static final boolean BLOCK_EXPLODE_EVENT;

    static {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        SUB_VERSION = NumberUtil.parseInt(packageName.substring(packageName.lastIndexOf('.') + 1).split("_")[1]).orElse(0);
        PAPER_SPIGOT = ReflectionUtil.getClassUnsafe("com.destroystokyo.paper.PaperConfig") != null || ReflectionUtil.getClassUnsafe("org.github.paperspigot.PaperSpigotConfig") != null;
        BLOCK_EXPLODE_EVENT = ReflectionUtil.getClassUnsafe("org.bukkit.event.block.BlockExplodeEvent") != null;
    }

    private CompatUtil() {}

    public static boolean isPre1_12() {
        return SUB_VERSION < 12;
    }

    public static boolean isPre1_13() {
        return SUB_VERSION < 13;
    }

    public static boolean isPre1_14() {
        return SUB_VERSION < 14;
    }

    public static boolean isPaper() {
        return PAPER_SPIGOT;
    }

    public static boolean hasBlockExplodeEvent() {
        return BLOCK_EXPLODE_EVENT;
    }
}
