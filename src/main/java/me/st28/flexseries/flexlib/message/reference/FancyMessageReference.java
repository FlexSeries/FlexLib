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

import com.stealthyone.mcb.mcml.shade.fanciful.FancyMessage;
import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class FancyMessageReference extends MessageReference {

    private FancyMessage message;

    public FancyMessageReference(FancyMessage message) {
        Validate.notNull(message, "Message cannot be null.");
        this.message = message;
    }

    @Override
    public MessageReference duplicate(Map<String, Object> replacements) {
        return new FancyMessageReference(message);
    }

    @Override
    public void sendTo(CommandSender sender) {
        message.send(sender);
    }

    @Override
    public void sendTo(CommandSender sender, Map<String, Object> replacements) {
        // TODO: Use replacements
        message.send(sender);
    }

    @Override
    public String getMessage() {
        return message.toOldMessageFormat();
    }

    @Override
    public String getMessage(Map<String, Object> replacements) {
        // TODO: Use replacements
        return getMessage();
    }

}