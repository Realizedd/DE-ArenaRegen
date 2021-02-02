package me.realized.de.arenaregen.util;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public final class StringUtil {

    public static String color(final String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String from(final Location location) {
        return "(" + location.getWorld().getName() + ", " + location.getX() + ", " + location.getY() + ", " + location.getZ() + ")";
    }

    public static String fromList(final List<?> list) {
        StringBuilder builder = new StringBuilder();

        if (list != null && !list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                builder.append(list.get(i).toString()).append(i + 1 != list.size() ? "\n" : "");
            }
        }

        return builder.toString();
    }


    private StringUtil() {}
}
