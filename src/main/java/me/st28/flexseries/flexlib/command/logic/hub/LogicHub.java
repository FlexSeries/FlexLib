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
package me.st28.flexseries.flexlib.command.logic.hub;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.command.CommandInterruptedException;
import me.st28.flexseries.flexlib.command.logic.LogicPart;
import me.st28.flexseries.flexlib.command.logic.LogicPath;
import me.st28.flexseries.flexlib.message.MessageManager;
import me.st28.flexseries.flexlib.message.ReplacementMap;
import org.apache.commons.lang.Validate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The base logic handler for commands.
 */
public abstract class LogicHub extends LogicPart {

    protected final Map<String, LogicPath> paths = new HashMap<>();

    public LogicHub() {}

    public void addPath(LogicPath path) {
        Validate.notNull(path, "Path cannot be null.");

        for (String label : path.getLabels()) {
            if (paths.containsKey(label)) {
                // Duplicate/conflict?
                continue;
            }

            paths.put(label, path);
        }
    }

    @Override
    public void execute(CommandContext context, int curIndex) {
        final List<String> args = context.getArgs();

        if (curIndex < args.size()) {
            LogicPath path = paths.get(args.get(curIndex).toLowerCase());
            if (path != null) {
                if (args.size() < path.getRequiredArgs()) {
                    throw new CommandInterruptedException(MessageManager.getMessage(FlexLib.class, "lib_command.errors.usage", new ReplacementMap("{USAGE}", path.buildUsage()).getMap()));
                }

                path.execute(context, curIndex + 1);
                return;
            }
        }

        handleExecute(context, curIndex);
    }

    @Override
    public List<String> getSuggestions(CommandContext context, int curIndex) {
        final List<String> args = context.getArgs();

        if (curIndex < args.size()) {
            LogicPath path = paths.get(args.get(curIndex).toLowerCase());
            if (path != null) {
                return path.getSuggestions(context, curIndex);
            }
        }

        return null;
    }

    /**
     * Handles the execution of the command's logic.
     *
     * @param context The context in which the command is being executed in.
     */
    public abstract void handleExecute(CommandContext context, int curIndex);

}