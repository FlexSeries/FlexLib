/**
 * Copyright 2016 Stealth2800 <http://stealthyone.com/>
 * Copyright 2016 Contributors <https://github.com/FlexSeries>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.st28.flexseries.flexlib.logging;

import me.st28.flexseries.flexlib.plugin.FlexModule;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class LogHelper {

    private static final String SUFFIX_DEBUG = "DEBUG";

    private LogHelper() { }

    private static void log(FlexPlugin plugin, String identifier, String suffix, Level level, String message, Exception exception) {
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

        plugin.getLogger().log(level, prefix.toString() + " " + message, exception);
    }

    private static void logFromClass(Class clazz, String suffix, Level level, String message, Exception exception) {
        if (FlexPlugin.class.isAssignableFrom(clazz)) {
            JavaPlugin plugin = JavaPlugin.getPlugin(clazz);

            if (plugin == null) {
                throw new IllegalArgumentException("Plugin '" + clazz.getCanonicalName() + "' not found");
            }

            if (!(plugin instanceof FlexPlugin)) {
                throw new IllegalArgumentException("Plugin '" + clazz.getCanonicalName() + "' is not a FlexPlugin");
            }

            FlexPlugin fp = (FlexPlugin) plugin;

            log(fp, null, suffix, level, message, exception);
        } else if (FlexModule.class.isAssignableFrom(clazz)) {
            // TODO: Implement
        }
    }

    // ------------------------------------------------------------------------------------------ //

    public static void debug(FlexPlugin plugin, String message) {
        debug(plugin, message, null);
    }

    public static void debug(FlexPlugin plugin, String message, Exception exception) {
        if (!plugin.isDebugEnabled()) {
            return;
        }
        log(plugin, null, SUFFIX_DEBUG, Level.INFO, message, exception);
    }

    public static void debug(FlexModule module, String message) {
        debug(module, message, null);
    }

    public static void debug(FlexModule module, String message, Exception exception) {
        if (!module.getPlugin().isDebugEnabled()) {
            return;
        }
        log(module.getPlugin(), module.getName(), SUFFIX_DEBUG, Level.INFO, message, exception);
    }

    public static void debug(Class moduleOrPlugin, String message) {
        debug(moduleOrPlugin, message, null);
    }

    public static void debug(Class moduleOrPlugin, String message, Exception exception) {
        // TODO: Check for debug mode
        logFromClass(moduleOrPlugin, SUFFIX_DEBUG, Level.INFO, message, exception);
    }

    //

    public static void info(FlexPlugin plugin, String message) {
        info(plugin, message, null);
    }

    public static void info(FlexPlugin plugin, String message, Exception exception) {
        log(plugin, null, null, Level.INFO, message, exception);
    }

    public static void info(FlexModule module, String message) {
        info(module, message, null);
    }

    public static void info(FlexModule module, String message, Exception exception) {
        log(module.getPlugin(), module.getName(), null, Level.INFO, message, exception);
    }

    public static void info(Class moduleOrPlugin, String message) {
        info(moduleOrPlugin, message, null);
    }

    public static void info(Class moduleOrPlugin, String message, Exception exception) {
        logFromClass(moduleOrPlugin, null, Level.INFO, message, exception);
    }

    //

    public static void warning(FlexPlugin plugin, String message) {
        warning(plugin, message, null);
    }

    public static void warning(FlexPlugin plugin, String message, Exception exception) {
        log(plugin, null, null, Level.WARNING, message, exception);
    }

    public static void warning(FlexModule module, String message) {
        warning(module, message, null);
    }

    public static void warning(FlexModule module, String message, Exception exception) {
        log(module.getPlugin(), module.getName(), null, Level.WARNING, message, exception);
    }

    public static void warning(Class moduleOrPlugin, String message) {
        warning(moduleOrPlugin, message, null);
    }

    public static void warning(Class moduleOrPlugin, String message, Exception exception) {
        logFromClass(moduleOrPlugin, null, Level.WARNING, message, exception);
    }

    //

    public static void severe(FlexPlugin plugin, String message) {
        severe(plugin, message, null);
    }

    public static void severe(FlexPlugin plugin, String message, Exception exception) {
        log(plugin, null, null, Level.SEVERE, message, exception);
    }

    public static void severe(FlexModule module, String message) {
        severe(module, message, null);
    }

    public static void severe(FlexModule module, String message, Exception exception) {
        log(module.getPlugin(), module.getName(), null, Level.SEVERE, message, exception);
    }

    public static void severe(Class moduleOrPlugin, String message) {
        severe(moduleOrPlugin, message, null);
    }

    public static void severe(Class moduleOrPlugin, String message, Exception exception) {
        logFromClass(moduleOrPlugin, null, Level.SEVERE, message, exception);
    }

}