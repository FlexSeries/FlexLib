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