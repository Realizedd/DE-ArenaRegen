package me.realized.de.arenaregen.util.compat;

import me.realized.de.arenaregen.util.NumberUtil;
import me.realized.de.arenaregen.util.ReflectionUtil;
import org.bukkit.Bukkit;

final class CompatUtil {

    private static final int SUB_VERSION;
    private static final boolean PAPER_SPIGOT;

    static {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        SUB_VERSION = NumberUtil.parseInt(packageName.substring(packageName.lastIndexOf('.') + 1).split("_")[1]).orElse(0);
        PAPER_SPIGOT = ReflectionUtil.getClassUnsafe("com.destroystokyo.paper.PaperConfig") != null;
    }

    private CompatUtil() {}

    static boolean isPre1_8() {
        return SUB_VERSION < 8;
    }

    static boolean isPre1_13() {
        return SUB_VERSION < 13;
    }

    static boolean isPaperSpigot() {
        return PAPER_SPIGOT;
    }
}
