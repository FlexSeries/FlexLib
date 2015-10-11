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
package me.st28.flexseries.flexlib.backend.commands;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandDescriptor;
import me.st28.flexseries.flexlib.command.FlexCommand;
import me.st28.flexseries.flexlib.command.argument.FlexPluginArgument;
import me.st28.flexseries.flexlib.command.argument.list.FlexModuleListArgument;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.permission.PermissionNodes;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.plugin.module.FlexModule;

import java.util.List;

public final class CmdFlexReload extends FlexCommand<FlexLib> {

    public CmdFlexReload(FlexLib plugin) {
        super(plugin, new CommandDescriptor("flexreload").permission(PermissionNodes.RELOAD));

        addArgument(new FlexPluginArgument("plugin", true));
        addArgument(new FlexModuleListArgument("modules", false));
    }

    @Override
    public void handleExecute(CommandContext context) {
        FlexPlugin plugin = context.getGlobalObject("plugin", FlexPlugin.class);

        plugin.reloadConfig();

        List<FlexModule> modules = context.getGlobalObject("modules", List.class);

        for (FlexModule module : plugin.getModules()) {
            if (modules != null && !modules.contains(module)) {
                continue;
            }

            module.reloadConfig();
        }

        plugin.reloadAll();

        MessageManager.getMessage(FlexLib.class, "lib_plugin.notices.plugin_reloaded", new ReplacementMap("{PLUGIN}", plugin.getName()).getMap()).sendTo(context.getSender());
        // TODO: Show different message if specified modules are reloaded
    }

}