package me.realized.de.arenaregen.zone.task.tasks;

import java.util.Queue;

import org.bukkit.block.Block;

import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.nms.fallback.NMSHandler;
import me.realized.de.arenaregen.util.BlockInfo;
import me.realized.de.arenaregen.util.Callback;
import me.realized.de.arenaregen.util.Pair;
import me.realized.de.arenaregen.zone.ResetZone;
import me.realized.de.arenaregen.zone.task.Task;

public class ResetBlocksTask extends Task {
    
    private final Queue<Pair<Block, BlockInfo>> changed;

    public ResetBlocksTask(final ArenaRegen extension, final ResetZone zone, final Callback onDone, final Queue<Pair<Block, BlockInfo>> changed) {
        super(extension, zone, onDone);
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
        if (handler instanceof NMSHandler) {
            return;
        }

        zone.startTask(new RelightBlocksTask(extension, zone, onDone));
    }
}
