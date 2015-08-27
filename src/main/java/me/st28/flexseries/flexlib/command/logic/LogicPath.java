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
import me.st28.flexseries.flexlib.command.CommandUtils;
import me.st28.flexseries.flexlib.command.logic.hub.LogicHub;
import me.st28.flexseries.flexlib.command.logic.input.InputPart;
import me.st28.flexseries.flexlib.permission.PermissionNode;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A linear layout of a command's logic.
 */
public class LogicPath {

    private LogicHub parent;

    private final List<String> labels = new ArrayList<>();

    private final List<LogicPart> parts = new ArrayList<>();

    private PermissionNode permission;

    public LogicPath(String... labels) {
        this(null, labels);
    }

    public LogicPath(LogicHub parent, String... labels) {
        this.parent = parent;
        Collections.addAll(this.labels, labels);
    }

    public LogicPart getParent() {
        return parent;
    }

    public void setParent(LogicHub parent) {
        Validate.notNull(parent, "Parent cannot be null.");
        if (this.parent != null) {
            throw new IllegalStateException("Parent is already set.");
        }

        this.parent = parent;
    }

    public int getRequiredArgs() {
        int count = 0;

        for (LogicPart part : parts) {
            if (part instanceof InputPart) {
                if (((InputPart) part).isRequired()) {
                    count++;
                }
            }
        }

        return count;
    }

    public String buildUsage() {
        StringBuilder builder = new StringBuilder();

        if (parent == null) {
            builder.append("/").append(labels.get(0));
        }

        for (LogicPart part : parts) {
            if (part instanceof LogicHub) {
                break;
            }

            if (builder.length() > 0) {
                builder.append(" ");
            }

            builder.append(part.toString());
        }

        return builder.toString();
    }

    public List<String> getLabels() {
        return Collections.unmodifiableList(labels);
    }

    public void setPermissionNode(PermissionNode permission) {
        this.permission = permission;
    }

    public LogicPath append(LogicPart part) {
        if (!parts.isEmpty() && parts.get(parts.size() - 1) instanceof LogicHub) {
            throw new IllegalStateException("No more LogicParts can be added because a LogicHub was added already.");
        }

        parts.add(part);
        return this;
    }

    public final void execute(CommandContext context, int curIndex) {
        if (permission != null) {
            CommandUtils.performPermissionTest(context.getSender(), permission);
        }

        for (LogicPart part : parts) {
            part.execute(context, curIndex++);
        }
    }

    public List<String> getSuggestions(CommandContext context, int curIndex) {
        if (!permission.isAllowed(context.getSender())) {
            return null;
        }

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