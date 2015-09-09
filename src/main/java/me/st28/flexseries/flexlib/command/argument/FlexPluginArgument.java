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
    public List<String> getSuggestions(String argument) {
        List<String> returnList = new ArrayList<>();

        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin instanceof FlexPlugin) {
                returnList.add(plugin.getName());
            }
        }

        return returnList;
    }

}