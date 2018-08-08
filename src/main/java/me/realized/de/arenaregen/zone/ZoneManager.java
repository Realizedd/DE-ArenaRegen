package me.realized.de.arenaregen.zone;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.Lang;
import me.realized.de.arenaregen.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class ZoneManager implements Listener {

    // TODO: 08/08/2018 Config define
    private final Material configMaterial = Material.IRON_HOE;

    private final ArenaRegen extension;

    private final Map<String, ResetZone> zones = new HashMap<>();
    private final Map<UUID, Selection> selections = new HashMap<>();

    public ZoneManager(final ArenaRegen extension) {
        this.extension = extension;
    }

    public Selection get(final Player player) {
        return selections.get(player.getUniqueId());
    }

    public ResetZone get(final String name) {
        return zones.get(name);
    }

    public void create(final String name, final Selection selection) {
        zones.put(name, new ResetZone(name, selection.getFirst(), selection.getSecond()));
    }

    @EventHandler
    public void on(final PlayerInteractEvent event) {
        if (!(event.hasItem() && event.hasBlock())) {
            return;
        }

        final ItemStack item = event.getItem();

        if (item.getType() != configMaterial) {
            return;
        }

        final Player player = event.getPlayer();

        if (!player.hasPermission("duels.admin")) {
            return;
        }

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
