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
import me.st28.flexseries.flexlib.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            bukkit_registerMethod.invoke(bukkit_commandMap, plugin.getName(), command.bukkitCommand);
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
        final List<PendingCommand> pending = new ArrayList<>();

        for (final Method method : methods) {
            if (method.isAnnotationPresent(CommandHandler.class)) {
                PendingCommand cur = new PendingCommand();
                cur.meta = method.getDeclaredAnnotation(CommandHandler.class);
                cur.method = method;

                pending.add(cur);
            }
        }

        pending.sort((o1, o2) -> {
            final String p1 = o1.meta.parent();
            final String p2 = o2.meta.parent();

            if (p1.isEmpty() && p2.isEmpty()) {
                return 0;
            } else if (p1.isEmpty() && !p2.isEmpty()) {
                return -1;
            } else if (!p1.isEmpty() && p2.isEmpty()) {
                return 1;
            }

            return Integer.compare(p1.split(" ").length, p2.split(" ").length);
        });

        final Map<String, FlexCommand> topCommands = new HashMap<>();

        PENDING_LOOP:
        for (PendingCommand cur : pending) {
            if (cur.meta.parent().isEmpty()) {
                FlexCommand command = new FlexCommand(plugin, cur.meta, handler, cur.method);
                topCommands.put(cur.meta.value()[0], command);
                registerBukkitCommand(plugin, command);
                continue;
            }

            BasicCommand command = new BasicCommand(plugin, cur.meta, handler, cur.method);

            String[] parents = cur.meta.parent().split(" ");

            BasicCommand curParent = null;
            for (int i = 0; i < parents.length; i++) {
                if (i == 0) {
                    curParent = topCommands.get(parents[i]);
                } else {
                    curParent = curParent.subcommands.get(parents[i].toLowerCase());
                }

                if (curParent == null) {
                    LogHelper.warning(plugin, "Invalid parent command '" + parents[i] + "'");
                    continue PENDING_LOOP;
                }
            }

            if (curParent != null) {
                for (String label : cur.meta.value()) {
                    curParent.subcommands.put(label, command);
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------ //

    private static final class PendingCommand {

        CommandHandler meta;
        Method method;

    }

}