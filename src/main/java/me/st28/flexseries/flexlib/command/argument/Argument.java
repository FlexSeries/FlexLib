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
package me.st28.flexseries.flexlib.command.argument;

import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandInterruptedException;
import me.st28.flexseries.flexlib.command.CommandInterruptedException.InterruptReason;

import java.util.List;

public abstract class Argument {

    private final String name;
    private final boolean isRequired;

    public Argument(String name, boolean isRequired) {
        this.name = name;
        this.isRequired = isRequired;
    }

    public final String getName() {
        return name;
    }

    public final boolean isRequired() {
        return isRequired;
    }

    @Override
    public String toString() {
        return String.format(isRequired ? "<%s>" : "[%s]", name);
    }

    public final void execute(CommandContext context, int curIndex) {
        try {
            if (curIndex < context.getArgs().size()) {
                context.addGlobalObject(name, parseInput(context, context.getArgs().get(curIndex)));
            } else {
                context.addGlobalObject(name, getDefaultValue(context));
                context.indicateDefaultValue(name);
            }
        } catch (CommandInterruptedException ex) {
            // No need to handle.
            throw ex;
        } catch (Exception ex) {
            // Uncaught exception.
            throw new CommandInterruptedException(InterruptReason.ARGUMENT_ERROR, ex);
        }
    }

    public abstract Object parseInput(CommandContext context, String input);

    public Object getDefaultValue(CommandContext context) {
        return null;
    }

    public List<String> getSuggestions(CommandContext context, String input) {
        return null;
    }

}