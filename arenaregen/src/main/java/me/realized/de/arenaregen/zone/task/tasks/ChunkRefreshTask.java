package me.realized.de.arenaregen.zone.task.tasks;

import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.Chunk;

import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.util.Callback;
import me.realized.de.arenaregen.util.ChunkLoc;
import me.realized.de.arenaregen.zone.Zone;
import me.realized.de.arenaregen.zone.task.Task;

public class ChunkRefreshTask extends Task {

    private final Queue<ChunkLoc> chunks;

    public ChunkRefreshTask(final ArenaRegen extension, final Zone zone, final Callback onDone) {
        super(extension, zone, onDone);
        this.chunks = new LinkedList<>(zone.getChunks());
    }

    @Override
    public void run() {
        ChunkLoc current = chunks.poll();

        if (current == null) {
            cancel();
            zone.startTask(null);
            zone.getArena().setDisabled(false);

            if (onDone != null) {
                onDone.call();
            }

            return;
        }

        final Chunk chunk = zone.getMin().getWorld().getChunkAt(current.getX(), current.getZ());

        if (!chunk.isLoaded()) {
            return;
        }

        api.getServer().getOnlinePlayers().stream().filter(player -> player.getWorld().equals(chunk.getWorld())).forEach(online -> handler.sendChunkUpdate(online, chunk));
    }
    
}
