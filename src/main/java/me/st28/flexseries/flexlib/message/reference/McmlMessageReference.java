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
package me.st28.flexseries.flexlib.message.reference;

import com.stealthyone.mcb.mcml.MCMLBuilder;
import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class McmlMessageReference extends MessageReference {

    private final String message;
    private final Map<String, Object> replacements = new HashMap<>();

    public McmlMessageReference(String message) {
        this(message, null);
    }

    public McmlMessageReference(String message, Map<String, Object> replacements) {
        Validate.notNull(message, "Message cannot be null.");
        this.message = message;

        if (replacements != null) {
            this.replacements.putAll(replacements);
        }
    }

    @Override
    public MessageReference duplicate(Map<String, Object> replacements) {
        return new McmlMessageReference(message, replacements);
    }

    @Override
    public void sendTo(CommandSender sender) {
        sendTo(sender, null);
    }

    @Override
    public void sendTo(CommandSender sender, Map<String, Object> replacements) {
        Validate.notNull(sender, "Sender cannot be null.");

        if (replacements == null) {
            if (this.replacements == null) {
                new MCMLBuilder(message).getFancyMessage().send(sender);
            } else {
                new MCMLBuilder(message, this.replacements).getFancyMessage().send(sender);
            }
            return;
        }

        Map<String, Object> allReplacements = new HashMap<>();
        allReplacements.putAll(this.replacements);
        allReplacements.putAll(replacements);

        new MCMLBuilder(message, allReplacements).getFancyMessage().send(sender);
    }

}