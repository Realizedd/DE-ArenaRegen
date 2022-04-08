package me.realized.de.arenaregen.zone;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.selection.Selection;
import me.realized.duels.api.Duels;
import me.realized.duels.api.arena.Arena;
import me.realized.duels.api.arena.ArenaManager;

public class ZoneManager {

    private final ArenaRegen extension;
    private final Duels api;
    private final ArenaManager arenaManager;
    private final File folder;

    private final Map<String, Zone> zones = new HashMap<>();

    public ZoneManager(final ArenaRegen extension, final Duels api) {
        this.extension = extension;
        this.api = api;
        this.arenaManager = api.getArenaManager();
        this.folder = new File(extension.getDataFolder(), "zones");
        api.registerListener(new ZoneListener(extension, this));

        if (!folder.exists()) {
            folder.mkdir();
        }

        final File[] files = folder.listFiles();

        if (files != null) {
            for (final File file : files) {
                final String name = file.getName().substring(0, file.getName().lastIndexOf("."));
                final Arena arena = arenaManager.get(name);
                
                if (arena == null) {
                    file.delete();
                    continue;
                }

                try {
                    zones.put(name, new Zone(extension, api, arena, file));
                } catch (Exception ex) {
                    extension.error("Could not load reset zone '" + name + "'!", ex);
                }
            }
        }
    }

    public void handleDisable() {
        zones.values().stream().filter(zone -> zone.getArena().isUsed() || zone.isResetting()).forEach(zone -> {
            zone.getArena().setDisabled(false);
            zone.getTask().cancel();
            zone.resetInstant();
        });
    }

    public Zone get(final String name) {
        return zones.get(name);
    }

    public Zone get(final Arena arena) {
        return get(arena.getName());
    }

    public Zone get(final Player player) {
        final Arena arena = arenaManager.get(player);
        return arena != null ? get(arena) : null;
    }

    public Zone get(final Block block) {
        return zones.values().stream().filter(any -> any.contains(block)).findFirst().orElse(null);
    }

    public boolean create(final Arena arena, final Selection selection) {
        if (zones.containsKey(arena.getName())) {
            return false;
        }

        final Zone zone = new Zone(extension, api, arena, folder, selection.getFirst(), selection.getSecond());
        zones.put(arena.getName(), zone);
        return true;
    }

    public boolean remove(final String name) {
        final Zone zone = zones.remove(name);

        if (zone == null) {
            return false;
        }

        zone.delete();
        return true;
    }

    public Collection<Zone> getZones() {
        return zones.values();
    }
}
