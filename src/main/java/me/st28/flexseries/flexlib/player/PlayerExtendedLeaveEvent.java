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
package me.st28.flexseries.flexlib.player;

import me.st28.flexseries.flexlib.message.reference.MessageReference;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerExtendedLeaveEvent extends PlayerEvent {

    private static HandlerList handlerList = new HandlerList();

    private PlayerData data;

    private MessageReference leaveMessage;
    private Map<UUID, MessageReference> overriddenLeaveMessages = new HashMap<>();

    public PlayerExtendedLeaveEvent(Player who, PlayerData data) {
        super(who);

        Validate.notNull(data, "Data cannot be null.");
        this.data = data;
    }

    public PlayerData getData() {
        return data;
    }

    public void setLeaveMessage(MessageReference message) {
        this.leaveMessage = message;
    }

    public void setLeaveMessage(UUID player, MessageReference message) {
        overriddenLeaveMessages.put(player, message);
    }

    public MessageReference getLeaveMessage() {
        return leaveMessage;
    }

    public MessageReference getLeaveMessage(UUID player) {
        return overriddenLeaveMessages.containsKey(player) ? overriddenLeaveMessages.get(player) : leaveMessage;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}