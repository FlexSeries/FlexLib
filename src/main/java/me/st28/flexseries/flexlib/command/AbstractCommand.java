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
package me.st28.flexseries.flexlib.command;

import me.st28.flexseries.flexlib.command.CommandInterruptedException.InterruptReason;
import me.st28.flexseries.flexlib.command.argument.Argument;
import me.st28.flexseries.flexlib.permission.PermissionNode;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;

import java.util.*;

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

    public String buildUsage(CommandContext context) {
        StringBuilder builder = new StringBuilder();

        for (Argument argument : getArguments()) {
            if (builder.length() != 0) {
                builder.append(" ");
            }
            builder.append(argument.toString());
        }

        return builder.toString();
    }

    public final int getArgumentOffset() {
        return argumentOffset;
    }

    public final List<Argument> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public final void addArgument(Argument argument) {
        arguments.add(argument);
    }

    public final void registerSubcommand(Subcommand<T> subcommand, String... directAliases) {
        Validate.notNull(subcommand, "Subcommand cannot be null.");

        final List<String> labels = subcommand.getDescriptor().getLabels();
        final String mainLabel = labels.get(0);

        ((AbstractCommand) subcommand).argumentOffset = argumentOffset + 1;

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
        final List<String> args = context.getArgs();
        final String label = context.getLabel();
        final String directAlias = subcommandDirectAliases.get(label.toLowerCase());

        Subcommand subcommand = null;
        if (directAlias != null) {
            subcommand = subcommands.get(directAlias);
        }

        if (curIndex < args.size()) {
            final String alias = subcommandAliases.get(args.get(curIndex).toLowerCase());
            if (alias != null) {
                subcommand = subcommands.get(alias);
                curIndex++;
            }
        }

        if (subcommand != null) {
            subcommand.execute(context, curIndex);
            return;
        }

        if (getRelativeArgs(context).size() < getRequiredArgs()) {
            // Show usage
            throw new CommandInterruptedException(InterruptReason.INVALID_USAGE);
        }

        for (Argument argument : this.arguments) {
            try {
                argument.execute(context, curIndex);
            } catch (CommandInterruptedException ex) {
                if (ex.getReason() == InterruptReason.ARGUMENT_INVALID_INPUT) {
                    if (!argument.isRequired() && curIndex - argumentOffset != this.arguments.size() - 1) {
                        context.addGlobalObject(argument.getName(), argument.getDefaultValue(context));
                        context.indicateDefaultValue(argument.getName());
                        continue;
                    } else {
                        ex.getExitMessage().sendTo(context.getSender());
                        return;
                    }
                }
            }
            curIndex++;
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
        return context.getArgs().subList(argumentOffset, context.getArgs().size());
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
                PermissionNode curPermission = curSubcommand.getPermission();

                if (curPermission == null || curPermission.isAllowed(sender)) {
                    returnList.add(curSubcommand.getDescriptor().getLabels().get(0));
                }
            }
        }

        if (newIndex < arguments.size() && newIndex < args.size()) {
            returnList.addAll(arguments.get(newIndex).getSuggestions(args.get(newIndex)));
        }
        return returnList;
    }

}