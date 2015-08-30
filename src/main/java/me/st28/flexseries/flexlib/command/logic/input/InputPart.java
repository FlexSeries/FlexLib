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
package me.st28.flexseries.flexlib.command.logic.input;

import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.logic.LogicPart;

import java.util.List;

public abstract class InputPart extends LogicPart {

    protected final String name;
    protected final boolean isRequired;

    public InputPart(String name, boolean isRequired) {
        this.name = name;
        this.isRequired = isRequired;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return isRequired;
    }

    @Override
    public String toString() {
        return String.format(isRequired ? "<%s>" : "[%s]", name);
    }

    @Override
    public void execute(CommandContext context, int curIndex) {
        if (curIndex < context.getArgs().size()) {
            context.addGlobalObject(name, parseInput(context, context.getArgs().get(curIndex)));
        } else {
            context.addGlobalObject(name, getDefaultValue(context));
            context.indicateDefaultValue(name);
        }

        handleExecution(context, curIndex);
    }

    public abstract Object parseInput(CommandContext context, String input);

    public Object getDefaultValue(CommandContext context) {
        return null;
    }

    public List<String> getSuggestions(CommandContext context, int curIndex) {
        return null;
    }

    public void handleExecution(CommandContext context, int curIndex) {}

}