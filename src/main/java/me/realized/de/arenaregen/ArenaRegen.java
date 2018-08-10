package me.realized.de.arenaregen;

import lombok.Getter;
import me.realized.de.arenaregen.command.ArenaregenCommand;
import me.realized.de.arenaregen.config.Config;
import me.realized.de.arenaregen.zone.ResetZoneManager;
import me.realized.duels.api.extension.DuelsExtension;

public class ArenaRegen extends DuelsExtension {

    @Getter
    private Config configuration;

    @Getter
    private ResetZoneManager zoneManager;

    @Override
    public void onEnable() {
        this.configuration = new Config(this);
        this.zoneManager = new ResetZoneManager(this, api);
        api.registerSubCommand("duels", new ArenaregenCommand(this, api));
    }

    @Override
    public void onDisable() {
        zoneManager.save();
    }

    @Override
    public String getRequiredVersion() {
        return "3.1.2";
    }

    public void error(final String s, final Throwable thrown) {
        api.error("[" + getName() + " Extension] " + s, thrown);
    }
}
