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
import me.st28.flexseries.flexlib.command.argument.PageArgument;
import me.st28.flexseries.flexlib.message.list.ListBuilder;
import me.st28.flexseries.flexlib.permission.PermissionNodes;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.plugin.module.FlexModule;
import me.st28.flexseries.flexlib.utils.QuickMap;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CmdFlexModules extends FlexCommand<FlexLib> {

    public CmdFlexModules(FlexLib plugin) {
        super(plugin, new CommandDescriptor("flexmodules").permission(PermissionNodes.MODULES));

        addArgument(new FlexPluginArgument("plugin", true));
        addArgument(new PageArgument(false));
    }

    @Override
    public void handleExecute(CommandContext context) {
        final FlexPlugin plugin = context.getGlobalObject("plugin", FlexPlugin.class);

        List<FlexModule> modules = new ArrayList<>(plugin.getModules());

        Collections.sort(modules, new Comparator<FlexModule>() {
            @Override
            public int compare(FlexModule o1, FlexModule o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });

        ListBuilder builder = new ListBuilder("page_subtitle", "Modules", plugin.getName(), context.getLabel());

        for (FlexModule module : modules) {
            String status;
            switch (module.getStatus()) {
                case ENABLED:
                    status = ChatColor.GREEN + "enabled";
                    break;

                case DISABLED:
                    status = ChatColor.DARK_RED + "disabled";
                    break;

                case DISABLED_ERROR:
                    status = ChatColor.RED + "error";
                    break;

                case DISABLED_DEPENDENCY:
                    status = ChatColor.RED + "missing dependency";
                    break;

                default:
                    status = ChatColor.RED + "error";
                    break;
            }

            builder.addMessage("lib_plugin_module", new QuickMap<>("{STATUS}", status).put("{NAME}", module.getName()).put("{DESCRIPTION}", module.getDescription()).getMap());
        }

        //TODO: Don't include page number if one was specified
        //builder.enableNextPageNotice(command.replace("/", ""));
        builder.sendTo(context.getSender(), context.getGlobalObject("page", Integer.class));
    }

}