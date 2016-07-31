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
package me.st28.flexseries.flexlib.command;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.command.DefaultArgumentResolvers.BooleanResolver;
import me.st28.flexseries.flexlib.command.DefaultArgumentResolvers.DoubleResolver;
import me.st28.flexseries.flexlib.command.DefaultArgumentResolvers.FloatResolver;
import me.st28.flexseries.flexlib.command.DefaultArgumentResolvers.IntegerResolver;
import me.st28.flexseries.flexlib.command.DefaultArgumentResolvers.LongResolver;
import me.st28.flexseries.flexlib.command.DefaultArgumentResolvers.PlayerResolver;
import me.st28.flexseries.flexlib.command.DefaultArgumentResolvers.StringResolver;
import me.st28.flexseries.flexlib.command.argument.ArgumentResolver;
import me.st28.flexseries.flexlib.plugin.FlexModule;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.apache.commons.lang.Validate;

import java.util.HashMap;
import java.util.Map;

public final class CommandModule extends FlexModule<FlexLib> {

    final Map<Class<? extends FlexPlugin>, Map<String, FlexCommand>> commands = new HashMap<>();

    public CommandModule(FlexLib plugin) {
        super(plugin, "commands", "Manages the FlexLib command framework");
    }

    @Override
    protected void handleEnable() {
        ArgumentResolver.register(null, "boolean", new BooleanResolver());
        ArgumentResolver.register(null, "integer", new IntegerResolver());
        ArgumentResolver.register(null, "long", new LongResolver());
        ArgumentResolver.register(null, "float", new FloatResolver());
        ArgumentResolver.register(null, "double", new DoubleResolver());
        ArgumentResolver.register(null, "player", new PlayerResolver());
        ArgumentResolver.register(null, "string", new StringResolver());
    }

    public BasicCommand getCommand(Class<? extends FlexPlugin> plugin, String command) {
        Validate.notNull(plugin, "Plugin cannot be null");
        Validate.notNull(command, "Command cannot be null");

        if (!commands.containsKey(plugin)) {
            return null;
        }

        final String[] path = command.split(" ");

        BasicCommand found = commands.get(plugin).get(path[0].toLowerCase());
        for (int i = 1; i < path.length; ++i) {
            found = found.subcommands.get(path[i].toLowerCase());
            if (found == null) {
                break;
            }
        }
        return found;
    }

    void registerCommand(Class<? extends FlexPlugin> plugin, FlexCommand command) {
        if (!commands.containsKey(plugin)) {
            commands.put(plugin, new HashMap<>());
        }

        commands.get(plugin).put(command.label.toLowerCase(), command);
    }

}