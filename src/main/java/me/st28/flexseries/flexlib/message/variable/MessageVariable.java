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
package me.st28.flexseries.flexlib.message.variable;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents a text variable that will be replaced in chat messages by something else.
 */
public abstract class MessageVariable {

    private static final Map<String, MessageVariable> variables = new HashMap<>();

    public static boolean registerVariable(MessageVariable variable) {
        Validate.notNull(variable, "Variable cannot be null.");
        if (variables.containsKey(variable.key)) {
            return false;
        }

        variables.put(variable.key, variable);
        return true;
    }

    public static String handleReplacements(Player player, String input) {
        for (Entry<String, MessageVariable> entry : variables.entrySet()) {
            if (input.contains(entry.getKey())) {
                String replacement = entry.getValue().getReplacement(player);

                if (replacement != null) {
                    input = input.replace(entry.getKey(), replacement);
                }
            }
        }

        return input;
    }

    private String key;

    public MessageVariable(String key) {
        Validate.notNull(key, "Key cannot be null.");
        this.key = "{" + key.toUpperCase() + "}";
    }

    /**
     * @param player The player. Can be null, so this method should handle that appropriately.
     *
     * @return A replacement for the given player.<br />
     *         Null if there is no replacement for the player.
     */
    public abstract String getReplacement(Player player);

}