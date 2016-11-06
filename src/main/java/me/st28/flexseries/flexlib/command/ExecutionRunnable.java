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
import me.st28.flexseries.flexlib.command.argument.ArgumentResolveException;
import me.st28.flexseries.flexlib.command.argument.ArgumentResolver;
import me.st28.flexseries.flexlib.messages.Message;
import me.st28.flexseries.flexlib.utils.SchedulerUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Why is this a runnable?
class ExecutionRunnable implements Runnable {

    private final static Pattern PATTERN_PERMISSION_VAR = Pattern.compile("\\{(n:)?([a-zA-Z0-9-_]+)\\}");

    private final BasicCommand command;
    private final CommandContext context;

    private String permission;
    private final Map<String, String> permissionVariables = new HashMap<>(); // Argument name, full replacement key

    ExecutionRunnable(CommandContext context) {
        this.command = context.getCommand();
        this.context = context;
    }

    @Override
    public void run() {
        final CommandSender sender = context.getSender();

        // Test permission (if set)
        if (!command.permission.isEmpty()) {
            Matcher matcher = PATTERN_PERMISSION_VAR.matcher(command.permission);
            while (matcher.find()) {
                // If group(1) is set, indicates that variable is an argument name
                if (matcher.group(1) != null) {
                    permissionVariables.put(matcher.group(1), matcher.group(0));
                } else {
                    final String type = matcher.group(2);
                    String arg = null;
                    for (ArgumentConfig config : command.argumentConfig) {
                        if (config.getType().equals(type)) {
                            arg = config.getName();
                        }
                    }

                    if (arg == null) {
                        sendMessage(Message.getGlobal("error.command_unknown_argument", type));
                        return;
                    }

                    permissionVariables.put(arg, matcher.group(0));
                }
            }

            if (!permissionVariables.isEmpty()) {
                permission = command.permission;
            } else if (!sender.hasPermission(command.permission)) {
                sendMessage(Message.getGlobal("error.no_permission"));
                return;
            }
        }

        // Check if sender is a player (if command is limited to players only)
        if (command.isPlayerOnly && !(sender instanceof Player)) {
            sendMessage(Message.getGlobal("error.must_be_player"));
            return;
        }

        // Check if there are arguments
        if (command.argumentConfig.length > 0) {
            if (context.getCurArgs().length < command.getRequiredArgs(context)) {
                sendMessage(Message.getGlobal("error.command_usage", command.getUsage(context)));
                return;
            }

            handleArgument(0);
            return;
        }

        // Check if there are auto arguments
        if (command.autoArgumentConfig.length > 0) {
            handleAutoArgument(0);
            return;
        }

        // If no arguments or auto arguments, just run the command
        command.executor.execute(context);
    }

    private void handleArgument(int index) {
        final ArgumentConfig config = command.argumentConfig[index];
        final ArgumentResolver resolver = ArgumentResolver.getResolver(config.getType());
        if (resolver == null) {
            sendMessage(Message.get(null, "error.command_unknown_argument", config.getType()));
            return;
        }

        Object resolved;
        try {
            resolved = resolver.resolve(context, config, context.getCurArgs()[index]);
        } catch (ArgumentResolveException ex) {
            sendMessage(ex.getErrorMessage());
            return;
        }

        if (resolved == null && resolver.isAsync()) {
            SchedulerUtils.runAsap(command.plugin, () -> {
                Object asyncResolved;
                try {
                    asyncResolved = resolver.resolveAsync(context, config, context.getCurArgs()[index]);
                } catch (ArgumentResolveException ex) {
                    sendMessage(ex.getErrorMessage());
                    return;
                }

                handleArgument0(resolver, config, asyncResolved, index);
            }, true);
            return;
        }

        handleArgument0(resolver, config, resolved, index);
    }

    private void handleArgument0(ArgumentResolver resolver, ArgumentConfig config, Object value, int index) {
        final String argName = config.getName();

        context.setArgument(argName, value);

        // If pending permission check, attempt to finish permission string
        if (permissionVariables.containsKey(argName)) {
            permission = permission.replace(permissionVariables.get(argName), resolver.getPermissionString(value));
            permissionVariables.remove(argName);

            // If empty, no more permission variables are pending. Perform permission check.
            if (permissionVariables.isEmpty()) {
                SchedulerUtils.runSynchronously(command.plugin, () -> {
                    if (!context.getSender().hasPermission(command.permission)) {
                        sendMessage(Message.getGlobal("error.no_permission"));
                        return;
                    }
                    handleArgument1(config, value, index);
                });
                return;
            }
        }

        handleArgument1(config, value, index);
    }

    private void handleArgument1(ArgumentConfig config, Object value, int index) {
        if (index == context.getCurArgs().length - 1) {
            // Done, run command

            if (command.autoArgumentConfig.length > 0) {
                // Handle auto arguments, if any
                handleAutoArgument(0);
                return;
            }

            executeCommand();
        } else {
            // Parse next argument
            handleArgument(index + 1);
        }
    }

    private void handleAutoArgument(int index) {
        final ArgumentConfig config = command.autoArgumentConfig[index];
        final ArgumentResolver resolver = ArgumentResolver.getResolver(config.getType());
        if (resolver == null) {
            sendMessage(Message.getGlobal("error.command_unknown_argument", config.getType()));
            return;
        }

        Object resolved;
        try {
            resolved = resolver.getDefault(context, config);
        } catch (ArgumentResolveException ex) {
            sendMessage(ex.getErrorMessage());
            return;
        }

        if (resolved == null && resolver.isAsync()) {
            SchedulerUtils.runAsap(command.plugin, () -> {
                Object asyncResolved;
                try {
                    asyncResolved = resolver.getDefaultAsync(context, config);
                } catch (ArgumentResolveException ex) {
                    sendMessage(ex.getErrorMessage());
                    return;
                }

                handleAutoArgument0(config, asyncResolved, index);
            }, true);
            return;
        }

        handleAutoArgument0(config, resolved, index);
    }

    private void handleAutoArgument0(ArgumentConfig config, Object value, int index) {
        context.setArgument(config.getName(), value);

        if (index == command.autoArgumentConfig.length - 1) {
            // Done, run command
            executeCommand();
        } else {
            // Parse next auto argument
            handleAutoArgument(index + 1);
        }
    }

    private void sendMessage(Message message) {
        SchedulerUtils.runSynchronously(command.plugin, () -> {
            CommandSender sender = context.getSender();
            if (sender != null) {
                message.sendTo(sender);
            }
        });
    }

    private void executeCommand() {
        SchedulerUtils.runSynchronously(command.plugin, () -> command.executor.execute(context));
    }

}