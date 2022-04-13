package me.realized.de.arenaregen.zone;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

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

import lombok.Getter;
import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.config.Config;
import me.realized.de.arenaregen.nms.NMS;
import me.realized.de.arenaregen.util.BlockInfo;
import me.realized.de.arenaregen.util.BlockUtil;
import me.realized.de.arenaregen.util.Callback;
import me.realized.de.arenaregen.util.ChunkLoc;
import me.realized.de.arenaregen.util.Pair;
import me.realized.de.arenaregen.util.Position;
import me.realized.de.arenaregen.zone.task.Task;
import me.realized.de.arenaregen.zone.task.tasks.ResetBlocksTask;
import me.realized.de.arenaregen.zone.task.tasks.ScanBlocksTask;
import me.realized.duels.api.Duels;
import me.realized.duels.api.arena.Arena;

public class Zone {

    @Getter
    private final Duels api;
    private final ArenaRegen extension;
    private final NMS handler;

    @Getter
    private final Config config;
    @Getter
    private final Arena arena;
    @Getter
    private Location min, max;

    private File file;

    @Getter
    private Task task;

    @Getter
    private final Map<Position, BlockInfo> blocks = new HashMap<>();
    
    @Getter
    private final Set<ChunkLoc> chunks = new HashSet<>();

    @Getter
    private final List<Entity> spawnedEntities = new ArrayList<>();

    private Set<Block> changedBlocks = new HashSet<>();
    private Queue<Pair<Block, BlockInfo>> changes = new LinkedList<>();

    Zone(final ArenaRegen extension, final Duels api, final Arena arena, final File folder, final Location first, final Location second) {
        this.api = api;
        this.extension = extension;
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

        BlockUtil.runForCuboid(min, max, block -> {
            // Only store non-air blocks
            if (block.getType() == Material.AIR) {
                return;
            }

            blocks.put(new Position(block), new BlockInfo(block.getState()));
        });
        
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

        loadChunks();
    }

    Zone(final ArenaRegen extension, final Duels api, final Arena arena, File file) throws IOException {
        this.api = api;
        this.extension = extension;
        this.handler = extension.getHandler();
        this.config = extension.getConfiguration();
        this.arena = arena;
        this.file = file;
        
        // Convert from old yml format if needed
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

        loadChunks();
    }

    private void loadChunks() {
        for (int x = min.getBlockX() >> 4; x <= max.getBlockX() >> 4; x++) {
            for (int z = min.getBlockZ() >> 4; z <= max.getBlockZ() >> 4; z++) {
                chunks.add(new ChunkLoc(x, z));
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

    public boolean isResetting() {
        return task != null;
    }

    // Called before reset zones are saved to files.
    public void resetInstant() {
        BlockUtil.runForCuboid(min, max, block -> {
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
            handler.updateLighting(block);
        });
    }

    public void startTask(final Task task) {
        this.task = task;

        if (task != null) {
            task.runTaskTimer(api, 1L, 1L);
        }
    }

    public void reset(final Callback onDone, final boolean hard) {
        arena.setDisabled(true);

        if (hard) {	
            startTask(new ScanBlocksTask(extension, this, onDone));
            return;
        }

        if (config.isTrackBlockChanges()) {
            startTask(new ResetBlocksTask(extension, this, onDone, this.changes));
            this.changedBlocks = new HashSet<>();
            this.changes = new LinkedList<>();
        } else {
            startTask(new ScanBlocksTask(extension, this, onDone));       
        }
    }

    public void reset(final Callback onDone) {
        reset(onDone, false);
    }

    public void reset() {
        reset(null);
    }

    public boolean contains(final Location location) {
        return min.getWorld().equals(location.getWorld())
            && min.getBlockX() <= location.getBlockX() && location.getBlockX() <= max.getBlockX()
            && min.getBlockY() <= location.getBlockY() && location.getBlockY() <= max.getBlockY()
            && min.getBlockZ() <= location.getBlockZ() && location.getBlockZ() <= max.getBlockZ();
    }

    public boolean contains(final Block block) {
        return contains(block.getLocation());
    }

    boolean isCached(final Block block) {
        return contains(block) && blocks.containsKey(new Position(block));
    }

    boolean isCached(final Chunk chunk) {
        return chunks.contains(new ChunkLoc(chunk));
    }

    public void track(final Block block) {
        if (changedBlocks.contains(block)) {
            return;
        }

        final Position position = new Position(block);
        final BlockInfo info = blocks.get(position);

        if (info == null) {
            if (block.getType() != Material.AIR) {
                changes.add(new Pair<>(block, new BlockInfo()));
                changedBlocks.add(block);
            }

            return;
        } else if (info.matches(block)) {
            return;
        }

        changes.add(new Pair<>(block, info));
        changedBlocks.add(block);
    }

    public void track(final Collection<Block> blocks) {
        blocks.forEach(block -> track(block));
    }
}
