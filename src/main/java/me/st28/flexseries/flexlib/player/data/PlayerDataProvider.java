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
package me.st28.flexseries.flexlib.player.data;

import me.st28.flexseries.flexlib.player.PlayerData;
import me.st28.flexseries.flexlib.player.PlayerReference;

/**
 * Represents something that loads and/or saves player data.
 */
public interface PlayerDataProvider {

    /**
     * Called when a player begins to join the server via AsyncPlayerPreLoginEvent.
     * This method will be called asynchronously.
     *
     * @param loader The {@link PlayerLoader} instance for the player.
     * @param data The {@link PlayerData} instance for the player.
     * @param player A {@link PlayerReference} representation of the player.
     *               Avoid {@link PlayerReference#getPlayer()} as this method is called synchronously.
     */
    default void loadPlayer(PlayerLoader loader, PlayerData data, PlayerReference player) {}

    /**
     * Called after a player joins the server fully via PlayerLoginEvent.
     * This method will be called synchronously.
     *
     * @param loader The {@link PlayerLoader} instance for the player.
     * @param data The {@link PlayerData} instance for the player.
     * @param player A {@link PlayerReference} representation of the player.
     */
    default void handlePlayerJoin(PlayerLoader loader, PlayerData data, PlayerReference player) {}

    /**
     * Saves a player's data. If {@link DataProviderDescriptor#onlineOnly} for this module is false,
     * should also handle offline players.
     *
     * @param loader The {@link PlayerLoader} instance for the player.
     * @param data The {@link PlayerData} instance for the player.
     * @param player A {@link PlayerReference} representation of the player.
     *               {@link PlayerReference#getPlayer()} will be null if the player is offline.
     */
    default void savePlayer(PlayerLoader loader, PlayerData data, PlayerReference player) {}

    /**
     * Unloads a player's data.
     * This method will be called asynchronously in most cases.
     *
     * @param loader The {@link PlayerLoader} instance for the player.
     * @param data The {@link PlayerData} instance for the player.
     * @param player A {@link PlayerReference} representation of the player.
     * @param force This will usually only be true when the server is shutting down, indicating that
     *              the data should be unloaded and this method should return true.
     */
    default boolean unloadPlayer(PlayerLoader loader, PlayerData data, PlayerReference player, boolean force) {
        return true;
    }

}