package me.realized.de.arenaregen;

import lombok.Getter;
import me.realized.de.arenaregen.command.ArenaregenCommand;
import me.realized.de.arenaregen.config.Config;
import me.realized.de.arenaregen.config.Lang;
import me.realized.de.arenaregen.nms.NMS;
import me.realized.de.arenaregen.nms.fallback.NMSHandler;
import me.realized.de.arenaregen.selection.SelectionManager;
import me.realized.de.arenaregen.util.CompatUtil;
import me.realized.de.arenaregen.util.ReflectionUtil;
import me.realized.de.arenaregen.zone.ZoneManager;
import me.realized.duels.api.extension.DuelsExtension;

public class ArenaRegen extends DuelsExtension {

    @Getter
    private Config configuration;
    @Getter
    private Lang lang;
    @Getter
    private NMS handler;
    @Getter
    private SelectionManager selectionManager;
    @Getter
    private ZoneManager zoneManager;

    @Override
    public void onEnable() {
        this.configuration = new Config(this);
        this.lang = new Lang(this);
        this.handler = findHandler();

        info("NMSHandler: Using " + handler.getClass().getName());

        this.selectionManager = new SelectionManager(this, api);
        this.zoneManager = new ZoneManager(this, api);
        api.registerSubCommand("duels", new ArenaregenCommand(this, api));
    }

    private NMS findHandler() {
        final String handlerVersion = configuration.getBlockResetHandlerVersion();
        final String packageName = api.getServer().getClass().getPackage().getName();
        String version = packageName.substring(packageName.lastIndexOf('.') + 1);

        if (!handlerVersion.equalsIgnoreCase("auto")) {
            if (handlerVersion.equalsIgnoreCase("fallback")) {
                return new NMSHandler();
            }

            version = handlerVersion;
        }

        try {
            Class<?> clazz = null;

            if (CompatUtil.isPaper()) {
                clazz = ReflectionUtil.getClassUnsafe("me.realized.de.arenaregen.nms." + version + "_paper" + ".NMSHandler");
            }

            if (clazz == null) {
                clazz = ReflectionUtil.getClassUnsafe("me.realized.de.arenaregen.nms." + version + ".NMSHandler");
            }

            return clazz != null && NMS.class.isAssignableFrom(clazz) ? (NMS) clazz.newInstance() : new NMSHandler();
        } catch (IllegalAccessException | InstantiationException ex) {
            return new NMSHandler();
        }
    }

    @Override
    public void onDisable() {
        zoneManager.handleDisable();
    }

    @Override
    public String getRequiredVersion() {
        return "3.5.0";
    }

    public void info(final String s) {
        api.info("[" + getName() + " Extension] " + s);
    }

    public void error(final String s) {
        api.error("[" + getName() + " Extension] " + s);
    }

    public void error(final String s, final Throwable thrown) {
        api.error("[" + getName() + " Extension] " + s, thrown);
    }
}
