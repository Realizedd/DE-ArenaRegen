package me.realized.de.arenaregen.util;

import java.util.Objects;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class Position {

    @Getter
    private final int x, y, z;

    public Position(final Block block) {
        final Location location = block.getLocation();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final Position position = (Position) other;
        return x == position.x && y == position.y && z == position.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
