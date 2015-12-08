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

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.omg.CORBA.OBJ_ADAPTER;

import java.util.Map;

/**
 * Represents a reference to a message that can be sent directly to a CommandSender.
 */
public abstract class MessageReference {

    /**
     * @return A copy of this MessageReference using the given replacements.
     */
    public abstract MessageReference duplicate(Map<String, Object> replacements);

    /**
     * Sends the message to a given CommandSender.
     */
    public abstract void sendTo(CommandSender sender);

    /**
     * Sends the message to a given CommandSender with replacements.
     */
    public abstract void sendTo(CommandSender sender, Map<String, Object> replacements);

    /**
     * @return The represented message as a string.
     */
    public abstract String getMessage();

    /**
     * @return The represented message as a string with replacements.
     */
    public abstract String getMessage(Map<String, Object> replacements);

    public void broadcast() {
        broadcast(null);
    }

    public void broadcast(Map<String, Object> replacements) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTo(player, replacements);
        }
    }

}