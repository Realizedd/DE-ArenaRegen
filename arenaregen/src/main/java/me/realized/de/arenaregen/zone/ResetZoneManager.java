package me.realized.de.arenaregen.zone;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.config.Config;
import me.realized.de.arenaregen.config.Lang;
import me.realized.de.arenaregen.selection.Selection;
import me.realized.de.arenaregen.util.CompatUtil;
import me.realized.duels.api.Duels;
import me.realized.duels.api.arena.Arena;
import me.realized.duels.api.arena.ArenaManager;
import me.realized.duels.api.event.arena.ArenaRemoveEvent;
import me.realized.duels.api.event.match.MatchEndEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ResetZoneManager {

    private final ArenaRegen extension;
    private final Duels api;
    private final ArenaManager arenaManager;
    private final Config config;
    private final Lang lang;
    private final File folder;

    private final Map<String, ResetZone> zones = new HashMap<>();

    public ResetZoneManager(final ArenaRegen extension, final Duels api) {
        this.extension = extension;
        this.api = api;
        this.arenaManager = api.getArenaManager();
        this.config = extension.getConfiguration();
        this.lang = extension.getLang();
        this.folder = new File(extension.getDataFolder(), "zones");
        api.registerListener(new ResetZoneListener());

        if (CompatUtil.hasBlockExplodeEvent()) {
            api.registerListener(new BlockExplodeListener());
        }

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
                    zones.put(name, new ResetZone(extension, api, arena, file));
                } catch (Exception ex) {
                    extension.error("Could not load reset zone '" + name + "'!", ex);
                }
            }
        }
    }

    public void handleDisable() {
        zones.values().stream().filter(ResetZone::isResetting).forEach(zone -> {
            zone.getArena().setDisabled(false);
            zone.getTask().cancel();
            zone.resetInstant();
        });
    }

    public ResetZone get(final String name) {
        return zones.get(name);
    }

    public boolean create(final Arena arena, final Selection selection) {
        if (zones.containsKey(arena.getName())) {
            return false;
        }

        final ResetZone zone = new ResetZone(extension, api, arena, folder, selection.getFirst(), selection.getSecond());
        zone.loadBlocks();
        zones.put(arena.getName(), zone);
        return true;
    }

    public boolean remove(final String name) {
        final ResetZone zone = zones.remove(name);

        if (zone == null) {
            return false;
        }

        zone.delete();
        return true;
    }

    public Collection<ResetZone> getZones() {
        return zones.values();
    }

    private class ResetZoneListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void on(final ChunkUnloadEvent event) {
            if (!CompatUtil.isPurpur() && zones.values().stream().anyMatch(zone -> zone.isResetting() && zone.isCached(event.getChunk()))) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void on(final MatchEndEvent event) {
            final Arena arena = event.getMatch().getArena();
            final ResetZone zone = get(arena.getName());

            if (zone == null) {
                return;
            }

            zone.reset(null);
        }

        @EventHandler
        public void on(final ArenaRemoveEvent event) {
            remove(event.getArena().getName());
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void on(final BlockBreakEvent event) {
            final Player player = event.getPlayer();

            if (config.isAllowArenaBlockBreak()
                || !arenaManager.isInMatch(player)
                || zones.values().stream().noneMatch(zone -> zone.isCached(event.getBlock()))) {
                return;
            }

            event.setCancelled(true);
            lang.sendMessage(player, "ERROR.cancel-arena-block-break");
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void on(final BlockFadeEvent event) {
            if (!config.isPreventBlockMelt() || zones.values().stream().noneMatch(zone -> zone.isCached(event.getBlock()))) {
                return;
            }

            final Material changedType = event.getNewState().getType();

            if (!(changedType == Material.AIR || changedType.name().contains("WATER"))) {
                return;
            }

            event.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void on(final BlockBurnEvent event) {
            if (!config.isPreventBlockBurn() || zones.values().stream().noneMatch(zone -> zone.isCached(event.getBlock()))) {
                return;
            }

            event.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void on(final EntityExplodeEvent event) {
            if (!config.isPreventBlockExplode() || event.blockList().stream().allMatch(block -> zones.values().stream().noneMatch(zone -> zone.isCached(block)))) {
                return;
            }

            event.setCancelled(true);
        }


        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void on(final BlockIgniteEvent event) {
            if (!config.isPreventFireSpread() || event.getCause() != IgniteCause.SPREAD || zones.values().stream().noneMatch(zone -> zone.isCached(event.getBlock()))) {
                return;
            }

            event.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void on(final LeavesDecayEvent event) {
            if (!config.isPreventLeafDecay() || zones.values().stream().noneMatch(zone -> zone.isCached(event.getBlock()))) {
                return;
            }

            event.setCancelled(true);
        }
    }

    private class BlockExplodeListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void on(final BlockExplodeEvent event) {
            if (!config.isPreventBlockExplode() || event.blockList().stream().allMatch(block -> zones.values().stream().noneMatch(zone -> zone.isCached(block)))) {
                return;
            }

            event.setCancelled(true);
        }
    }
}
