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
        String arguments = super.buildUsage(context);

        return "/" + (context != null ? context.getLabel() : getDescriptor().getLabels().get(0)) + (arguments.length() == 0 ? "" : " " + arguments);
    }

}