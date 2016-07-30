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

import me.st28.flexseries.flexlib.command.argument.ArgumentConfig;
import me.st28.flexseries.flexlib.logging.LogHelper;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class BasicCommand {

    final FlexPlugin plugin;

    final Executor executor;

    final String permission;
    final boolean isPlayerOnly;

    final ArgumentConfig[] argumentConfig;

    BasicCommand parent;
    final Map<String, BasicCommand> subcommands = new HashMap<>();

    BasicCommand(FlexPlugin plugin, CommandHandler meta, Object handler, Method method) {
        this.plugin = plugin;
        permission = meta.permission();
        isPlayerOnly = meta.playerOnly();
        argumentConfig = ArgumentConfig.parse(meta.args());

        // Setup command executor
        executor = (context) -> {
            try {
                method.invoke(handler, context);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                context.getSender().sendMessage(ChatColor.RED + "An internal exception occurred while running this command.");
                LogHelper.severe(plugin, "An exception occurred while running command", ex);
            }
        };
    }

    public FlexPlugin getPlugin() {
        return plugin;
    }

    public String getUsage(CommandContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("/").append(context.getLabel());

        for (int i = 0; i < context.getOffset(); i++) {
            sb.append(" ").append(context.getRawArgs()[i]);
        }

        for (ArgumentConfig cur : argumentConfig) {
            sb.append(" ").append(cur.getUsage(context));
        }
        return sb.toString();
    }

    public int getRequiredArgs(CommandContext context) {
        int count = 0;
        for (ArgumentConfig cur : argumentConfig) {
            if (cur.isRequired(context)) {
                count++;
            }
        }
        return count;
    }

    public void execute(CommandSender sender, String label, String[] args, int offset) {
        if (args.length > offset && subcommands.containsKey(args[offset].toLowerCase())) {
            subcommands.get(args[offset].toLowerCase()).execute(sender, label, args, offset + 1);
            return;
        }

        new ExecutionRunnable(new CommandContext(this, sender, label, args, offset)).run();
    }

    // ---------------------------------------------------------------------------------- //

    interface Executor {

        void execute(CommandContext context);

    }

}