package me.realized.de.arenaregen.util;

import org.bukkit.Bukkit;

public final class CompatUtil {

    private static final int SUB_VERSION;

    static {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        SUB_VERSION = NumberUtil.parseInt(packageName.substring(packageName.lastIndexOf('.') + 1).split("_")[1]).orElse(0);
    }

    private CompatUtil() {}

    public static boolean isPre1_8() {
        return SUB_VERSION < 8;
    }

    public static boolean isPre1_13() {
        return SUB_VERSION < 13;
    }
}
