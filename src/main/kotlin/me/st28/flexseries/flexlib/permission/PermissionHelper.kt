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
package me.st28.flexseries.flexlib.permission

import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.util.*

object PermissionHelper {

    private var vaultPerm: net.milkbowl.vault.permission.Permission? = null
    private var vaultChat: net.milkbowl.vault.chat.Chat? = null

    internal val groupEntries: MutableMap<String, GroupEntry> = LinkedHashMap()

    // Group, groups that inherit from it
    internal val inheritanceIndex: MutableMap<String, MutableList<String>> = HashMap()

    internal fun reload(config: ConfigurationSection?) {
        if (config == null) {
            return
        }

        // Setup vault
        if (vaultPerm == null) {
            vaultPerm = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission::class.java)!!.provider
        }

        if (vaultChat == null) {
            vaultChat = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat::class.java)!!.provider
        }

        groupEntries.clear()
        inheritanceIndex.clear()

        val rawObj = config.get("inheritance")!!

        // Inferred permissions
        if (rawObj is List<*>) {
            for (group in rawObj as List<String>) {
                groupEntries.put(group, PermissionGroupEntry("flexlib.group." + group))
            }
            return
        }

        if (rawObj !is ConfigurationSection) {
            throw IllegalArgumentException("Invalid configuration for inheritance")
        }

        val sec = rawObj

        for (group in sec.getKeys(false)) {
            val permission = sec.getString("$group.permission")
            if (permission != null) {
                groupEntries.put(group, PermissionGroupEntry(permission))
                continue
            }

            if (!sec.isSet("groups")) {
                continue
            }

            val inherit = sec.getStringList("groups")
            if (inherit != null) {
                groupEntries.put(group, GroupsGroupEntry(group, inherit))
            }
        }
    }

    fun getPlayerGroups(player: Player): List<String> {
        return vaultPerm!!.getPlayerGroups(null, player).map(String::toLowerCase)
    }

    fun isPlayerInGroup(player: Player, group: String, checkInheritance: Boolean): Boolean {
        val entry = groupEntries[group.toLowerCase()] ?: return getPlayerGroups(player).contains(group.toLowerCase())

        return entry.containsPlayer(player) || (checkInheritance && entry.playerInherits(player))
    }

    fun getPrimaryGroup(player: Player): String {
        return vaultChat!!.getPrimaryGroup(player)
    }

    fun getTopGroup(player: Player, groups: List<String>, defaultGroup: String? = null): String {
        val reversed = groupEntries.keys.reversed()
        for (group in reversed) {
            val entry = groupEntries[group]!!
            if (groups.contains(group) && (entry.containsPlayer(player) || entry.playerInherits(player))) {
                return group
            }
        }
        return defaultGroup ?: getPrimaryGroup(player)
    }

}

internal interface GroupEntry {

    /**
     * @return True if the player is in this group
     */
    fun containsPlayer(player: Player): Boolean

    /**
     * @return True ift he player is in this group via inheritance.
     */
    fun playerInherits(player: Player): Boolean

}

internal class PermissionGroupEntry : GroupEntry {

    private val permission: String

    constructor(permission: String) {
        this.permission = permission
    }

    override fun containsPlayer(player: Player): Boolean {
        return player.hasPermission(permission)
    }

    override fun playerInherits(player: Player): Boolean {
        return containsPlayer(player)
    }

}

internal class GroupsGroupEntry: GroupEntry {

    private val group: String

    constructor(group: String, inherits: List<String>) {
        this.group = group.toLowerCase()

        for (inherit in inherits) {
            val key = inherit.toLowerCase()
            if (!PermissionHelper.inheritanceIndex.containsKey(key)) {
                PermissionHelper.inheritanceIndex.put(inherit, ArrayList())
            }
            PermissionHelper.inheritanceIndex[inherit]!!.add(group)
        }
    }

    override fun containsPlayer(player: Player): Boolean {
        return PermissionHelper.getPlayerGroups(player).contains(group)
    }

    override fun playerInherits(player: Player): Boolean {
        val playerGroups = PermissionHelper.getPlayerGroups(player)
        val inheritance = PermissionHelper.inheritanceIndex[group]!!

        for (inherit in inheritance) {
            if (playerGroups.contains(inherit)) {
                return true
            }
        }
        return false
    }

}
