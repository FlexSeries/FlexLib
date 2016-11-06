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
package me.st28.flexseries.flexlib.player

import me.st28.flexseries.flexlib.player.lookup.PlayerLookupModule
import me.st28.flexseries.flexlib.player.lookup.UnknownPlayerException
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*

/**
 * Represents a player.
 */
class PlayerReference {

    val uuid: UUID
    val name: String

    constructor(player: Player) {
        uuid = player.uniqueId
        name = player.name
    }

    constructor(uuid: UUID) {
        this.uuid = uuid
        this.name = FlexPlugin.getGlobalModule(PlayerLookupModule::class)!!.getName(uuid) ?: throw UnknownPlayerException(uuid)
    }

    constructor(name: String) {
        val lookup = FlexPlugin.getGlobalModule(PlayerLookupModule::class)!!
        this.uuid = lookup.getUuid(name) ?: throw UnknownPlayerException(name)
        this.name = lookup.getName(uuid) ?: throw UnknownPlayerException(name) // Shouldn't happen?
    }

    override fun toString(): String {
        return "$uuid ($name)"
    }

    fun isOnline(): Boolean {
        return getPlayer() != null
    }

    fun getPlayer(): Player? {
        return Bukkit.getPlayer(uuid)
    }

    fun getOfflinePlayer(): OfflinePlayer {
        return Bukkit.getOfflinePlayer(uuid)
    }

}
