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
package me.st28.flexseries.flexlib.command.argument.list;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandInterruptedException;
import me.st28.flexseries.flexlib.command.CommandInterruptedException.InterruptReason;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.plugin.module.FlexModule;

import java.util.List;
import java.util.stream.Collectors;

public class FlexModuleListArgument extends ListArgument {

    public FlexModuleListArgument(String name, boolean isRequired) {
        super(name, isRequired, new ListArgumentParser() {
            @Override
            public Object parseInput(CommandContext context, String input) {
                FlexPlugin plugin = context.getGlobalObject("plugin", FlexPlugin.class);

                for (FlexModule module : plugin.getModules()) {
                    if (module.getName().equalsIgnoreCase(input)) {
                        return module;
                    }
                }

                throw new CommandInterruptedException(InterruptReason.ARGUMENT_ERROR,
                        MessageManager.getMessage(FlexLib.class, "lib_plugin.errors.module_not_found",
                                new ReplacementMap("{PLUGIN}", plugin.getName())
                                        .put("{MODULE}", input)
                                        .getMap()
                        )
                );
            }
        });
    }

    @Override
    public List<String> getSuggestions(CommandContext context, String input) {
        FlexPlugin plugin = context.getGlobalObject("plugin", FlexPlugin.class);

        return plugin.getModules().stream().map(FlexModule::getName).collect(Collectors.toList());
    }

}