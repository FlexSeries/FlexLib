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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerExtendedJoinEvent extends PlayerEvent {

    private static HandlerList handlerList = new HandlerList();

    private MessageReference joinMessage;
    private Map<UUID, MessageReference> overriddenJoinMessages = new HashMap<>();

    /**
     * The messages that are to be sent to the newly joined player.<br />
     * The order of the messages sent can be set via the core plugin's configuration file.
     */
    private Map<String, MessageReference> loginMessages = new HashMap<>();

    private PlayerData data;

    public PlayerExtendedJoinEvent(Player who, PlayerData data) {
        super(who);

        Validate.notNull(data, "Data cannot be null.");
        this.data = data;
    }

    public PlayerData getData() {
        return data;
    }

    /**
     * Sets the default join message.
     *
     * @param message The message to set.
     */
    public void setJoinMessage(MessageReference message) {
        this.joinMessage = message;
    }

    /**
     * Sets the join message for a specified player.
     *
     * @param player The player to set the message for.
     * @param message The message to set for the player.
     */
    public void setJoinMessage(UUID player, MessageReference message) {
        overriddenJoinMessages.put(player, message);
    }

    /**
     * @return the default join message.
     */
    public MessageReference getJoinMessage() {
        return joinMessage;
    }

    /**
     * @return the join message for a specified player.
     */
    public MessageReference getJoinMessage(UUID player) {
        return overriddenJoinMessages.containsKey(player) ? overriddenJoinMessages.get(player) : joinMessage;
    }

    /**
     * @return an unmodifiable view of the login messages.
     */
    public Map<String, MessageReference> getLoginMessages() {
        return Collections.unmodifiableMap(loginMessages);
    }

    /**
     * Adds a login message.
     *
     * @param plugin The plugin that owns the message.
     * @param identifier The identifier for the message.
     * @param message The message to send.
     */
    public void addLoginMessage(Class<? extends JavaPlugin> plugin, String identifier, MessageReference message) {
        loginMessages.put(plugin.getName() + "." + identifier, message);
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}