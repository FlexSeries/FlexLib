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

import me.st28.flexseries.flexlib.player.uuidtracker.PlayerUuidTracker;
import me.st28.flexseries.flexlib.player.uuidtracker.UnknownPlayerException;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * A reference to an online or offline player.
 */
public class PlayerReference {

    private UUID uuid;
    private String name;

    public PlayerReference(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null.");

        this.uuid = uuid;
        this.name = getUuidTracker().getLatestName(uuid);

        validate();
    }

    public PlayerReference(String name) {
        Validate.notNull(name, "Name cannot be null.");

        this.uuid = getUuidTracker().getLatestUuid(name);
        this.name = getUuidTracker().getLatestName(this.uuid);

        validate();
    }

    public PlayerReference(Player player) {
        Validate.notNull(player, "Player cannot be null.");

        this.uuid = player.getUniqueId();
        this.name = player.getName();

        validate();
    }

    public PlayerReference(OfflinePlayer player) {
        Validate.notNull(player, "Player cannot be null.");

        this.uuid = player.getUniqueId();
        this.name = player.getName();

        validate();
    }

    private PlayerUuidTracker getUuidTracker() {
        return FlexPlugin.getGlobalModule(PlayerUuidTracker.class);
    }

    private void validate() {
        if (uuid == null || name == null) {
            throw new UnknownPlayerException(uuid, name);
        }
    }

    /**
     * @return The UUID of the player.
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * @return The latest name of the player.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The player that this reference references.<br />
     *         Null if the player is not online.
     */
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    /**
     * @return The player that this reference references.
     */
    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

}