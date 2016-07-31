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
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FlexCommandMap {

    private static CommandMap bukkit_commandMap;
    private static Method bukkit_registerMethod;

    private static void registerBukkitCommand(FlexPlugin plugin, FlexCommand command) {
        final PluginManager pluginManager = Bukkit.getPluginManager();

        try {
            if (bukkit_registerMethod == null) {
                Field commandMap = pluginManager.getClass().getDeclaredField("commandMap");
                commandMap.setAccessible(true);

                bukkit_commandMap = (CommandMap) commandMap.get(pluginManager);
                bukkit_registerMethod = bukkit_commandMap.getClass().getDeclaredMethod("register", String.class, Command.class);
            }

            bukkit_registerMethod.invoke(bukkit_commandMap, plugin.getName(), command.bukkitCommand);
        } catch (Exception ex) {
            LogHelper.severe(plugin, "An exception occurred while registering command with Bukkit", ex);
        }
    }

    private final FlexPlugin plugin;

    public FlexCommandMap(FlexPlugin plugin) {
        Validate.notNull(plugin, "Plugin cannot be null");
        this.plugin = plugin;
    }

    /**
     * Registers a class instance containing methods annotating with {@link CommandHandler}.
     */
    public void register(Object handler) {
        final CommandModule commandModule = FlexPlugin.getGlobalModule(CommandModule.class);
        for (final Method method : handler.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(CommandHandler.class)) {
                // Ignore methods without the annotation
                continue;
            }

            final CommandHandler meta = method.getDeclaredAnnotation(CommandHandler.class);
            final String[] commandPath = meta.value()[0].split(" ");

            // 1) Get base command (or create + register if doesn't exist)
            FlexCommand base = (FlexCommand) commandModule.getCommand(plugin.getClass(), commandPath[0]);
            if (base == null) {
                // Base doesn't exist, create and register it with the command module.
                base = new FlexCommand(plugin, commandPath[0]);
                commandModule.registerCommand(plugin.getClass(), base);
                registerBukkitCommand(plugin, base);
            }

            // 2) Iterate through labels until subcommand is found (creating along the way)
            BasicCommand subcmd = base;
            for (int i = 1; i < commandPath.length; ++i) {
                final String curLabel = commandPath[i].toLowerCase();

                BasicCommand temp = subcmd.subcommands.get(curLabel);
                if (temp == null) {
                    // Subcommand doesn't exist, create and register under the parent
                    temp = new BasicCommand(plugin, curLabel);
                    subcmd.registerSubcommand(temp);
                }
                subcmd = temp;
            }

            // 3) Update final command's executor and meta
            subcmd.setMeta(meta, handler, method);

            // Set default
            if (meta.defaultSubcommand()) {
                if (subcmd.parent.defaultSubcommand != null) {
                    LogHelper.warning(plugin, "Multiple default subcommands are defined for command '" + subcmd.parent.label + "'");
                }
                subcmd.parent.defaultSubcommand = subcmd.label;
            }
        }
    }

}