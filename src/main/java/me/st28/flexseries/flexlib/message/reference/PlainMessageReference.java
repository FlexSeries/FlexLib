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
package me.st28.flexseries.flexlib.message.reference;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.Map.Entry;

public class PlainMessageReference extends MessageReference {

    private final String message;
    private final boolean color;

    public PlainMessageReference(String message) {
        this(message, true);
    }

    public PlainMessageReference(String message, boolean color) {
        Validate.notNull(message, "Message cannot be null.");
        this.message = message;
        this.color = color;
    }

    @Override
    public MessageReference duplicate(Map<String, Object> replacements) {
        return new PlainMessageReference(message, color);
    }

    @Override
    public void sendTo(CommandSender sender) {
        sender.sendMessage(getMessage());
    }

    @Override
    public void sendTo(CommandSender sender, Map<String, Object> replacements) {
        sender.sendMessage(handleReplacements(replacements));
    }

    private String handleReplacements(Map<String, Object> replacements) {
        String newMessage = getMessage();

        for (Entry<String, Object> entry : replacements.entrySet()) {
            newMessage = newMessage.replace(entry.getKey(), entry.getValue().toString());
        }

        return newMessage;
    }

    @Override
    public String getMessage() {
        return !color ? message : ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public String getMessage(Map<String, Object> replacements) {
        return handleReplacements(replacements);
    }

}