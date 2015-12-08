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