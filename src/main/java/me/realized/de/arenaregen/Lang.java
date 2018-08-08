package me.realized.de.arenaregen;

import java.text.MessageFormat;
import me.realized.de.arenaregen.util.StringUtil;
import org.bukkit.command.CommandSender;

public enum Lang {

    PLAYER_ONLY("&cThis command is player only!"),
    INVALID_COMMAND("&c''{0}'' is not a valid command. Please type &f/{1} &cfor help."),
    NO_SELECTION("&cYou must select two points with an iron axe to create a reset zone."),
    ALREADY_EXISTS("&cArena ''{0}'' already has a reset zone set. To delete {0}''s reset zone, use the command ''/ds ar delete {0}''"),
    ARENA_NOT_FOUND("&c''{0}'' is not an existing arena."),

    HELP_HEADER("&9&m------------- &fLeaderboards &9&m-------------", false),
    HELP_FORMAT("&f/{0} &e- &7{1}", false),
    HELP_FOOTER("&9&m----------------------------------------", false),
    USAGE_FORMAT("&f/{0} &e- &7{1}"),
    POS_SET("{0} position set at &f{1}&7."),
    CREATED("Arena &f{0} &7will now reset automatically when a duel ends.");

    private final MessageFormat message;

    Lang(final String message, final boolean prefix) {
        this.message = new MessageFormat(StringUtil.color((prefix ? "&9[Duels] &7" : "") + message));
    }

    Lang(final String message) {
        this(message, true);
    }

    public String format(final Object... parameters) {
        return message.format(parameters);
    }

    public void sendTo(final CommandSender sender, final Object... parameters) {
        sender.sendMessage(format(parameters));
    }
}