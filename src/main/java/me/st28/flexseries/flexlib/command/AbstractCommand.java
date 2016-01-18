/**
 * Copyright 2015 Stealth2800 <http://stealthyone.com/>
 * Copyright 2015 Contributors <https://github.com/FlexSeries>
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

import me.st28.flexseries.flexlib.command.CommandInterruptedException.InterruptReason;
import me.st28.flexseries.flexlib.command.argument.Argument;
import me.st28.flexseries.flexlib.command.argument.AsyncArgument;
import me.st28.flexseries.flexlib.permission.PermissionNode;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.utils.CommandSenderRef;
import me.st28.flexseries.flexlib.utils.TaskChain;
import me.st28.flexseries.flexlib.utils.TaskChain.AsyncGenericTask;
import me.st28.flexseries.flexlib.utils.TaskChain.GenericTask;
import me.st28.flexseries.flexlib.utils.TaskChain.LastTask;
import me.st28.flexseries.flexlib.utils.task.SyncCommandSenderTask;
import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractCommand<T extends FlexPlugin> {

    /**
     * The plugin that owns this command.
     */
    private final T plugin;

    private final CommandDescriptor descriptor;

    /**
     * Subcommands registered under this command.
     */
    private final Map<String, Subcommand<T>> subcommands = new HashMap<>();

    /**
     * Aliases for the subcommands registered under this command.<br />
     * <pre>
     *     /label subcommand -> subcommand
     *     /label alias -> subcommand
     * </pre>
     */
    private final Map<String, String> subcommandAliases = new HashMap<>();

    /**
     * Aliases of the main label that will execute a subcommand directly.<br />
     * <pre>
     *     /label subcommand -> subcommand
     *     /directlabel -> subcommand
     * </pre>
     */
    private final Map<String, String> subcommandDirectAliases = new HashMap<>();

    private int argumentOffset = 0;

    private final List<Argument> arguments = new ArrayList<>();

    /**
     * @param plugin See {@link #plugin}
     * @param descriptor Information about the command.
     */
    AbstractCommand(T plugin, CommandDescriptor descriptor) {
        Validate.notNull(plugin, "Plugin cannot be null.");
        Validate.notNull(descriptor, "Descriptor cannot be null.");

        this.plugin = plugin;
        this.descriptor = descriptor;

        if (!(this instanceof HelpSubcommand)) {
            registerSubcommand(new HelpSubcommand<T>(this, new CommandDescriptor()));
        }
    }

    /**
     * @see #plugin
     */
    public final T getPlugin() {
        return plugin;
    }

    public final CommandDescriptor getDescriptor() {
        return descriptor;
    }

    public abstract PermissionNode getPermission();

    String buildArgumentUsage() {
        StringBuilder builder = new StringBuilder();

        for (Argument argument : getArguments()) {
            if (builder.length() != 0) {
                builder.append(" ");
            }
            builder.append(argument.toString());
        }

        return builder.toString();
    }

    public abstract String buildUsage(CommandContext context);

    public final int getArgumentOffset() {
        return argumentOffset;
    }

    public final List<Argument> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public final void addArgument(Argument argument) {
        arguments.add(argument);

        int newOffset = argumentOffset + arguments.size() + 2;
        for (Subcommand<T> subcommand : subcommands.values()) {
            if (subcommand instanceof ReverseSubcommand) {
                ((AbstractCommand) subcommand).argumentOffset = newOffset;
            }
        }
    }

    public final Collection<Subcommand<T>> getSubcommands() {
        return Collections.unmodifiableCollection(subcommands.values());
    }

    public final Subcommand<T> getSubcommand(String name) {
        Validate.notNull(name, "Name cannot be null.");
        return subcommands.get(name.toLowerCase());
    }

    public final void registerSubcommand(Subcommand<T> subcommand, String... directAliases) {
        Validate.notNull(subcommand, "Subcommand cannot be null.");

        final List<String> labels = subcommand.getDescriptor().getLabels();
        final String mainLabel = labels.get(0);

        if (subcommand instanceof ReverseSubcommand) {
            ((AbstractCommand) subcommand).argumentOffset = argumentOffset + arguments.size() + 2;
        } else {
            ((AbstractCommand) subcommand).argumentOffset = argumentOffset + 1;
        }

        subcommands.put(mainLabel, subcommand);

        for (String label : labels) {
            if (!subcommandAliases.containsKey(label)) {
                subcommandAliases.put(label, mainLabel);
            }
        }

        for (String label : directAliases) {
            subcommandDirectAliases.put(label.toLowerCase(), mainLabel);
        }
    }

    public final AbstractCommand<T> getFinalCommand(CommandContext context, int curIndex) {
        final List<String> args = context.getArgs();
        final String label = context.getLabel();
        final String directAlias = subcommandDirectAliases.get(label.toLowerCase());

        Subcommand<T> subcommand = null;
        if (directAlias != null) {
            subcommand = subcommands.get(directAlias);
        }

        if (curIndex < args.size()) {
            final String alias = subcommandAliases.get(args.get(curIndex).toLowerCase());
            if (alias != null) {
                subcommand = subcommands.get(alias);
            }
        }

        if (subcommand != null) {
            return subcommand;
        }

        return this;
    }

    public final void execute(CommandContext context, int curIndex) {
        final CommandSender sender = context.getSender();
        final List<String> args = context.getArgs();
        final String label = context.getLabel();
        final String directAlias = subcommandDirectAliases.get(label.toLowerCase());

        Subcommand subcommand = null;
        if (directAlias != null) {
            subcommand = subcommands.get(directAlias);
        }

        if (curIndex >= 0 && curIndex < args.size()) {
            final String alias = subcommandAliases.get(args.get(curIndex).toLowerCase());
            if (alias != null) {
                subcommand = subcommands.get(alias);
                curIndex++;
            }
        }

        if (subcommand != null && !(subcommand instanceof ReverseSubcommand)) {
            subcommand.execute(context, curIndex);
            return;
        }

        final PermissionNode permission = getPermission();

        if (permission != null && !permission.isAllowed(sender)) {
            throw new CommandInterruptedException(InterruptReason.NO_PERMISSION);
        }

        if (getDescriptor().isPlayerOnly() && !(sender instanceof Player)) {
            throw new CommandInterruptedException(InterruptReason.MUST_BE_PLAYER);
        }

        if (getRelativeArgs(context).size() < getRequiredArgs()) {
            // Show usage

            final String defName = descriptor.getDefaultCommand();
            if (defName != null) {
                final Subcommand<T> defCommand = subcommands.get(defName.toLowerCase());
                if (defCommand != null) {
                    defCommand.execute(context, curIndex);
                    return;
                }
            }

            throw new CommandInterruptedException(InterruptReason.INVALID_USAGE);
        }

        Map<Argument, Integer> asyncArgs = new HashMap<>();

        for (Argument argument : this.arguments) {
            if (argument instanceof AsyncArgument) {
                asyncArgs.put(argument, curIndex);
                curIndex++;
                continue;
            }

            try {
                argument.execute(context, curIndex);
            } catch (CommandInterruptedException ex) {
                if (ex.getReason() == InterruptReason.ARGUMENT_SOFT_ERROR) {
                    if (!argument.isRequired() && curIndex - argumentOffset != this.arguments.size() - 1) {
                        context.addGlobalObject(argument.getName(), argument.getDefaultValue(context));
                        context.indicateDefaultValue(argument.getName());
                        continue;
                    } else {
                        ex.getExitMessage().sendTo(sender);
                        return;
                    }
                }
                throw ex;
            }
            curIndex++;
        }

        if (asyncArgs.isEmpty()) {
            completeExecute(context, curIndex);
            return;
        }

        final CommandSenderRef senderRef = new CommandSenderRef(sender);
        final int finalCurIndex = curIndex;
        new TaskChain().add(new AsyncGenericTask() {
            @Override
            protected void run() {
                for (Entry<Argument, Integer> entry : asyncArgs.entrySet()) {
                    Argument argument = entry.getKey();
                    Integer curIndex = entry.getValue();

                    try {
                        argument.execute(context, curIndex);
                    } catch (CommandInterruptedException ex) {
                        if (ex.getReason() == InterruptReason.ARGUMENT_SOFT_ERROR) {
                            if (!argument.isRequired() && curIndex - argumentOffset != AbstractCommand.this.arguments.size() - 1) {
                                context.addGlobalObject(argument.getName(), argument.getDefaultValue(context));
                                context.indicateDefaultValue(argument.getName());
                                continue;
                            } else {
                                abort();
                                new SyncCommandSenderTask(senderRef, new LastTask<CommandSender>() {
                                    @Override
                                    protected void run(CommandSender arg) {
                                        ex.getExitMessage().sendTo(arg);
                                    }
                                }).execute();
                                return;
                            }
                        }
                        throw ex;
                    }
                }
            }
        }).add(new GenericTask() {
            @Override
            protected void run() {
                completeExecute(context, finalCurIndex);
            }
        }).execute();
    }

    public final void completeExecute(CommandContext context, int curIndex) {
        if (context.getSender() == null) {
            // Sender is a player who is no longer online
            return;
        }

        final List<String> args = context.getArgs();

        if (curIndex < args.size()) {
            Subcommand<T> preSubcommand = subcommands.get(args.get(curIndex));
            if (preSubcommand != null && preSubcommand instanceof ReverseSubcommand) {
                preSubcommand.execute(context, curIndex + 1);
                return;
            }
        }

        handleExecute(context);
    }

    public final int getRequiredArgs() {
        int count = 0;
        for (Argument arg : arguments) {
            if (arg.isRequired()) {
                count++;
            }
        }
        return count;
    }

    public final List<String> getRelativeArgs(CommandContext context) {
        if (context.getArgs().isEmpty()) {
            return new ArrayList<>();
        } else {
            return context.getArgs().subList(argumentOffset, context.getArgs().size());
        }
    }

    public abstract void handleExecute(CommandContext context);

    public List<String> getSuggestions(CommandContext context, int curIndex) {
        final CommandSender sender = context.getSender();
        final List<String> args = getRelativeArgs(context);
        final String label = context.getLabel();
        final String directAlias = subcommandDirectAliases.get(label.toLowerCase());

        Subcommand<T> subcommand = null;
        if (directAlias != null) {
            subcommand = subcommands.get(directAlias);
        }

        int newIndex = curIndex - argumentOffset;

        if (!args.isEmpty() && subcommand == null) {
            final String alias = subcommandAliases.get(args.get(0).toLowerCase());
            if (alias != null) {
                subcommand = subcommands.get(alias);
            }
        }

        if (subcommand != null) {
            return subcommand.getSuggestions(context, directAlias == null ? curIndex : (curIndex + 1));
        }

        final PermissionNode permission = getPermission();

        if (permission != null && !permission.isAllowed(context.getSender())) {
            return null;
        }

        List<String> returnList = new ArrayList<>();

        if (newIndex == 0) {
            for (Subcommand<T> curSubcommand : subcommands.values()) {
                if (curSubcommand instanceof ReverseSubcommand) {
                    continue;
                }

                PermissionNode curPermission = curSubcommand.getPermission();

                if (curPermission == null || curPermission.isAllowed(sender)) {
                    returnList.add(curSubcommand.getDescriptor().getLabels().get(0));
                }
            }
        } else {
            for (Subcommand<T> curSubcommand : subcommands.values()) {
                if (!(curSubcommand instanceof ReverseSubcommand) || curIndex != curSubcommand.getArgumentOffset() - 1) {
                    continue;
                }

                PermissionNode curPermission = curSubcommand.getPermission();

                if (curPermission == null || curPermission.isAllowed(sender)) {
                    returnList.add(curSubcommand.getDescriptor().getLabels().get(0));
                }
            }
        }

        if (newIndex >= 0 && newIndex < arguments.size() && newIndex < args.size()) {
            final List<String> suggestionsToAdd = arguments.get(newIndex).getSuggestions(context, args.get(newIndex));

            if (suggestionsToAdd != null) {
                returnList.addAll(suggestionsToAdd);
            }
        }
        return returnList;
    }

}