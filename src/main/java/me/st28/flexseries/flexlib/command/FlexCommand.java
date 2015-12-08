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

import me.st28.flexseries.flexlib.permission.PermissionNode;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.bukkit.command.PluginCommand;

/**
 * Represents a registered command in a plugin's plugin.yml that uses FlexLib's command library.
 */
public abstract class FlexCommand<T extends FlexPlugin> extends AbstractCommand<T> {

    public FlexCommand(T plugin, CommandDescriptor descriptor) {
        super(plugin, descriptor);

        final String label = descriptor.getLabels().get(0);

        PluginCommand pluginCommand = plugin.getCommand(label);
        if (pluginCommand == null) {
            throw new IllegalArgumentException("Command '" + label + "' is not a registered command for plugin '" + plugin.getName() + "'");
        }

        descriptor.description(pluginCommand.getDescription());

        if (descriptor.getDefaultCommand() == null && descriptor.isDummy()) {
            descriptor.defaultCommand("help");
        }

        descriptor.lock();
    }

    @Override
    public final PermissionNode getPermission() {
        return getDescriptor().getPermission();
    }

    @Override
    public String buildUsage(CommandContext context) {
        String arguments = buildArgumentUsage();

        return "/" + (context != null ? context.getLabel() : getDescriptor().getLabels().get(0)) + (arguments.length() == 0 ? "" : " " + arguments);
    }

}