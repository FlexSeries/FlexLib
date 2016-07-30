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
package me.st28.flexseries.flexlib.command;

import me.st28.flexseries.flexlib.logging.LogHelper;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FlexCommandMap {

    private static Object bukkit_commandMap;
    private static Method bukkit_registerMethod;

    private static void registerBukkitCommand(FlexPlugin plugin, FlexCommand command) {
        final PluginManager pluginManager = Bukkit.getPluginManager();

        try {
            if (bukkit_registerMethod == null) {
                Field commandMap = pluginManager.getClass().getDeclaredField("commandMap");
                commandMap.setAccessible(true);

                bukkit_commandMap = commandMap.get(pluginManager);
                bukkit_registerMethod = bukkit_commandMap.getClass().getDeclaredMethod("register", String.class, Command.class);
            }

            bukkit_registerMethod.invoke(bukkit_commandMap, plugin.getName(), command);
        } catch (Exception ex) {
            LogHelper.severe(plugin, "An exception occurred while registering command with Bukkit", ex);
        }
    }

    private final FlexPlugin plugin;

    public FlexCommandMap(FlexPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(Object handler) {
        final Method[] methods = handler.getClass().getDeclaredMethods();
        int count = 0;

        for (final Method method : methods) {
            if (method.isAnnotationPresent(CommandHandler.class)) {
                registerBukkitCommand(plugin, new FlexCommand(plugin, method.getDeclaredAnnotation(CommandHandler.class), handler, method));
                count++;
            }
        }

        LogHelper.debug(plugin, "Registered " + count + " command(s) in '" + handler.getClass().getCanonicalName() + "'");
    }

}