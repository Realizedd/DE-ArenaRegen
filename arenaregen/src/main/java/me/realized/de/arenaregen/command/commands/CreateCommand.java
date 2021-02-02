package me.realized.de.arenaregen.command.commands;

import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.command.ARCommand;
import me.realized.de.arenaregen.selection.Selection;
import me.realized.duels.api.Duels;
import me.realized.duels.api.arena.Arena;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateCommand extends ARCommand {

    public CreateCommand(final ArenaRegen extension, final Duels api) {
        super(extension, api, "create", "create [arena]", "Creates a reset zone for arena.", 3, true);
    }

    @Override
    public void execute(final CommandSender sender, final String label, final String[] args) {
        final String name = StringUtils.join(args, " ", 2, args.length);
        final Arena arena = arenaManager.get(name);

        if (arena == null) {
            lang.sendMessage(sender, "ERROR.arena-not-found", "name", name);
            return;
        }

        final Player player = (Player) sender;
        final Selection selection = selectionManager.get(player);

        if (selection == null || !selection.isSelected()) {
            lang.sendMessage(sender, "ERROR.no-selection");
            return;
        }

        if (!zoneManager.create(arena, selection)) {
            lang.sendMessage(sender, "ERROR.already-exists", "name", name);
            return;
        }

        lang.sendMessage(sender, "COMMAND.arenaregen.create", "name", name);
    }
}
