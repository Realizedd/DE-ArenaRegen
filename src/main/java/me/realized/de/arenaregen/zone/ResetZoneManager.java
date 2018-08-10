package me.realized.de.arenaregen.zone;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.Lang;
import me.realized.de.arenaregen.config.Config;
import me.realized.de.arenaregen.util.StringUtil;
import me.realized.duels.api.Duels;
import me.realized.duels.api.arena.ArenaManager;
import me.realized.duels.api.event.arena.ArenaRemoveEvent;
import me.realized.duels.api.event.match.MatchEndEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class ResetZoneManager {

    private final ArenaRegen extension;
    private final Duels api;
    private final ArenaManager arenaManager;

    private final Config config;
    private final File folder;

    private final Map<String, ResetZone> zones = new HashMap<>();
    private final Map<UUID, Selection> selections = new HashMap<>();

    public ResetZoneManager(final ArenaRegen extension, final Duels api) {
        this.extension = extension;
        this.api = api;
        this.arenaManager = api.getArenaManager();
        this.config = extension.getConfiguration();
        this.folder = new File(extension.getDataFolder(), "zones");
        api.registerListener(new ResetZoneListener());

        if (!folder.exists()) {
            folder.mkdir();
        }

        final File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files != null) {
            for (final File file : files) {
                final String name = file.getName().replace(".yml", "");

                if (arenaManager.get(name) == null) {
                    file.delete();
                    continue;
                }

                try {
                    zones.put(name, new ResetZone(extension, api, name, file));
                } catch (Exception ex) {
                    extension.error("Could not load reset zone '" + name + "'!", ex);
                }
            }
        }
    }

    public void save() {
        zones.values().forEach(zone -> {
            try {
                zone.save();
            } catch (IOException ex) {
                extension.error("Could not save reset zone '" + zone.getName() + "'!", ex);
            }
        });
    }

    public Selection get(final Player player) {
        return selections.get(player.getUniqueId());
    }

    public ResetZone get(final String name) {
        return zones.get(name);
    }

    public boolean create(final String name, final Selection selection) {
        if (zones.containsKey(name)) {
            return false;
        }

        final ResetZone zone = new ResetZone(extension, api, name, folder, selection.getFirst(), selection.getSecond());
        zone.loadBlocks();
        zones.put(name, zone);
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

        @EventHandler
        public void on(final PlayerInteractEvent event) {
            if (!(event.hasItem() && event.hasBlock())) {
                return;
            }

            final ItemStack item = event.getItem();

            if (item.getType() != config.getSelectingTool()) {
                return;
            }

            final Player player = event.getPlayer();

            if (!player.hasPermission("duels.admin")) {
                return;
            }

            event.setCancelled(true);

            final Selection selection = selections.computeIfAbsent(player.getUniqueId(), result -> new Selection());

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                selection.setFirst(event.getClickedBlock().getLocation().clone());
                Lang.POS_SET.sendTo(player, "First", StringUtil.from(selection.getFirst()));
            } else {
                selection.setSecond(event.getClickedBlock().getLocation().clone());
                Lang.POS_SET.sendTo(player, "Second", StringUtil.from(selection.getSecond()));
            }
        }

        @EventHandler
        public void on(final PlayerQuitEvent event) {
            selections.remove(event.getPlayer().getUniqueId());
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
            Lang.BLOCK_ARENA_BLOCK_BREAK.sendTo(player);
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

        @EventHandler
        public void on(final MatchEndEvent event) {
            final ResetZone zone = get(event.getMatch().getArena().getName());

            if (zone == null) {
                return;
            }

            zone.reset();
        }
    }

    public class Selection {

        @Getter
        @Setter
        private Location first, second;

        Selection() {}

        public boolean isSelected() {
            return first != null && second != null;
        }
    }
}
