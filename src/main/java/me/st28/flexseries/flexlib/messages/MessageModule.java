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
package me.st28.flexseries.flexlib.messages;

import me.st28.flexseries.flexlib.plugin.FlexModule;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MessageModule<T extends FlexPlugin> extends FlexModule<T> {

    private final static Pattern PATTERN_VARIABLE = Pattern.compile("\\{([0-9]+)\\}");

    private final Map<String, String> messages = new HashMap<>();

    public MessageModule(T plugin) {
        super(plugin, "messages", "Manages a plugin's messages");
    }

    public Message getMessage(String name, Object... replacements) {
        String message;
        if (messages.containsKey(name)) {
            message = PATTERN_VARIABLE.matcher(messages.get(name)).replaceAll("%\\$$1s");
        } else {
            message = name;
        }
        return new Message(message, replacements);
    }

}