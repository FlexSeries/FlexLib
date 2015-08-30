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
import me.st28.flexseries.flexlib.permission.PermissionNode;
import org.apache.commons.lang.Validate;

import java.util.*;
import java.util.Map.Entry;

/**
 * The base logic handler for commands.
 */
public abstract class LogicHub extends LogicPart {

    private int firstArgumentIndex = -1;

    private final Map<String, LogicPath> paths = new HashMap<>();

    public LogicHub() {}

    public final void setFirstArgumentIndex(int index) {
        if (firstArgumentIndex != -1) {
            throw new IllegalStateException("First argument index has already been set.");
        }

        this.firstArgumentIndex = index;
    }

    public final int getFirstArgumentIndex() {
        return firstArgumentIndex;
    }

    public final void addPath(LogicPath path) {
        Validate.notNull(path, "Path cannot be null.");

        for (String label : path.getLabels()) {
            if (paths.containsKey(label)) {
                // Duplicate/conflict?
                continue;
            }

            path.setParent(this);
            paths.put(label, path);
        }
    }

    public final Map<String, LogicPath> getPaths() {
        return Collections.unmodifiableMap(paths);
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
        final List<String> suggestions = new ArrayList<>();

        if (getFirstArgumentIndex() == curIndex) {
            for (Entry<String, LogicPath> entry : paths.entrySet()) {
                PermissionNode permission = entry.getValue().getPermission();
                if (permission != null && permission.isAllowed(context.getSender())) {
                    suggestions.add(entry.getKey());
                }
            }
        }

        final List<String> args = context.getArgs();

        if (curIndex > 0 && curIndex < args.size()) {
            LogicPath path = paths.get(args.get(curIndex - 1).toLowerCase());
            if (path != null) {
                suggestions.addAll(path.getSuggestions(context, curIndex - getFirstArgumentIndex() - 1));
            }
        }

        return suggestions;
    }

    /**
     * Handles the execution of the command's logic.
     *
     * @param context The context in which the command is being executed in.
     */
    public abstract void handleExecute(CommandContext context, int curIndex);

}