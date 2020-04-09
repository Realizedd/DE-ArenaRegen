package me.realized.de.arenaregen.util;

import lombok.Getter;

public final class Pair<K, V> {

    @Getter
    private final K key;
    @Getter
    private final V value;

    public Pair(final K key, final V value) {
        this.key = key;
        this.value = value;
    }
}
