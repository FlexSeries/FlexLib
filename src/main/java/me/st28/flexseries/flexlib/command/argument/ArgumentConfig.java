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
package me.st28.flexseries.flexlib.command.argument;

import me.st28.flexseries.flexlib.command.CommandContext;
import me.st28.flexseries.flexlib.utils.GenericDataContainer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgumentConfig extends GenericDataContainer {

    private final static Pattern PATTERN_INFO = Pattern.compile("([a-zA-Z0-9-_]+) ([a-zA-Z0-9-_]+) (always|player|nonplayer)");
    private final static Pattern PATTERN_INFO_AUTO = Pattern.compile("([a-zA-Z0-9-_]+) ([a-zA-Z0-9-_]+)");
    private final static Pattern PATTERN_OPTION = Pattern.compile("-([a-zA-Z0-9-_]+)(?:=([a-zA-Z0-9-_]+))?");

    public static ArgumentConfig[] parse(String[] raw) {
        if (raw.length == 1 && raw[0].isEmpty()) {
            return new ArgumentConfig[0];
        }

        List<ArgumentConfig> ret = new ArrayList<>();
        for (int i = 0; i < raw.length; ++i) {
            ret.add(new ArgumentConfig(raw[i], i));
        }
        return ret.toArray(new ArgumentConfig[ret.size()]);
    }

    public static ArgumentConfig[] parseAuto(String[] raw) {
        if (raw.length == 1 && raw[0].isEmpty()) {
            return new ArgumentConfig[0];
        }

        List<ArgumentConfig> ret = new ArrayList<>();
        for (int i = 0; i < raw.length; ++i) {
            ret.add(new ArgumentConfig(raw[i]));
        }
        return ret.toArray(new ArgumentConfig[ret.size()]);
    }

    // ------------------------------------------------------------------------------------------ //

    private final static int REQUIRED_ALWAYS = 0;
    private final static int REQUIRED_PLAYER = 1;
    private final static int REQUIRED_NONPLAYER = 2;

    private final int index;
    private final int isRequired;
    private final String name;
    private final String type;

    /**
     * This constructor is for auto arguments ONLY.
     */
    private ArgumentConfig(String raw) {
        index = -1;
        isRequired = REQUIRED_ALWAYS;

        Matcher matcher = PATTERN_INFO_AUTO.matcher(raw);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid auto argument syntax '" + raw + "'");
        }

        name = matcher.group(1);
        type = matcher.group(2);

        parseOptions(raw);
    }

    private ArgumentConfig(String raw, int index) {
        this.index = index;

        Matcher matcher = PATTERN_INFO.matcher(raw);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid argument syntax '" + raw + "'");
        }

        name = matcher.group(1);
        type = matcher.group(2);
        switch (matcher.group(3)) {
            case "always":
                isRequired = REQUIRED_ALWAYS;
                break;
            case "player":
                isRequired = REQUIRED_PLAYER;
                break;
            case "nonplayer":
                isRequired = REQUIRED_NONPLAYER;
                break;
            default:
                // Should never happen
                throw new IllegalArgumentException("Invalid requirement '" + matcher.group(3) + "'");
        }

        parseOptions(raw);
    }

    private void parseOptions(String raw) {
        Matcher optionMatcher = PATTERN_OPTION.matcher(raw);
        while (optionMatcher.find()) {
            Object value = null;

            final String rawValue = optionMatcher.group(2);

            if (rawValue == null) {
                data.put(optionMatcher.group(1), null);
                continue;
            }

            // Boolean
            switch (rawValue) {
                case "true":
                    value = true;
                    break;
                case "false":
                    value = false;
                    break;
            }

            // Integer
            if (value == null) {
                try {
                    value = Integer.valueOf(rawValue);
                } catch (NumberFormatException ex) { }
            }

            // Floating point
            if (value == null) {
                try {
                    value = Double.valueOf(rawValue);
                } catch (NumberFormatException ex) { }
            }

            // String
            if (value == null) {
                value = rawValue;
            }

            data.put(optionMatcher.group(1), value);
        }
    }

    public int getIndex() {
        return index;
    }

    public String getUsage(CommandContext context) {
        return String.format(isRequired(context) ? "<%s>" : "[%s]", name);
    }

    public boolean isRequired(CommandContext context) {
        switch (isRequired) {
            default:
            case REQUIRED_ALWAYS:
                return true;
            case REQUIRED_PLAYER:
                return context.getSender() instanceof Player;
            case REQUIRED_NONPLAYER:
                return !(context.getSender() instanceof Player);
        }
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

}