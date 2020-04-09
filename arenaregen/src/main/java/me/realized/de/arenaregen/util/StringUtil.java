package me.realized.de.arenaregen.util;

import org.bukkit.ChatColor;
import org.bukkit.Location;

public final class StringUtil {

    public static String color(final String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String from(final Location location) {
        return "(" + location.getWorld().getName() + ", " + location.getX() + ", " + location.getY() + ", " + location.getZ() + ")";
    }

    private StringUtil() {}
}
