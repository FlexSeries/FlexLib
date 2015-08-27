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

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;

import java.util.*;

public class CommandContext {

    private FlexCommand command;

    private CommandSender sender;

    private String label;

    private final List<String> args = new ArrayList<>();

    private final Map<String, Object> globalObjects = new HashMap<>();

    public CommandContext(FlexCommand command, CommandSender sender, String label, String... args) {
        Validate.notNull(command, "Command cannot be null.");
        Validate.notNull(sender, "Sender cannot be null.");
        Validate.notNull(label, "Label cannot be null.");

        this.command = command;
        this.sender = sender;
        this.label = label;
        Collections.addAll(this.args, args);
    }

    public FlexCommand getCommand() {
        return command;
    }

    public CommandSender getSender() {
        return sender;
    }

    public String getLabel() {
        return label;
    }

    public List<String> getArgs() {
        return Collections.unmodifiableList(args);
    }

    public void addGlobalObject(String name, Object object) {
        globalObjects.put(name, object);
    }

    public boolean deleteGlobalObject(String name) {
        return globalObjects.remove(name) != null;
    }

    public <T> T getGlobalObject(String name, Class<T> type) {
        return (T) globalObjects.get(name);
    }

}