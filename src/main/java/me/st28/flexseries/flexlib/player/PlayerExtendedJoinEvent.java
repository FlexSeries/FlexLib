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
import me.st28.flexseries.flexlib.player.data.PlayerData;
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