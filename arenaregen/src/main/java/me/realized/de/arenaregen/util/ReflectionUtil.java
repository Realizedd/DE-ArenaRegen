package me.realized.de.arenaregen.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;

public final class ReflectionUtil {

    private final static String VERSION;

    static {
        VERSION = Bukkit.getServer().getClass().getName().split("\\.")[3];
    }

    private ReflectionUtil() {}

    public static Class<?> getClassUnsafe(final String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    public static Class<?> getNMSClass(final String name) {
        try {
            return Class.forName("net.minecraft.server." + VERSION + "." + name);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    public static Class<?> getCBClass(final String path) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + VERSION + "." + path);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    public static Method getMethod(final Class<?> clazz, final String name, final Class<?>... parameters) {
        try {
            return clazz.getMethod(name, parameters);
        } catch (Throwable throwable) {
            return null;
        }
    }

    public static Field getField(final Class<?> clazz, final String name) {
        try {
            return clazz.getField(name);
        } catch (Throwable throwable) {
            return null;
        }
    }

    public static Constructor<?> getConstructor(final Class<?> clazz, final Class<?>... parameters) {
        try {
            return clazz.getConstructor(parameters);
        } catch (Throwable throwable) {
            return null;
        }
    }

    public static Object getEnumConstant(final Class<?> clazz, final int index) {
        try {
            return clazz.getEnumConstants()[index];
        } catch (Throwable throwable) {
            return null;
        }
    }
}
