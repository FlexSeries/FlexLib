/**
 * Copyright 2016 Stealth2800 <http://stealthyone.com/>
 * Copyright 2016 Contributors <https://github.com/FlexSeries>
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
package me.st28.flexseries.flexlib.commands;

import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandHandler;
import me.st28.flexseries.flexlib.messages.Message;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;

public class FlexPluginCommands {

    @CommandHandler(
            value = "flexreload",
            description = "Reloads a FlexPlugin",
            args = {
                    "plugin flexplugin always",
                    "module flexmodule optional"
            },
            permission = "{flexplugin}.reload"
    )
    public void reload(CommandContext context) {
        final FlexPlugin plugin = context.getArgument("plugin", FlexPlugin.class);
        plugin.reloadAll();
        Message.getGlobal("notice.plugin_reloaded", plugin.getName()).sendTo(context.getSender());
    }

}