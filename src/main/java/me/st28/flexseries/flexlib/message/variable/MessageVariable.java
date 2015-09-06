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