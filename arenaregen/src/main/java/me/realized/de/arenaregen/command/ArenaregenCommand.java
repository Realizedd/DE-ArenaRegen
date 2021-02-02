package me.realized.de.arenaregen.command;

import java.util.LinkedHashMap;
import java.util.Map;
import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.command.commands.CreateCommand;
import me.realized.de.arenaregen.command.commands.DeleteCommand;
import me.realized.de.arenaregen.command.commands.ListCommand;
import me.realized.de.arenaregen.command.commands.ResetCommand;
import me.realized.de.arenaregen.config.Lang;
import me.realized.de.arenaregen.util.StringUtil;
import me.realized.duels.api.Duels;
import me.realized.duels.api.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaregenCommand extends SubCommand {

    private static final String PLAYER_ONLY_MESSAGE = "&cThis command is player only!";

    private final Lang lang;
    private final Map<String, ARCommand> commands = new LinkedHashMap<>();

    public ArenaregenCommand(final ArenaRegen extension, final Duels api) {
        super("arenaregen", null, null, null, false, 1, "ar");
        this.lang = extension.getLang();

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
        final String cmdName = label + " " + args[0];

        if (args.length == getLength()) {
            lang.sendMessage(sender, "COMMAND.arenaregen.usage", "command", cmdName);
            return;
        }

        final ARCommand command = commands.get(args[1].toLowerCase());

        if (command != null) {
            if (command.isPlayerOnly() && !(sender instanceof Player)) {
                sender.sendMessage(StringUtil.color(PLAYER_ONLY_MESSAGE));
                return;
            }

            if (args.length < command.getLength()) {
                lang.sendMessage(sender, "COMMAND.sub-command-usage", "command", cmdName, "usage", command.getUsage(), "description", command.getDescription());
                return;
            }

            command.execute(sender, label, args);
            return;
        }

        lang.sendMessage(sender, "ERROR.invalid-sub-command", "argument", args[1], "command", cmdName);
    }
}