package me.realized.de.arenaregen;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import me.realized.de.arenaregen.command.ArenaregenCommand;
import me.realized.de.arenaregen.util.compat.NMSUtil;
import me.realized.de.arenaregen.zone.ResetZone;
import me.realized.de.arenaregen.zone.ZoneManager;
import me.realized.duels.api.command.SubCommand;
import me.realized.duels.api.event.match.MatchEndEvent;
import me.realized.duels.api.extension.DuelsExtension;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;

public class ArenaRegen extends DuelsExtension {

    private WorldGuardPlugin worldGuard;
    private ProtectedCuboidRegion region;

    @Getter
    private ZoneManager zoneManager;

    private ResetZone zone;

    @Override
    public void onEnable() {
        api.registerSubCommand("duels", new SubCommand("test", null, null, null, true, 2) {
            @Override
            public void execute(final CommandSender commandSender, final String s, final String[] strings) {
                final Player player = (Player) commandSender;
                final Location location = player.getLocation();
                final Set<Chunk> chunks = new HashSet<>();

                for (int i = 0; i < 20; i++) {
                    final Location blockLoc = location.clone().add(i, 0, i);
                    NMSUtil.setBlockFast(blockLoc.getBlock(), Material.getMaterial(strings[1]), 0);
                    chunks.add(blockLoc.getChunk());
                }

                update(chunks);
                player.sendMessage("done!");
            }
        });

        this.worldGuard = (WorldGuardPlugin) api.getServer().getPluginManager().getPlugin("WorldGuard");
        this.region = (ProtectedCuboidRegion) worldGuard.getRegionManager(Bukkit.getWorlds().get(0)).getRegion("arena");

        if (region == null) {
            return;
        }

        api.registerSubCommand("duels", new ArenaregenCommand(this, api));

        final BlockVector min = region.getMinimumPoint();
        final BlockVector max = region.getMaximumPoint();
        final Location first = new Location(Bukkit.getWorlds().get(0), min.getBlockX(), min.getBlockY(), min.getBlockZ());
        final Location second = new Location(Bukkit.getWorlds().get(0), max.getBlockX(), max.getBlockY(), max.getBlockZ());
        zone = new ResetZone("boi", first, second);
        api.registerListener(this.zoneManager = new ZoneManager(this));
        api.registerListener(new RegenListener());
    }

    @Override
    public void onDisable() {

    }

    @Override
    public String getRequiredVersion() {
        return "3.1.2";
    }

    private void update(final Set<Chunk> chunks) {
        chunks.forEach(chunk -> chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ()));
    }

    private class RegenListener implements Listener {

        private boolean allowArenaBlockBreak = false;

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void on(final BlockBreakEvent event) {
            final Block block = event.getBlock();

            if (allowArenaBlockBreak || !zone.isCached(block)) {
                return;
            }

            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Can't break the arena!");
        }

        private boolean preventBlockMelt = true;

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void on(final BlockFadeEvent event) {
            final Block block = event.getBlock();

            if (!zone.contains(block)) {
                return;
            }

            final Material changedType = event.getNewState().getType();

            if (!(preventBlockMelt && (changedType == Material.AIR || changedType.name().contains("WATER")))) {
                return;
            }

            event.setCancelled(true);
        }

        private boolean preventBlockBurn = true;

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void on(final BlockBurnEvent event) {
            final Block block = event.getBlock();

            if (!preventBlockBurn || !zone.contains(block)) {
                return;
            }

            event.setCancelled(true);
        }

        private boolean preventFireSpread = true;

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void on(final BlockIgniteEvent event) {
            if (!preventFireSpread || event.getCause() != IgniteCause.SPREAD || !zone.contains(event.getBlock())) {
                return;
            }

            event.setCancelled(true);
        }

        @EventHandler
        public void on(final MatchEndEvent event) {
            if (!event.getMatch().getArena().getName().equals(zone.getName())) {
                return;
            }

            final long start = System.nanoTime();
            System.out.println("Resetting...");
            zone.reset();
            System.out.println("Done. Took " + TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS) + "ms");
        }
    }
}
