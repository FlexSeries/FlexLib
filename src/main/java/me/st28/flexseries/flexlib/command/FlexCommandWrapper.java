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
        maxArgSuggestions = config.getInt("command library.max argument suggestions", 50);
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