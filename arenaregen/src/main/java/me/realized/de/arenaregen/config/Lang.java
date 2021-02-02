package me.realized.de.arenaregen.config;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import me.realized.de.arenaregen.ArenaRegen;
import me.realized.de.arenaregen.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Lang {

    private static final String NAME = "lang.yml";

    private final ArenaRegen extension;
    private final Map<String, String> messages = new HashMap<>();

    public Lang(final ArenaRegen extension) {
        this.extension = extension;

        final File file = new File(extension.getDataFolder(), NAME);

        if (!file.exists()) {
            extension.saveResource(NAME);
        }

        final FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);


        final Map<String, String> strings = new HashMap<>();

        for (String key : configuration.getKeys(true)) {
            final Object value = configuration.get(key);

            if (value == null || value instanceof MemorySection) {
                continue;
            }

            final String message = value instanceof List ? StringUtil.fromList((List<?>) value) : value.toString();

            if (key.startsWith("STRINGS")) {
                final String[] args = key.split(Pattern.quote("."));
                strings.put(args[args.length - 1], message);
            } else {
                messages.put(key, message);
            }
        }

        messages.replaceAll((key, value) -> {
            for (final Map.Entry<String, String> entry : strings.entrySet()) {
                final String placeholder = "{" + entry.getKey() + "}";

                if (StringUtils.containsIgnoreCase(value, placeholder)) {
                    value = value.replaceAll("(?i)" + Pattern.quote(placeholder), entry.getValue());
                }
            }

            return value;
        });
    }

    private String getRawMessage(final String key) {
        final String message = messages.get(key);

        if (message == null) {
            extension.error("Failed to load message: provided key '" + key + "' has no assigned value");
            return null;
        }

        // Allow disabling any message by setting it to ''
        return !message.isEmpty() ? message : null;
    }

    private String replace(String message, Object... replacers) {
        if (replacers.length == 1 && replacers[0] instanceof Object[]) {
            replacers = (Object[]) replacers[0];
        }

        for (int i = 0; i < replacers.length; i += 2) {
            if (i + 1 >= replacers.length) {
                break;
            }

            message = message.replace("%" + replacers[i].toString() + "%", String.valueOf(replacers[i + 1]));
        }

        return message;
    }

    public void sendMessage(final CommandSender receiver, final String key, final Object... replacers) {
        final String message = getRawMessage(key);

        if (message == null) {
            return;
        }

        receiver.sendMessage(StringUtil.color(replace(message, replacers)));
    }
}
