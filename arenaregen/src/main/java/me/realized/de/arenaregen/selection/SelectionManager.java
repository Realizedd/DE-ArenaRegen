package me.realized.de.arenaregen.selection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.config.Config;
import me.realized.de.arenaregen.config.Lang;
import me.realized.de.arenaregen.util.StringUtil;
import me.realized.duels.api.Duels;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class SelectionManager {

    private final Config config;
    private final Lang lang;

    private final Map<UUID, Selection> selections = new HashMap<>();

    public SelectionManager(final ArenaRegen extension, final Duels api) {
        this.config = extension.getConfiguration();
        this.lang = extension.getLang();
        api.registerListener(new SelectionListener());
    }


    public Selection get(final Player player) {
        return selections.get(player.getUniqueId());
    }

    private class SelectionListener implements Listener {

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
            final Location location = event.getClickedBlock().getLocation().clone();
            final String pos;

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                selection.setFirst(location);
                pos = "First";
            } else {
                selection.setSecond(location);
                pos = "Second";
            }

            lang.sendMessage(player, "SELECTION.pos-set", "pos", pos, "location", StringUtil.from(location));
        }

        @EventHandler
        public void on(final PlayerQuitEvent event) {
            selections.remove(event.getPlayer().getUniqueId());
        }
    }
}
