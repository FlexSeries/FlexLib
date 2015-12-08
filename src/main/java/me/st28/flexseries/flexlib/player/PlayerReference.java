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