package me.realized.de.arenaregen.command.commands;

import java.util.Collection;
import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.Lang;
import me.realized.de.arenaregen.command.ARCommand;
import me.realized.de.arenaregen.util.StringUtil;
import me.realized.de.arenaregen.zone.ResetZone;
import me.realized.duels.api.Duels;
import org.bukkit.command.CommandSender;

public class ListCommand extends ARCommand {

    public ListCommand(final ArenaRegen extension, final Duels api) {
        super(extension, api, "list", "list", "Lists available reset zones.", 2, false);
    }

    @Override
    public void execute(final CommandSender sender, final String label, final String[] args) {
        final Collection<ResetZone> zones = zoneManager.getZones();

        if (zones.isEmpty()) {
            Lang.NO_ACTIVE_ZONES.sendTo(sender);
            return;
        }

        Lang.LIST_HEADER.sendTo(sender);
        zones.forEach(zone -> Lang.LIST_FORMAT.sendTo(sender, zone.getName(), StringUtil.from(zone.getMin()), StringUtil.from(zone.getMax()), zone.getTotalBlocks()));
        Lang.LIST_FOOTER.sendTo(sender, zones.size());
    }
}
