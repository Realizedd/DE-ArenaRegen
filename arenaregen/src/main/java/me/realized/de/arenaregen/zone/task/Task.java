package me.realized.de.arenaregen.zone.task;

import org.bukkit.scheduler.BukkitRunnable;

import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.config.Config;
import me.realized.de.arenaregen.nms.NMS;
import me.realized.de.arenaregen.util.Callback;
import me.realized.de.arenaregen.zone.Zone;
import me.realized.duels.api.Duels;

public abstract class Task extends BukkitRunnable {
    
    protected final ArenaRegen extension;
    protected final Duels api;
    protected final Config config;
    protected final NMS handler;
    protected final Zone zone;
    protected final Callback onDone;

    protected Task(ArenaRegen extension, Zone zone, Callback onDone) {
        this.extension = extension;
        this.api = extension.getApi();
        this.config = extension.getConfiguration();
        this.handler = extension.getHandler();
        this.zone = zone;
        this.onDone = onDone;
    }
}
