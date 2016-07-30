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
import me.st28.flexseries.flexlib.command.argument.ArgumentResolver;
import me.st28.flexseries.flexlib.logging.LogHelper;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public final class FlexCommand extends Command {

    final FlexPlugin plugin;

    final Executor executor;

    final String permission;
    final boolean isPlayerOnly;

    final ArgumentConfig[] argumentConfig;

    FlexCommand(FlexPlugin plugin, CommandHandler meta, Object handler, Method method) {
        super(meta.value()[0], meta.description(), "(usage message)", Arrays.asList(meta.value()).subList(0, meta.value().length));

        this.plugin = plugin;
        permission = meta.permission();
        isPlayerOnly = meta.playerOnly();
        argumentConfig = ArgumentConfig.parse(meta.args());

        // Set default usage message
        StringBuilder sb = new StringBuilder();
        sb.append("/").append(meta.value()[0]);
        for (ArgumentConfig cur : argumentConfig) {
            sb.append(" ").append(cur.getUsage(null));
        }
        setUsage(sb.toString());

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

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        new ExecutionRunnable(this, new CommandContext(this, sender, label, args), args).run();
        return true;
    }

    // ---------------------------------------------------------------------------------- //

    interface Executor {

        void execute(CommandContext context);

    }

}