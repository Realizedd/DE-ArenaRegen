package me.realized.de.arenaregen;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import me.realized.de.arenaregen.util.BlockInfo;
import me.realized.de.arenaregen.util.NMSUtil;
import me.realized.de.arenaregen.util.Position;
import me.realized.duels.api.arena.Arena;
import me.realized.duels.api.arena.ArenaManager;
import me.realized.duels.api.command.SubCommand;
import me.realized.duels.api.event.match.MatchEndEvent;
import me.realized.duels.api.event.match.MatchStartEvent;
import me.realized.duels.api.extension.DuelsExtension;
import me.realized.duels.api.match.Match;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;

// TODO: 08/08/2018 Clear dropped items in min/max zone!
public class ArenaRegen extends DuelsExtension implements Listener {

    private WorldGuardPlugin worldGuard;
    private ProtectedCuboidRegion region;
    private ArenaManager arenaManager;

    private final Map<Match, BlockChange> changes = new HashMap<>();

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

        this.arenaManager = api.getArenaManager();
        api.registerListener(this);
    }

    @Override
    public void onDisable() {
        changes.clear();
    }

    @Override
    public String getRequiredVersion() {
        return "3.1.2";
    }

    @EventHandler
    public void on(final MatchStartEvent event) {
        final BlockChange change = new BlockChange();
        changes.put(event.getMatch(), change);

        final Set<Chunk> chunks = new HashSet<>();
        final BlockVector min = region.getMinimumPoint();
        final BlockVector max = region.getMaximumPoint();

        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    final Block block = Bukkit.getWorlds().get(0).getBlockAt(x, y, z);
                    chunks.add(block.getChunk());

                    if (!block.getType().name().contains("AIR")) {
                        continue;
                    }

                    change.setSpace(new Position(block));
                    System.out.println("Added space " + block.getLocation());
                }
            }
        }

        update(chunks);
    }

    private void handleChange(final BlockChange change, final Block block) {
        if (change == null) {
            return;
        }

        change.setModified(new Position(block), new BlockInfo(block));
    }

    private BlockChange get(final Player player) {
        final Arena arena = arenaManager.get(player);

        if (arena == null) {
            return null;
        }

        return arena.getMatch() != null ? changes.get(arena.getMatch()) : null;
    }

    private BlockChange get(final Position position) {
        return changes.values().stream().filter(change -> region.contains(new Vector(position.getX(), position.getY(), position.getZ()))).findFirst().orElse(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(final BlockBreakEvent event) {
        handleChange(get(event.getPlayer()), event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(final BlockFromToEvent event) {
        final Block toBlock = event.getToBlock();

        if (toBlock.isLiquid()) {
            return;
        }

        handleChange(get(new Position(toBlock)), toBlock);
    }

    @EventHandler
    public void on(final MatchEndEvent event) {
        final BlockChange change = changes.remove(event.getMatch());

        if (change == null) {
            return;
        }

        final World world = Bukkit.getWorlds().get(0);

        change.getSpaces().forEach(position -> {
            final Block block = world.getBlockAt(position.getX(), position.getY(), position.getZ());

            if (!block.getType().name().contains("AIR")) {
                NMSUtil.setBlockFast(block, Material.AIR, 0);
                System.out.println("Setting air " + block.getLocation());
            }
        });
        // TODO: 08/08/2018 No lighting updates for AIR & Check if block is affected by lighting before calling c!
        change.getChanges()
            .forEach((position, info) -> NMSUtil.setBlockFast(world.getBlockAt(position.getX(), position.getY(), position.getZ()), info.getType(), info.getData()));
    }

    private void update(final Set<Chunk> chunks) {
        chunks.forEach(chunk -> chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ()));
    }
}
