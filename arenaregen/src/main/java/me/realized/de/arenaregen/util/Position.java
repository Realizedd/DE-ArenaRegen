package me.realized.de.arenaregen.util;

import java.util.Objects;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class Position {

    @Getter
    private final int x, y, z;

    public Position(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Position(final BlockState state) {
        this(state.getLocation().getBlockX(), state.getLocation().getBlockY(), state.getLocation().getBlockZ());
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

    @Override
    public String toString() {
        return x + ";" + y + ";" + z;
    }
}
