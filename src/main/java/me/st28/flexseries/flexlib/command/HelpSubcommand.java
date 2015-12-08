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

        if (!parent.getDescriptor().isDummy()) {
            rawMessages.put(parent.buildUsage(context), parent.getDescriptor().getDescription());
        }

        for (Subcommand<T> subcommand : parent.getSubcommands()) {
            if (!subcommand.getDescriptor().isDummy()) {
                rawMessages.put(subcommand.buildUsage(context), subcommand.getDescriptor().getDescription());
            }
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