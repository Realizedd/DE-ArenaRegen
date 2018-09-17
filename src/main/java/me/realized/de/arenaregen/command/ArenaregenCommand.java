package me.realized.de.arenaregen.command;

import java.util.LinkedHashMap;
import java.util.Map;
import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.Lang;
import me.realized.de.arenaregen.command.commands.CreateCommand;
import me.realized.de.arenaregen.command.commands.DeleteCommand;
import me.realized.de.arenaregen.command.commands.ListCommand;
import me.realized.de.arenaregen.command.commands.ResetCommand;
import me.realized.duels.api.Duels;
import me.realized.duels.api.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaregenCommand extends SubCommand {

    private final Map<String, ARCommand> commands = new LinkedHashMap<>();

    public ArenaregenCommand(final ArenaRegen extension, final Duels api) {
        super("arenaregen", null, null, null, false, 1, "ar");
        register(
            new CreateCommand(extension, api),
            new DeleteCommand(extension, api),
            new ResetCommand(extension, api),
            new ListCommand(extension, api)
        );
    }

    private void register(final ARCommand... commands) {
        for (final ARCommand command : commands) {
            this.commands.put(command.getName(), command);
        }
    }

    @Override
    public void execute(final CommandSender sender, final String label, final String[] args) {
        if (args.length == getLength()) {
            Lang.HELP_HEADER.sendTo(sender);
            commands.values().forEach(command -> Lang.HELP_FORMAT.sendTo(sender, label + " " + args[0] + " " + command.getUsage(), command.getDescription()));
            Lang.HELP_FOOTER.sendTo(sender);
            return;
        }

        final ARCommand command = commands.get(args[1].toLowerCase());

        if (command != null) {
            if (command.isPlayerOnly() && !(sender instanceof Player)) {
                Lang.PLAYER_ONLY.sendTo(sender);
                return;
            }

            if (args.length < command.getLength()) {
                Lang.USAGE_FORMAT.sendTo(sender, label + " " + args[0] + " " + command.getUsage(), command.getDescription());
                return;
            }

            command.execute(sender, label, args);
            return;
        }

        Lang.INVALID_COMMAND.sendTo(sender, args[1], label + " " + args[0]);
    }
}