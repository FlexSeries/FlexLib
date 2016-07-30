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
package me.st28.flexseries.flexlib.player;

import me.st28.flexseries.flexlib.player.lookup.PlayerLookupModule;
import me.st28.flexseries.flexlib.player.lookup.UnknownPlayerException;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.utils.ArgumentCallback;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * A reference to a player.
 */
public class PlayerReference {

    /**
     * Utility method for constructing a player reference from a UUID.
     *
     * @param plugin The plugin requesting the player reference.
     * @param callback The callback that receives the reference.
     */
    public static void get(Class<? extends JavaPlugin> plugin, UUID uuid, ArgumentCallback<PlayerReference> callback) {
        Validate.notNull(uuid, "UUID cannot be null");

        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(plugin), () -> {
            callback.call(plugin, new PlayerReference(uuid));
        });
    }

    /**
     * Utility method for constructing a player reference from a name.
     *
     * @param plugin The plugin requesting the player reference.
     * @param callback The callback that receives the reference.
     */
    public static void get(Class<? extends JavaPlugin> plugin, String name, ArgumentCallback<PlayerReference> callback) {
        Validate.notNull(name, "Name cannot be null");

        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(plugin), () -> {
            callback.call(plugin, new PlayerReference(name));
        });
    }

    // ------------------------------------------------------------------------------------------ //

    private final UUID uuid;
    private final String name;

    /**
     * Constructs a player reference from a player.
     */
    public PlayerReference(Player player) {
        Validate.notNull(player, "Player cannot be null");
        uuid = player.getUniqueId();
        name = player.getName();
    }

    /**
     * Constructs a player reference from a UUID.
     * This method may perform a lookup, so it should be called asynchronously.
     */
    public PlayerReference(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null");
        this.uuid = uuid;
        this.name = FlexPlugin.getGlobalModule(PlayerLookupModule.class).getName(uuid);
    }

    /**
     * Constructs a player reference from a name.
     * This method may perform a lookup, so it should be called asynchronously.
     *
     * @throws UnknownPlayerException Thrown if no player matched the given name.
     */
    public PlayerReference(String name) {
        Validate.notNull(name, "Name cannot be null");

        final PlayerLookupModule lookup = FlexPlugin.getGlobalModule(PlayerLookupModule.class);
        this.uuid = lookup.getUuid(name);
        if (uuid == null) {
            throw new UnknownPlayerException(name);
        }
        this.name = lookup.getName(uuid);
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    /**
     * @return True if the referenced player is online.
     *         False otherwise.
     */
    public boolean isOnline() {
        return getPlayer() != null;
    }

    /**
     * @return The referenced player.
     *         Null if the player is offline.
     */
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    /**
     * @return The referenced player.
     */
    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

}