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
package me.st28.flexseries.flexlib.command.argument;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandInterruptedException;
import me.st28.flexseries.flexlib.command.CommandInterruptedException.InterruptReason;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.utils.PluginUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class FlexPluginArgument extends Argument {

    public FlexPluginArgument(String name, boolean isRequired) {
        super(name, isRequired);
    }

    @Override
    public FlexPlugin parseInput(CommandContext context, String input) {
        Plugin plugin = PluginUtils.getPlugin(input);

        if (plugin == null) {
            throw new CommandInterruptedException(InterruptReason.ARGUMENT_SOFT_ERROR, MessageManager.getMessage(FlexLib.class, "general.errors.plugin_not_found", new ReplacementMap("{PLUGIN}", input).getMap()));
        }

        if (!(plugin instanceof FlexPlugin)) {
            throw new CommandInterruptedException(InterruptReason.ARGUMENT_SOFT_ERROR, MessageManager.getMessage(FlexLib.class, "lib_plugin.errors.plugin_not_flexplugin", new ReplacementMap("{PLUGIN}", plugin.getName()).getMap()));
        }

        return (FlexPlugin) plugin;
    }

    @Override
    public List<String> getSuggestions(CommandContext context, String argument) {
        List<String> returnList = new ArrayList<>();

        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin instanceof FlexPlugin) {
                returnList.add(plugin.getName());
            }
        }

        return returnList;
    }

}