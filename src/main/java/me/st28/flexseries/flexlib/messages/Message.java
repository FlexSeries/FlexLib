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

import com.stealthyone.mcb.mcml.MCMLBuilder;
import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Message {

    public static Message get(Class<? extends FlexPlugin> plugin, String name, Object... replacements) {
        final MessageModule module = FlexPlugin.getPluginModule(plugin, MessageModule.class);
        return module == null ? new Message(name, replacements) : module.getMessage(name, replacements);
    }

    public static Message getGlobal(String name, Object... replacements) {
        return get(FlexLib.class, name, replacements);
    }

    // ------------------------------------------------------------------------------------------ //

    private final String message;
    private final Object[] replacements;

    public Message(String message, Object... replacements) {
        this.message = message;
        this.replacements = replacements;
    }

    public void sendTo(CommandSender sender, Object... replacements) {
        sendTo(Collections.singletonList(sender), replacements);
    }

    public void sendTo(Collection<? extends CommandSender> senders, Object... replacements) {
        final int replacementCount = this.replacements.length + replacements.length;

        if (replacementCount == 0) {
            new MCMLBuilder(message).toFancyMessage().send(senders);
            return;
        }

        List<Object> finalReplacements = new ArrayList<>(replacementCount);
        Collections.addAll(finalReplacements, this.replacements);
        Collections.addAll(finalReplacements, replacements);

        new MCMLBuilder(String.format(message, finalReplacements.toArray(new Object[replacementCount]))).toFancyMessage().send(senders);
    }

}