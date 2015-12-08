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