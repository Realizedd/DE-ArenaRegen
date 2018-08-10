package me.realized.de.arenaregen.command;

import lombok.Getter;
import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.zone.ResetZoneManager;
import me.realized.duels.api.Duels;
import me.realized.duels.api.arena.ArenaManager;
import org.bukkit.command.CommandSender;

public abstract class ARCommand {

    protected final ArenaManager arenaManager;
    protected final ResetZoneManager zoneManager;

    @Getter
    private final String name;
    @Getter
    private final String usage;
    @Getter
    private final String description;
    @Getter
    private final int length;
    @Getter
    private final boolean playerOnly;

    protected ARCommand(final ArenaRegen extension, final Duels api, final String name, final String usage, final String description, final int length, final boolean playerOnly) {
        this.arenaManager = api.getArenaManager();
        this.zoneManager = extension.getZoneManager();
        this.name = name;
        this.usage = usage;
        this.description = description;
        this.length = length;
        this.playerOnly = playerOnly;
    }

    public abstract void execute(final CommandSender sender, final String label, final String[] args);
}
