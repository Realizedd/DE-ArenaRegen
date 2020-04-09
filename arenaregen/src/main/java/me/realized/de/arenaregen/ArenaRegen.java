package me.realized.de.arenaregen;

import lombok.Getter;
import me.realized.de.arenaregen.command.ArenaregenCommand;
import me.realized.de.arenaregen.config.Config;
import me.realized.de.arenaregen.nms.NMS;
import me.realized.de.arenaregen.nms.fallback.NMSHandler;
import me.realized.de.arenaregen.util.CompatUtil;
import me.realized.de.arenaregen.util.ReflectionUtil;
import me.realized.de.arenaregen.zone.ResetZoneManager;
import me.realized.duels.api.extension.DuelsExtension;

public class ArenaRegen extends DuelsExtension {

    @Getter
    private Config configuration;

    @Getter
    private NMS handler;

    @Getter
    private ResetZoneManager zoneManager;

    @Override
    public void onEnable() {
        this.configuration = new Config(this);

        final String packageName = api.getServer().getClass().getPackage().getName();
        final String version = packageName.substring(packageName.lastIndexOf('.') + 1);

        try {
            Class<?> clazz = null;

            if (CompatUtil.isPaper()) {
                clazz = ReflectionUtil.getClassUnsafe("me.realized.de.arenaregen.nms." + version + "_paper" + ".NMSHandler");
            }

            if (clazz == null) {
                clazz = Class.forName("me.realized.de.arenaregen.nms." + version + ".NMSHandler");
            }

            this.handler = NMS.class.isAssignableFrom(clazz) ? (NMS) clazz.newInstance() : new NMSHandler();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            this.handler = new NMSHandler();
        }

        info("NMSHandler: Using " + handler.getClass().getName());

        this.zoneManager = new ResetZoneManager(this, api);
        api.registerSubCommand("duels", new ArenaregenCommand(this, api));
    }

    @Override
    public void onDisable() {
        zoneManager.save();
    }

    @Override
    public String getRequiredVersion() {
        return "3.3.0";
    }

    public void info(final String s) {
        api.info("[" + getName() + " Extension] " + s);
    }

    public void error(final String s, final Throwable thrown) {
        api.error("[" + getName() + " Extension] " + s, thrown);
    }
}
