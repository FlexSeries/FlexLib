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

import me.st28.flexseries.flexlib.command.argument.PageArgument;
import me.st28.flexseries.flexlib.message.list.ListBuilder;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.utils.QuickMap;

import java.util.*;

public class HelpSubcommand<T extends FlexPlugin> extends Subcommand<T> {

    public HelpSubcommand(AbstractCommand<T> parent, CommandDescriptor descriptor) {
        super(parent, descriptor.labels("help", "?").description("Shows help"));

        addArgument(new PageArgument(false));
    }

    @Override
    public void handleExecute(CommandContext context) {
        Map<String, String> rawMessages = new HashMap<>();

        final AbstractCommand<T> parent = getParent();

        rawMessages.put(parent.buildUsage(context), parent.getDescriptor().getDescription());

        for (Subcommand<T> subcommand : parent.getSubcommands()) {
            rawMessages.put(subcommand.buildUsage(context), subcommand.getDescriptor().getDescription());
        }

        List<String> order = new ArrayList<>(rawMessages.keySet());
        Collections.sort(order);

        ListBuilder builder = new ListBuilder("page_subtitle", "Help", parent.getDescriptor().getLabels().get(0), context.getLabel());

        for (String key : order) {
            String description = rawMessages.get(key);

            if (description == null) {
                description = "{o=description_not_set}";
            }

            final Map<String, String> replacements = new QuickMap<>("{COMMAND}", key).put("{DESCRIPTION}", description).getMap();

            if (key.contains("<") || key.contains("[")) {
                builder.addMessage("help_command_suggestion", replacements);
            } else {
                builder.addMessage("help_command", replacements);
            }
        }

        builder.sendTo(context.getSender(), context.getGlobalObject("page", Integer.class));
    }

}