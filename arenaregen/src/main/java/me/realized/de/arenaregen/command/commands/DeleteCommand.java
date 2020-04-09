package me.realized.de.arenaregen.command.commands;

import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.Lang;
import me.realized.de.arenaregen.command.ARCommand;
import me.realized.duels.api.Duels;
import me.realized.duels.api.arena.Arena;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

public class DeleteCommand extends ARCommand {

    public DeleteCommand(final ArenaRegen extension, final Duels api) {
        super(extension, api, "delete", "delete [arena]", "Deletes the reset zone for arena.", 3, false);
    }

    @Override
    public void execute(final CommandSender sender, final String label, final String[] args) {
        final String name = StringUtils.join(args, " ", 2, args.length);
        final Arena arena = arenaManager.get(name);

        if (arena == null) {
            Lang.ARENA_NOT_FOUND.sendTo(sender, name);
            return;
        }

        if (!zoneManager.remove(name)) {
            Lang.ZONE_NOT_FOUND.sendTo(sender, name);
            return;
        }

        Lang.DELETED.sendTo(sender, name);
    }
}
