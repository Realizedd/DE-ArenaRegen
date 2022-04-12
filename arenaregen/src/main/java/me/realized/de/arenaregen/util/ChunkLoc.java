package me.realized.de.arenaregen.util;

import java.util.Objects;

import org.bukkit.Chunk;

import lombok.Getter;

public class ChunkLoc {

    @Getter
    private final int x, z;

    public ChunkLoc(final int x, final int z) {
        this.x = x;
        this.z = z;
    }

    public ChunkLoc(final Chunk chunk) {
        this(chunk.getX(), chunk.getZ());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final ChunkLoc chunkLoc = (ChunkLoc) o;
        return x == chunkLoc.x && z == chunkLoc.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public String toString() {
        return "{" + x + "," + z + "}";
    }
}