package me.realized.de.arenaregen.command.commands;

import me.realized.de.arenaregen.ArenaRegen;
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
            lang.sendMessage(sender, "ERROR.arena-not-found", "name", name);
            return;
        }

        if (!zoneManager.remove(name)) {
            lang.sendMessage(sender, "ERROR.zone-not-found", "name", name);
            return;
        }

        lang.sendMessage(sender, "COMMAND.arenaregen.delete", "name", name);
    }
}
