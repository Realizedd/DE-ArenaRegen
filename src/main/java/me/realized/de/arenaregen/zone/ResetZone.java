package me.realized.de.arenaregen.zone;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import lombok.Getter;
import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.config.Config;
import me.realized.de.arenaregen.util.BlockInfo;
import me.realized.de.arenaregen.util.Position;
import me.realized.de.arenaregen.util.compat.NMSUtil;
import me.realized.duels.api.Duels;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

public class ResetZone {

    @Getter
    private final Duels api;

    @Getter
    private final Config config;
    @Getter
    private final String name;
    @Getter
    private final Location min, max;

    private final Map<Position, BlockInfo> blocks = new HashMap<>();
    private final File file;

    ResetZone(final ArenaRegen extension, final Duels api, final String name, final File folder, final Location first, final Location second) {
        this.api = api;
        this.config = extension.getConfiguration();
        this.name = name;
        this.file = new File(folder, name + ".yml");
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
    }

    ResetZone(final ArenaRegen extension, final Duels api, final String name, final File file) {
        this.api = api;
        this.config = extension.getConfiguration();

        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        this.name = name;
        this.file = file;

        final String worldName = config.getString("world");
        final World world;

        if (worldName == null || (world = Bukkit.getWorld(worldName)) == null) {
            throw new NullPointerException("worldName or world is null");
        }

        this.min = new Location(world, config.getInt("min.x"), config.getInt("min.y"), config.getInt("min.z"));
        this.max = new Location(world, config.getInt("max.x"), config.getInt("max.y"), config.getInt("max.z"));

        final ConfigurationSection blocks = config.getConfigurationSection("blocks");

        if (blocks == null) {
            return;
        }

        blocks.getKeys(false).forEach(key -> {
            final String[] posData = key.split(";");
            final Position pos = new Position(Integer.parseInt(posData[0]), Integer.parseInt(posData[1]), Integer.parseInt(posData[2]));
            final String[] blockData = blocks.getString(key).split(";");
            final BlockInfo info = new BlockInfo(Material.getMaterial(blockData[0]), Byte.parseByte(blockData[1]));
            this.blocks.put(pos, info);
        });
    }

    void save() throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }

        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("world", min.getWorld().getName());
        config.set("min.x", min.getBlockX());
        config.set("min.y", min.getBlockY());
        config.set("min.z", min.getBlockZ());
        config.set("max.x", max.getBlockX());
        config.set("max.y", max.getBlockY());
        config.set("max.z", max.getBlockZ());
        blocks.forEach((position, info) -> config.set("blocks." + position.toString(), info.toString()));
        config.save(file);
    }

    void delete() {
        blocks.clear();
        file.delete();
    }

    public int getTotalBlocks() {
        return blocks.size();
    }

    void loadBlocks() {
        // Only store non-air blocks
        doForAll(block -> {
            if (block.getType() == Material.AIR) {
                return;
            }

            blocks.put(new Position(block), new BlockInfo(block.getState()));
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

    @SuppressWarnings("deprecation")
    public void reset() {
        final Set<ChunkLoc> chunks = new HashSet<>();

        doForAll(block -> {
            final BlockInfo info = blocks.get(new Position(block));

            if (info == null) {
                // If no stored information is available (= air) but block is not air, set to air
                if (block.getType() != Material.AIR) {
                    NMSUtil.setBlockFast(block, Material.AIR, 0);
                    chunks.add(new ChunkLoc(block.getChunk()));
                }

                return;
            } else if (info.matches(block)) {
                return;
            }

            NMSUtil.setBlockFast(block, info.getType(), info.getData());
            chunks.add(new ChunkLoc(block.getChunk()));
        });
        chunks.forEach(chunkLoc -> min.getWorld().refreshChunk(chunkLoc.x, chunkLoc.z));

        if (!config.isRemoveDroppedItems()) {
            return;
        }

        min.getWorld().getEntitiesByClass(Item.class).stream().filter(item -> contains(item.getLocation())).forEach(Entity::remove);
    }

    private boolean contains(final Location location) {
        return min.getWorld().equals(location.getWorld())
            && min.getBlockX() <= location.getBlockX() && location.getBlockX() <= max.getBlockX()
            && min.getBlockY() <= location.getBlockY() && location.getBlockY() <= max.getBlockY()
            && min.getBlockZ() <= location.getBlockZ() && location.getBlockZ() <= max.getBlockZ();
    }

    @SuppressWarnings("deprecation")
    boolean isCached(final Block block) {
        return contains(block.getLocation()) && blocks.containsKey(new Position(block));
    }

    private class ChunkLoc {

        private final int x, z;

        ChunkLoc(final Chunk chunk) {
            this.x = chunk.getX();
            this.z = chunk.getZ();
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
    }
}