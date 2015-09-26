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

/**
 * A subcommand that is registered under another command.
 */
public abstract class Subcommand<T extends FlexPlugin> extends AbstractCommand<T> {

    private final AbstractCommand<T> parent;

    public Subcommand(AbstractCommand<T> parent, CommandDescriptor descriptor) {
        super(parent.getPlugin(), descriptor);

        this.parent = parent;

        descriptor.lock();
    }

    /**
     * @return The command this subcommand is registered under.
     */
    public final AbstractCommand<T> getParent() {
        return parent;
    }

    @Override
    public final PermissionNode getPermission() {
        PermissionNode permission = getDescriptor().getPermission();
        return permission != null || !getDescriptor().shouldInheritPermission() ? permission : getParent().getPermission();
    }

    @Override
    public String buildUsage(CommandContext context) {
        StringBuilder builder = new StringBuilder(buildArgumentUsage());

        if (parent instanceof FlexCommand) {
            if (builder.length() > 0) {
                builder.insert(0, " ");
            }

            builder.insert(0, getDescriptor().getLabels().get(0));

            builder.insert(0, "/" + (context != null ? context.getLabel() : parent.getDescriptor().getLabels().get(0)) + " ");
        } else if (parent instanceof Subcommand) {
            if (builder.length() > 0) {
                builder.insert(0, " ");
            }

            builder.insert(0, parent.getDescriptor().getLabels().get(0));
        }

        return builder.toString();
    }

}