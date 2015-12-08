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

import com.stealthyone.mcb.mcml.MCMLBuilder;
import com.stealthyone.mcb.mcml.shade.fanciful.FancyMessage;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
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

        getFancyMessage(replacements).send(sender);
    }

    private FancyMessage getFancyMessage(Map<String, Object> replacements) {
        if (replacements == null) {
            if (this.replacements == null) {
                return new MCMLBuilder(message).getFancyMessage();
            } else {
                return new MCMLBuilder(message, this.replacements).getFancyMessage();
            }
        }

        Map<String, Object> allReplacements = new HashMap<>();
        allReplacements.putAll(this.replacements);
        allReplacements.putAll(replacements);

        return new MCMLBuilder(message, allReplacements).getFancyMessage();
    }

    @Override
    public String getMessage() {
        return getFancyMessage(null).toOldMessageFormat();
    }

    @Override
    public String getMessage(Map<String, Object> replacements) {
        return getFancyMessage(replacements).toOldMessageFormat();
    }

    @Override
    public void broadcast(Map<String, Object> replacements) {
        getFancyMessage(replacements).send(Bukkit.getOnlinePlayers());
    }

}