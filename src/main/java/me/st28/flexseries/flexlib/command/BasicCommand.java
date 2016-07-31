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
import me.st28.flexseries.flexlib.messages.Message;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicCommand {

    final FlexPlugin plugin;
    final String label;
    final List<String> aliases = new ArrayList<>();
    String description;
    String permission;
    boolean isPlayerOnly;

    Executor executor;
    ArgumentConfig[] argumentConfig;

    BasicCommand parent;
    String defaultSubcommand;
    final Map<String, BasicCommand> subcommands = new HashMap<>();

    /**
     * Constructs an empty basic command as an empty command container.
     */
    BasicCommand(FlexPlugin plugin, String label) {
        this.plugin = plugin;
        this.label = label.toLowerCase();
    }

    /**
     * Sets the command's aliases and registers them with the parent command.
     */
    void setAliases(List<String> aliases) {
        this.aliases.addAll(aliases);
        if (parent != null) {
            parent.registerSubcommand(this);
        }
    }

    void registerSubcommand(BasicCommand command) {
        command.parent = this;
        subcommands.put(command.label.toLowerCase(), command);
        for (String alias : command.aliases) {
            subcommands.put(alias.toLowerCase(), command);
        }
    }

    void setMeta(CommandHandler meta, Object handler, Method method) {
        if (meta != null) {
            permission = meta.permission();
            isPlayerOnly = meta.playerOnly();
            argumentConfig = ArgumentConfig.parse(meta.args());
            setAliases(Arrays.asList(meta.value()).subList(1, meta.value().length));
        }

        if (method == null || handler == null) {
            executor = null;
            return;
        }

        executor = (context) -> {
            try {
                method.invoke(handler, context);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                Message.getGlobal("error.internal_error").sendTo(context.getSender());
                LogHelper.severe(plugin, "An exception occurred while running command " + Arrays.toString(context.getCurArgs()).replace("[", "").replace("]", ""), ex);
            }
        };
    }

    public FlexPlugin getPlugin() {
        return plugin;
    }

    public String getUsage(CommandContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("/").append(context.getLabel());

        for (int i = 0; i < context.getLevel(); i++) {
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

        if (executor == null) {
            // Try default subcommand
            if (defaultSubcommand != null) {
                BasicCommand subcmd = subcommands.get(defaultSubcommand);

                if (subcmd != null) {
                    subcmd.execute(sender, label, args, offset);
                    return;
                }
            }

            // Unknown command
            Message.getGlobal("error.unknown_subcommand").sendTo(sender, args.length > offset ? args[offset] : defaultSubcommand);
            return;
        }

        new ExecutionRunnable(new CommandContext(this, sender, label, args, offset)).run();
    }

    // ---------------------------------------------------------------------------------- //

    interface Executor {

        void execute(CommandContext context);

    }

}