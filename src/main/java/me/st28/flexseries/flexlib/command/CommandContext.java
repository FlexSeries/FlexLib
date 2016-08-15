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

import me.st28.flexseries.flexlib.player.PlayerReference;
import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds a command's execution context.
 */
public final class CommandContext {

    private final BasicCommand command;
    private final CommandSender sender;
    private final PlayerReference player;
    private final List<String> labels = new ArrayList<>();
    private final String[] rawArgs;
    private final String[] curArgs;
    private final int level;

    private final Map<String, Object> arguments = new HashMap<>();

    public CommandContext(BasicCommand command, CommandSender sender, String label, String[] args, int level) {
        this.command = command;
        if (sender instanceof Player) {
            this.sender = null;
            player = new PlayerReference((Player) sender);
        } else {
            this.sender = sender;
            player = null;
        }
        labels.add(label);
        this.rawArgs = args;
        this.curArgs = args.length == 0 ? new String[0] : Arrays.asList(rawArgs).subList(level, rawArgs.length).toArray(new String[rawArgs.length - level]);
        this.level = level;
    }

    public final BasicCommand getCommand() {
        return command;
    }

    /**
     * @return The CommandSender running the command.
     */
    public final CommandSender getSender() {
        return player != null ? player.getPlayer() : sender;
    }

    /**
     * @return The player reference to the sender running the command.
     *         Null if the sender is not a player.
     */
    public final PlayerReference getPlayer() {
        return player;
    }

    /**
     * @return The label that the sender used to execute the command.
     */
    public final String getLabel() {
        return labels.get(0);
    }

    /**
     * @return The current command level's label.
     */
    public final String getCurrentLabel() {
        return labels.get(level);
    }

    public final String[] getRawArgs() {
        return rawArgs;
    }

    public final String[] getCurArgs() {
        return curArgs;
    }

    public final int getLevel() {
        return level;
    }

    public final Object getArgument(String name) {
        Validate.notNull(name, "Name cannot be null");
        return arguments.get(name);
    }

    public final <T> T getArgument(String name, Class<T> type) {
        Validate.notNull(name, "Name cannot be null");
        Validate.notNull(type, "Type cannot be null");
        return (T) arguments.get(name);
    }

    /**
     * @return The first argument matching the given type.
     *         Null if no argument with the given type was found.
     */
    public final <T> T getArgument(Class<T> type) {
        for (Object val : arguments.values()) {
            if (val.getClass() == type) {
                return (T) val;
            }
        }
        return null;
    }

    public final void setArgument(String name, Object value) {
        arguments.put(name, value);
    }

    public final CommandSession getSession() {
        return getSession("session");
    }

    public final CommandSession getSession(String name) {
        return getArgument(name, CommandSession.class);
    }

}