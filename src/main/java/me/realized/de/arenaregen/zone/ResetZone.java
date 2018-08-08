package me.realized.de.arenaregen.zone;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import lombok.Getter;
import me.realized.de.arenaregen.util.BlockInfo;
import me.realized.de.arenaregen.util.Position;
import me.realized.de.arenaregen.util.compat.NMSUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

public class ResetZone {

    @Getter
    private final String name;
    @Getter
    private final Location min, max;
    @Getter
    private final Map<Position, BlockInfo> blocks = new HashMap<>();

    public ResetZone(final String name, final Location first, final Location second) {
        this.name = name;
        this.min = new Location(
            first.getWorld(),
            Math.min(first.getBlockX(), second.getBlockX()),
            Math.min(first.getBlockY(), second.getBlockY()),
            Math.min(first.getBlockZ(), second.getBlockZ())
        );
        this.max = new Location(
            first.getWorld(),
            Math.max(first.getBlockX(), second.getBlockX()),
            Math.max(first.getBlockY(), second.getBlockY()),
            Math.max(first.getBlockZ(), second.getBlockZ())
        );

        // Only store non-air blocks
        doForAll(block -> {
            if (block.getType().name().contains("AIR")) {
                return;
            }

            blocks.put(new Position(block), new BlockInfo(block.getState()));
            System.out.println("Storing " + block + ".");
        });
    }

    private void doForAll(final Consumer<Block> consumer) {
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    consumer.accept(min.getWorld().getBlockAt(x, y, z));
                }
            }
        }
    }

    private boolean removeDroppedItems = true;

    public void reset() {
        final Set<Chunk> chunks = new HashSet<>();
        doForAll(block -> {
            final BlockInfo info = blocks.get(new Position(block));

            if (info == null) {
                // If no stored information is available (= air) but block is not air, set to air
                if (!block.getType().name().contains("AIR")) {
                    NMSUtil.setBlockFast(block, Material.AIR, 0);
                }

                return;
            }

            chunks.add(block.getChunk());
            NMSUtil.setBlockFast(block, info.getType(), info.getData());
        });
        chunks.forEach(chunk -> chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ()));

        if (!removeDroppedItems) {
            return;
        }

        min.getWorld().getEntitiesByClass(Item.class).stream().filter(item -> contains(item.getLocation())).forEach(Entity::remove);
    }

    public boolean contains(final Location location) {
        return min.getWorld().equals(location.getWorld())
            && min.getBlockX() <= location.getBlockX() && location.getBlockX() <= max.getBlockX()
            && min.getBlockY() <= location.getBlockY() && location.getBlockY() <= max.getBlockY()
            && min.getBlockZ() <= location.getBlockZ() && location.getBlockZ() <= max.getBlockZ();
    }

    public boolean contains(final BlockState state) {
        return contains(state.getLocation());
    }

    public boolean contains(final Block block) {
        return contains(block.getState());
    }

    @SuppressWarnings("deprecation")
    public boolean isCached(final Block block) {
        return contains(block) && blocks.containsKey(new Position(block));
    }
}
