package me.realized.de.arenaregen.util;

import java.util.Objects;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class Position {

    @Getter
    private final int x, y, z;

    public Position(final BlockState state) {
        final Location location = state.getLocation();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    public Position(final Block block) {
        this(block.getState());
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
