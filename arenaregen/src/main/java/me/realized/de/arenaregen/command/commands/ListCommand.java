package me.realized.de.arenaregen.command.commands;

import java.util.Collection;
import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.command.ARCommand;
import me.realized.de.arenaregen.util.StringUtil;
import me.realized.de.arenaregen.zone.Zone;
import me.realized.duels.api.Duels;
import org.bukkit.command.CommandSender;

public class ListCommand extends ARCommand {

    public ListCommand(final ArenaRegen extension, final Duels api) {
        super(extension, api, "list", "list", "Lists available reset zones.", 2, false);
    }

    @Override
    public void execute(final CommandSender sender, final String label, final String[] args) {
        final Collection<Zone> zones = zoneManager.getZones();

        if (zones.isEmpty()) {
            lang.sendMessage(sender, "ERROR.no-active-zones");
            return;
        }

        lang.sendMessage(sender, "COMMAND.arenaregen.list.header");
        zones.forEach(zone -> lang.sendMessage(sender, "COMMAND.arenaregen.list.format",
            "name", zone.getName(), "min_pos", StringUtil.from(zone.getMin()), "max_pos", StringUtil.from(zone.getMax()), "blocks_count", zone.getTotalBlocks()));
        lang.sendMessage(sender, "COMMAND.arenaregen.list.footer", "zones_count", zones.size());
    }
}
