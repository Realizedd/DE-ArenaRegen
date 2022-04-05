package me.realized.de.arenaregen.zone;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

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
import org.bukkit.scheduler.BukkitRunnable;

import lombok.Getter;
import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.config.Config;
import me.realized.de.arenaregen.nms.NMS;
import me.realized.de.arenaregen.nms.fallback.NMSHandler;
import me.realized.de.arenaregen.util.BlockInfo;
import me.realized.de.arenaregen.util.BlockUtil;
import me.realized.de.arenaregen.util.Callback;
import me.realized.de.arenaregen.util.Pair;
import me.realized.de.arenaregen.util.Position;
import me.realized.duels.api.Duels;
import me.realized.duels.api.arena.Arena;

public class ResetZone {

    @Getter
    private final Duels api;
    private final NMS handler;

    @Getter
    private final Config config;
    @Getter
    private final Arena arena;
    @Getter
    private Location min, max;

    private final Map<Position, BlockInfo> blocks = new HashMap<>();
    private File file;

    @Getter
    private BukkitRunnable task;

    @Getter
    private Set<ChunkLoc> chunks = new HashSet<>();

    @Getter
    private List<Entity> spawnedEntities = new ArrayList<>();

    ResetZone(final ArenaRegen extension, final Duels api, final Arena arena, final File folder, final Location first, final Location second) {
        this.api = api;
        this.handler = extension.getHandler();
        this.config = extension.getConfiguration();
        this.arena = arena;
        this.file = new File(folder, arena.getName() + ".txt");
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

        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(min.getWorld().getName());
            writer.newLine();
            writer.write(String.valueOf(min.getBlockX()));
            writer.newLine();
            writer.write(String.valueOf(min.getBlockY()));
            writer.newLine();
            writer.write(String.valueOf(min.getBlockZ()));
            writer.newLine();
            writer.write(String.valueOf(max.getBlockX()));
            writer.newLine();
            writer.write(String.valueOf(max.getBlockY()));
            writer.newLine();
            writer.write(String.valueOf(max.getBlockZ()));
            writer.newLine();

            for (Map.Entry<Position, BlockInfo> entry : blocks.entrySet()) {
                writer.write(entry.getKey().toString() + ":" + entry.getValue().toString());
                writer.newLine();
            }
        } catch (IOException ex) {
            extension.error("Could not save reset zone '" + getName()+ "'!", ex);
        }
    }

    ResetZone(final ArenaRegen extension, final Duels api, final Arena arena, File file) throws IOException {
        this.api = api;
        this.handler = extension.getHandler();
        this.config = extension.getConfiguration();
        this.arena = arena;
        this.file = file;
        
        if (file.getName().endsWith(".yml")) {
            final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            final File newFile = new File(file.getParent(), arena.getName() + ".txt");
            
            try (final BufferedWriter writer = new BufferedWriter(new FileWriter(newFile))) {
                writer.write(config.getString("world"));
                writer.newLine();
                writer.write(String.valueOf(config.getInt("min.x")));
                writer.newLine();
                writer.write(String.valueOf(config.getInt("min.y")));
                writer.newLine();
                writer.write(String.valueOf(config.getInt("min.z")));
                writer.newLine();
                writer.write(String.valueOf(config.getInt("max.x")));
                writer.newLine();
                writer.write(String.valueOf(config.getInt("max.y")));
                writer.newLine();
                writer.write(String.valueOf(config.getInt("max.z")));
                writer.newLine();

                final ConfigurationSection blocks = config.getConfigurationSection("blocks");

                if (blocks == null) {
                    return;
                }
    
                for (String key : blocks.getKeys(false)) {
                    writer.write(key + ":" + blocks.getString(key));
                    writer.newLine();
                }
            }
            
            file.delete();
            extension.info("Converted " + file.getName() + " to " + newFile.getName() + ".");

            this.file = file = newFile;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String worldName = reader.readLine();
            final World world;

            if (worldName == null || (world = Bukkit.getWorld(worldName)) == null) {
                throw new NullPointerException("worldName or world is null");
            }

            this.min = new Location(world, Integer.parseInt(reader.readLine()), Integer.parseInt(reader.readLine()), Integer.parseInt(reader.readLine()));
            this.max = new Location(world, Integer.parseInt(reader.readLine()), Integer.parseInt(reader.readLine()), Integer.parseInt(reader.readLine()));

            String block;

            while ((block = reader.readLine()) != null) {
                final String[] data = block.split(":");
                final String[] posData = data[0].split(";");
                final Position pos = new Position(Integer.parseInt(posData[0]), Integer.parseInt(posData[1]), Integer.parseInt(posData[2]));
                final String[] blockData = data[1].split(";");
                final BlockInfo info = new BlockInfo(Material.getMaterial(blockData[0]), Byte.parseByte(blockData[1]));
                this.blocks.put(pos, info);
            }
        }
    }

    public String getName() {
        return arena.getName();
    }

    public int getTotalBlocks() {
        return blocks.size();
    }

    void delete() {
        blocks.clear();
        file.delete();
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

    public boolean isResetting() {
        return task != null;
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

    // Called before reset zones are saved to files.
    public void resetInstant() {
        doForAll(block -> {
            final Position position = new Position(block);
            final BlockInfo info = blocks.get(position);

            if (info == null) {
                if (block.getType() != Material.AIR) {
                    handler.setBlockFast(block, Material.AIR, 0);
                }

                return;
            } else if (info.matches(block)) {
                return;
            }

            handler.setBlockFast(block, info.getType(), info.getData());
        });

        if (!config.isRemoveDroppedItems()) {
            return;
        }

        min.getWorld().getEntitiesByClass(Item.class).stream().filter(item -> contains(item.getLocation())).forEach(Entity::remove);
    }

    public void reset(final Callback onDone) {
        arena.setDisabled(true);
        task = new IndexTask(onDone);
        task.runTaskTimer(api, 1L, 1L);
    }

    public boolean contains(final Location location) {
        return min.getWorld().equals(location.getWorld())
            && min.getBlockX() <= location.getBlockX() && location.getBlockX() <= max.getBlockX()
            && min.getBlockY() <= location.getBlockY() && location.getBlockY() <= max.getBlockY()
            && min.getBlockZ() <= location.getBlockZ() && location.getBlockZ() <= max.getBlockZ();
    }

    @SuppressWarnings("deprecation")
    boolean isCached(final Block block) {
        return contains(block.getLocation()) && blocks.containsKey(new Position(block));
    }

    boolean isCached(final Chunk chunk) {
        return chunks.contains(new ChunkLoc(chunk));
    }

    private static class ChunkLoc {

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

    public class IndexTask extends BukkitRunnable {

        private final Callback onDone;
        private final Queue<Pair<Block, BlockInfo>> changed = new LinkedList<>();
        private int x = min.getBlockX();

        public IndexTask(final Callback onDone) {
            this.onDone = onDone;
        }

        @Override
        public void run() {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    final Block block = min.getWorld().getBlockAt(x, y, z);
                    final Position position = new Position(block);
                    final BlockInfo info = blocks.get(position);

                    chunks.add(new ChunkLoc(block.getChunk()));

                    if (info == null) {
                        // If no stored information is available (= air) but block is not air, set to air
                        if (block.getType() != Material.AIR) {
                            changed.add(new Pair<>(block, new BlockInfo()));
                        }

                        continue;
                    } else if (info.matches(block)) {
                        continue;
                    }

                    changed.add(new Pair<>(block, info));
                }
            }

            x++;

            if (x > max.getBlockX()) {
                cancel();
                task = new ResetTask(onDone, changed);
                task.runTaskTimer(api, 1L, 1L);
            }
        }
    }

    public class ResetTask extends BukkitRunnable {

        private final Callback onDone;
        private final Queue<Pair<Block, BlockInfo>> changed;

        public ResetTask(final Callback onDone, final Queue<Pair<Block, BlockInfo>> changed) {
            this.onDone = onDone;
            this.changed = changed;
        }

        @Override
        public void run() {
            int count = 0;
            Pair<Block, BlockInfo> current;

            while ((current = changed.poll()) != null) {
                final Block block = current.getKey();
                final BlockInfo info = current.getValue();
                handler.setBlockFast(block, info.getType(), info.getData());
                count++;

                if (count >= config.getBlocksPerTick()) {
                    return;
                }
            }

            cancel();

            // Skip relighting if using fallback handler
            task = handler instanceof NMSHandler ? new ChunkRefreshTask(onDone) : new RelightTask(onDone);
            task.runTaskTimer(api, 1L, 1L);
        }
    }

    public class RelightTask extends BukkitRunnable {

        private final Callback onDone;
        private int x = min.getBlockX();

        public RelightTask(final Callback onDone) {
            this.onDone = onDone;
        }

        @Override
        public void run() {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    final Block block = min.getWorld().getBlockAt(x, y, z);
                    
                    if (block.getType() == Material.AIR || BlockUtil.isSurrounded(block)) {
                        continue;
                    }

                    handler.updateLighting(block);
                }
            }

            x++;

            if (x > max.getBlockX()) {
                cancel();
                task = new ChunkRefreshTask(onDone);
                task.runTaskTimer(api, 1L, 1L);
            }
        }
    }


    public class ChunkRefreshTask extends BukkitRunnable {

        private final Callback onDone;
        private final Queue<ChunkLoc> chunks;

        public ChunkRefreshTask(final Callback onDone) {
            this.onDone = onDone;
            this.chunks = new LinkedList<>(ResetZone.this.chunks);
        }

        @Override
        public void run() {
            ChunkLoc current = chunks.poll();

            if (current == null) {
                cancel();
                task = null;

                arena.setDisabled(false);

                if (onDone != null) {
                    onDone.call();
                }

                return;
            }

            final Chunk chunk = min.getWorld().getChunkAt(current.x, current.z);

            if (!chunk.isLoaded()) {
                return;
            }

            api.getServer().getOnlinePlayers().stream().filter(player -> player.getWorld().equals(chunk.getWorld())).forEach(online -> handler.sendChunkUpdate(online, chunk));
        }
    }
}
