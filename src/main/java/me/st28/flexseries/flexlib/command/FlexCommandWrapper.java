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

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.log.LogHelper;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.message.reference.MessageReference;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class FlexCommandWrapper implements CommandExecutor, TabCompleter {

    private static int maxArgSuggestions;

    public static void reload(ConfigurationSection config) {
        maxArgSuggestions = config.getInt("command library.max argument suggestions", 15);
    }

    public static int getMaxArgSuggestions() {
        return maxArgSuggestions;
    }

    public static void registerCommand(FlexCommand<? extends FlexPlugin> command) {
        final FlexPlugin plugin = command.getPlugin();
        final String label = command.getDescriptor().getLabels().get(0);

        PluginCommand pluginCommand = plugin.getCommand(label);
        if (pluginCommand == null) {
            throw new IllegalArgumentException("Command '" + label + "' is not a registered command for plugin '" + plugin.getName() + "'");
        }

        FlexCommandWrapper wrapper = new FlexCommandWrapper(command);
        pluginCommand.setExecutor(wrapper);
        pluginCommand.setTabCompleter(wrapper);
    }

    private final FlexCommand<? extends FlexPlugin> command;

    public FlexCommandWrapper(FlexCommand<? extends FlexPlugin> command) {
        this.command = command;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CommandContext context = new CommandContext(this.command, sender, label, args);

        try {
            this.command.execute(context, 0);
        } catch (CommandInterruptedException ex) {
            if (ex.getExitMessage() != null) {
                ex.getExitMessage().sendTo(sender);
            } else {
                switch (ex.getReason()) {
                    case INVALID_USAGE:
                        MessageManager.getMessage(FlexLib.class, "lib_command.errors.usage", new ReplacementMap("{USAGE}", this.command.getFinalCommand(context, 0).buildUsage(context)).getMap()).sendTo(sender);
                        return true;

                    default:
                }
            }

            if (ex.getReason().isError()) {
                MessageReference message = MessageManager.getMessage(FlexLib.class, "lib_command.errors.uncaught_exception", new ReplacementMap("{MESSAGE}", ex.getMessage()).getMap());
                LogHelper.severe(this.command.getPlugin(), message.getMessage(), ex);
            }
        } catch (Exception ex) {
            MessageReference message = MessageManager.getMessage(FlexLib.class, "lib_command.errors.uncaught_exception", new ReplacementMap("{MESSAGE}", ex.getMessage()).getMap());
            LogHelper.severe(this.command.getPlugin(), message.getMessage(), ex);
            message.sendTo(sender);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        CommandContext context = new CommandContext(this.command, sender, label, args);

        final List<String> suggestions = this.command.getSuggestions(context, args.length - 1);

        if (suggestions == null) {
            return null;
        }

        String argument = context.getArgs().get(args.length - 1).toLowerCase();

        List<String> returnList = new ArrayList<>();

        for (String suggestion : suggestions) {
            if (maxArgSuggestions != 0 && returnList.size() >= maxArgSuggestions) {
                break;
            }

            if (suggestion.toLowerCase().startsWith(argument)) {
                returnList.add(suggestion);
            }
        }

        return returnList;
    }

}