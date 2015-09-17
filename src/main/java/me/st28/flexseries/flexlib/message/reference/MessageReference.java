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