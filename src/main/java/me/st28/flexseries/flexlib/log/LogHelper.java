/**
 * FlexLib - Licensed under the MIT License (MIT)
 *
 * Copyright (c) Stealth2800 <http://stealthyone.com/>
 * Copyright (c) contributors <https://github.com/FlexSeries>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.st28.flexseries.flexlib.log;

import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.plugin.module.FlexModule;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class LogHelper {

    private static final String PREFIX_DEBUG = "DEBUG";

    private LogHelper() {}

    public static void log(FlexPlugin plugin, String identifier, String suffix, Level level, String message, Exception exception) {
        Validate.notNull(plugin, "Plugin cannot be null.");
        Validate.notNull(level, "Level cannot be null.");
        Validate.notNull(message, "Message cannot be null.");

        StringBuilder prefix = new StringBuilder();

        prefix.append("[");
        prefix.append(plugin.getName());
        if (identifier != null) {
            prefix.append("/").append(identifier);
        }

        if (suffix != null) {
            prefix.append(" ").append(suffix);
        }

        prefix.append("]");

        if (exception == null) {
            Bukkit.getLogger().log(level, prefix.toString() + " " + message);
        } else {
            Bukkit.getLogger().log(level, prefix.toString() + " " + message, exception);
        }
    }

    private static void logFromClass(Level level, Class clazz, String message) {
        Validate.notNull(clazz, "Class cannot be null.");
        Validate.notNull(message, "Message cannot be null.");

        if (FlexPlugin.class.isAssignableFrom(clazz)) {
            JavaPlugin plugin = JavaPlugin.getPlugin(clazz);

            if (plugin == null) {
                throw new IllegalArgumentException("Plugin '" + clazz.getCanonicalName() + "' not found.");
            }

            if (!(plugin instanceof FlexPlugin)) {
                throw new IllegalArgumentException("Plugin '" + plugin.getName() + "' is not a FlexPlugin.");
            }

            FlexPlugin fp = (FlexPlugin) plugin;

            if (level == Level.INFO) {
                info(fp, message);
            } else if (level == Level.WARNING) {
                warning(fp, message);
            } else if (level == Level.SEVERE) {
                severe(fp, message);
            } else if (level == null) {
                debug(fp, message);
            }
            throw new IllegalArgumentException("Invalid log level '" + level.getName() + "'");
        }

        if (FlexModule.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Invalid log level '" + level.getName() + "'");
        }

        throw new IllegalArgumentException("Class '" + clazz.getCanonicalName() + "' must be an instance of FlexPlugin or FlexModule.");
    }

    // ------------------------------------------------------------------------------------------ //

    public static void debug(FlexPlugin plugin, String message) {
        if (plugin.getConfig().getBoolean("debug", false)) {
            log(plugin, null, PREFIX_DEBUG, Level.INFO, message, null);
        }
    }

    public static void debug(FlexModule module, String message) {
        if (module.getPlugin().getConfig().getBoolean("debug", false)) {
            log(module.getPlugin(), module.getName(), PREFIX_DEBUG, Level.INFO, message, null);
        }
    }

    public static void debug(FlexModule module, String message, Exception exception) {
        if (module.getPlugin().getConfig().getBoolean("debug", false)) {
            log(module.getPlugin(), module.getName(), PREFIX_DEBUG, Level.INFO, message, exception);
        }
    }

    // ------------------------------------------------------------------------------------------ //


    public static void info(FlexPlugin plugin, String message) {
        log(plugin, null, null, Level.INFO, message, null);
    }

    public static void info(FlexModule module, String message) {
        log(module.getPlugin(), module.getName(), null, Level.INFO, message, null);
    }

    public static void info(FlexModule module, String message, Exception exception) {
        log(module.getPlugin(), module.getName(), null, Level.INFO, message, exception);
    }

    // ------------------------------------------------------------------------------------------ //

    public static void warning(FlexPlugin plugin, String message) {
        log(plugin, null, null, Level.WARNING, message, null);
    }

    public static void warning(FlexPlugin plugin, String message, Exception exception) {
        log(plugin, null, null, Level.WARNING, message, exception);
    }

    public static void warning(FlexModule module, String message) {
        log(module.getPlugin(), module.getName(), null, Level.WARNING, message, null);
    }

    public static void warning(FlexModule module, String message, Exception exception) {
        log(module.getPlugin(), module.getName(), null, Level.WARNING, message, exception);
    }

    // ------------------------------------------------------------------------------------------ //

    public static void severe(FlexPlugin plugin, String message) {
        log(plugin, null, null, Level.SEVERE, message, null);
    }

    public static void severe(FlexPlugin plugin, String message, Exception exception) {
        log(plugin, null, null, Level.SEVERE, message, exception);
    }

    public static void severe(FlexModule module, String message) {
        log(module.getPlugin(), module.getName(), null, Level.SEVERE, message, null);
    }

    public static void severe(FlexModule module, String message, Exception exception) {
        log(module.getPlugin(), module.getName(), null, Level.SEVERE, message, exception);
    }

}