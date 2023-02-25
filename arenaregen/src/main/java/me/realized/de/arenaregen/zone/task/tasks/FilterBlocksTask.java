package me.realized.de.arenaregen.zone.task.tasks;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.util.BlockInfo;
import me.realized.de.arenaregen.util.Callback;
import me.realized.de.arenaregen.util.Pair;
import me.realized.de.arenaregen.util.Position;
import me.realized.de.arenaregen.zone.Zone;
import me.realized.de.arenaregen.zone.task.Task;
import org.bukkit.Material;

public class FilterBlocksTask extends Task {

    private final Set<Position> changedBlocks;

    public FilterBlocksTask(final ArenaRegen extension, final Zone zone, final Callback onDone, final Set<Position> changedBlocks) {
        super(extension, zone, onDone);
        this.changedBlocks = changedBlocks;
    }

    @Override
    public void run() {
        final Queue<Pair<Position, BlockInfo>> changes = new LinkedList<>();

        try (final BufferedReader reader = new BufferedReader(new FileReader(zone.getFile()))) {
            // Skip 7 lines to get to blocks section
            for (int i = 0; i < 7; i++) {
                reader.readLine();
            }

            String block;

            while ((block = reader.readLine()) != null) {
                final String[] data = block.split(":");
                final String[] posData = data[0].split(";");
                final Position pos = new Position(Integer.parseInt(posData[0]), Integer.parseInt(posData[1]), Integer.parseInt(posData[2]));

                if (changedBlocks.contains(pos)) {
                    final String[] blockData = data[1].split(";");
                    final BlockInfo info = new BlockInfo(Material.getMaterial(blockData[0]), Byte.parseByte(blockData[1]));
                    changes.add(new Pair<>(pos, info));
                    changedBlocks.remove(pos);
                }
            }
        } catch (IOException ignored) {
            changes.clear();
            changedBlocks.clear();
        }

        Iterator<Position> iterator = changedBlocks.iterator();

        while (iterator.hasNext()) {
            changes.add(new Pair<>(iterator.next(), new BlockInfo()));
            iterator.remove();
        }

        zone.startSyncTaskTimer(new ResetBlocksTask(extension, zone, onDone, changes));
    }
}
