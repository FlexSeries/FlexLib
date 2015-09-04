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
        super(plugin, new CommandDescriptor("flexreload").permission(PermissionNodes.MODULES));

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