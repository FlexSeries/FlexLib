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
package me.st28.flexseries.flexlib.command.logic;

import me.st28.flexseries.flexlib.command.CommandContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A linear layout of a command's logic.
 */
public class LogicPath {

    private final List<String> labels = new ArrayList<>();

    private final List<LogicPart> parts = new ArrayList<>();

    public LogicPath(String... labels) {
        Collections.addAll(this.labels, labels);
    }

    public List<String> getLabels() {
        return Collections.unmodifiableList(labels);
    }

    public LogicPath append(LogicPart part) {
        parts.add(part);
        return this;
    }

    public final void execute(CommandContext context, int curIndex) {
        for (LogicPart part : parts) {
            part.execute(context, curIndex++);
        }
    }

    public List<String> getSuggestions(CommandContext context, int curIndex) {
        LogicPart part = parts.get(curIndex);

        String argument = context.getArgs().get(curIndex).toLowerCase();

        List<String> suggestions = part.getSuggestions(context, curIndex);

        List<String> returnList = new ArrayList<>();

        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(argument)) {
                returnList.add(suggestion);
            }
        }

        return returnList;
    }

}