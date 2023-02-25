package me.realized.de.arenaregen.zone;

import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.config.Config;
import me.realized.de.arenaregen.config.Lang;
import me.realized.de.arenaregen.util.ChunkLoc;
import me.realized.de.arenaregen.util.CompatUtil;
import me.realized.duels.api.arena.Arena;
import me.realized.duels.api.event.arena.ArenaRemoveEvent;
import me.realized.duels.api.event.match.MatchEndEvent;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ZoneListener implements Listener {

    private final Config config;
    private final Lang lang;
    private final ZoneManager zoneManager;

    public ZoneListener(final ArenaRegen extension, final ZoneManager zoneManager) {
        this.config = extension.getConfiguration();
        this.lang = extension.getLang();
        this.zoneManager = zoneManager;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final ChunkUnloadEvent event) {
        for (final Entity entity : event.getChunk().getEntities()) {
            if (!(entity instanceof Item) && !config.getRemoveEntities().contains(entity.getType().name().toUpperCase())) {
                continue;
            }

            entity.remove();
        }

        if (!CompatUtil.isPaper() && zoneManager.getZones().stream().anyMatch(zone -> zone.isResetting() && zone.contains(event.getChunk()))) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void on(final MatchEndEvent event) {
        final Arena arena = event.getMatch().getArena();
        final Zone zone = zoneManager.get(arena);

        if (zone == null) {
            return;
        }

        for (final ChunkLoc chunkLoc : zone.getChunks()) {
            final Chunk chunk = zone.getWorld().getChunkAt(chunkLoc.getX(), chunkLoc.getZ());

            for (final Entity entity : chunk.getEntities()) {
                if (!(entity instanceof Item) && !config.getRemoveEntities().contains(entity.getType().name().toUpperCase())) {
                    continue;
                }

                entity.remove();
            }
        }

        zone.reset();
    }


    @EventHandler
    public void on(final ArenaRemoveEvent event) {
        zoneManager.remove(event.getArena().getName());
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final BlockBreakEvent event) {
        final Block block = event.getBlock();
        final Player player = event.getPlayer();
        final Zone zone = zoneManager.get(player);

        if (zone == null) {
            return;
        }

        if (config.isTrackBlockChanges()) {
            zone.track(block);
        }

        if (config.isAllowArenaBlockBreak() || !zone.isCached(block)) {
            return;
        }

        event.setCancelled(true);
        lang.sendMessage(player, "ERROR.cancel-arena-block-break");
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final BlockPlaceEvent event) {
        if (!config.isTrackBlockChanges()) {
            return;
        }

        final Player player = event.getPlayer();
        final Zone zone = zoneManager.get(player);

        if (zone == null) {
            return;
        }

        zone.track(event.getBlock());
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final BlockFadeEvent event) {
        final Block block = event.getBlock();
        final Zone zone = zoneManager.get(block);

        if (zone == null) {
            return;
        }

        if (config.isTrackBlockChanges()) {
            zone.track(block);
        }

        if (!config.isPreventBlockMelt()) {
            return;
        }

        final Material changedType = event.getNewState().getType();

        if (!(changedType == Material.AIR || changedType.name().contains("WATER"))) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final BlockBurnEvent event) {
        final Block block = event.getBlock();
        final Zone zone = zoneManager.get(block);

        if (zone == null) {
            return;
        }

        if (config.isTrackBlockChanges()) {
            zone.track(block);
        }

        if (!config.isPreventBlockBurn()) {
            return;
        }

        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final EntityExplodeEvent event) {
        final Zone zone = zoneManager.get(event.getEntity().getLocation().getBlock());

        if (zone == null) {
            return;
        }

        if (config.isTrackBlockChanges()) {
            zone.track(event.blockList());
        }

        if (!config.isPreventBlockExplode()) {
            return;
        }

        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final BlockExplodeEvent event) {
        final Block block = event.getBlock();
        final Zone zone = zoneManager.get(block);

        if (zone == null) {
            return;
        }

        if (config.isTrackBlockChanges()) {
            zone.track(event.blockList());
        }

        if (!config.isPreventBlockExplode()) {
            return;
        }

        event.setCancelled(true);
    }

    
    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final BlockIgniteEvent event) {
        final Block block = event.getBlock();
        final Zone zone = zoneManager.get(block);

        if (zone == null) {
            return;
        }

        if (config.isTrackBlockChanges()) {
            zone.track(block);
        }
        
        if (!config.isPreventFireSpread() || event.getCause() != IgniteCause.SPREAD) {
            return;
        }

        event.setCancelled(true);
    }

    
    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final LeavesDecayEvent event) {
        final Block block = event.getBlock();
        final Zone zone = zoneManager.get(block);

        if (zone == null) {
            return;
        }

        if (config.isTrackBlockChanges()) {
            zone.track(event.getBlock());
        }
        
        if (!config.isPreventLeafDecay()) {
            return;
        }

        event.setCancelled(true);
    }

    
    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final BlockPhysicsEvent event) {
        if (!config.isTrackBlockChanges()) {
            return;
        }
        
        final Block block = event.getBlock();
        final Zone zone = zoneManager.get(block);

        if (zone == null) {
            return;
        }

        zone.track(event.getBlock());
    }

    
    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final BlockGrowEvent event) {
        if (!config.isTrackBlockChanges()) {
            return;
        }
        
        final Block block = event.getBlock();
        final Zone zone = zoneManager.get(block);

        if (zone == null) {
            return;
        }

        zone.track(event.getBlock());
    }

    
    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final BlockFromToEvent event) {
        if (!config.isTrackBlockChanges()) {
            return;
        }
        
        final Block block = event.getBlock();
        final Zone zone = zoneManager.get(block);

        if (zone == null) {
            return;
        }

        zone.track(event.getBlock());
    }
}
