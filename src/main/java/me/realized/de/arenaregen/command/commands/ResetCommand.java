package me.realized.de.arenaregen.command.commands;

import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.Lang;
import me.realized.de.arenaregen.command.ARCommand;
import me.realized.de.arenaregen.zone.ResetZone;
import me.realized.duels.api.Duels;
import me.realized.duels.api.arena.Arena;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

public class ResetCommand extends ARCommand {

    public ResetCommand(final ArenaRegen extension, final Duels api) {
        super(extension, api, "reset", "reset [arena]", "Resets the reset zone for arena.", 3, false);
    }

    @Override
    public void execute(final CommandSender sender, final String label, final String[] args) {
        final String name = StringUtils.join(args, " ", 2, args.length);
        final Arena arena = arenaManager.get(name);

        if (arena == null) {
            Lang.ARENA_NOT_FOUND.sendTo(sender, name);
            return;
        }

        final ResetZone zone = zoneManager.get(name);

        if (zone == null) {
            Lang.ZONE_NOT_FOUND.sendTo(sender, name);
            return;
        }

        zone.reset();
        Lang.RESET.sendTo(sender, name);
    }
}
