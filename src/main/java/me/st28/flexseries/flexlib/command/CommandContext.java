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

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;

import java.util.*;

public class CommandContext {

    private FlexCommand command;

    private CommandSender sender;

    private String label;

    private final List<String> args = new ArrayList<>();

    private final Map<String, Object> globalObjects = new HashMap<>();

    private final Set<String> defaultValues = new HashSet<>();

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

    public void indicateDefaultValue(String name) {
        Validate.notNull(name, "Name cannot be null.");
        defaultValues.add(name);
    }

    public boolean isDefaultValue(String name) {
        Validate.notNull(name, "Name cannot be null.");
        return defaultValues.contains(name);
    }

    public <T> T getGlobalObject(String name, Class<T> type) {
        return (T) globalObjects.get(name);
    }

}