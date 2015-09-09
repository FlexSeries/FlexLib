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