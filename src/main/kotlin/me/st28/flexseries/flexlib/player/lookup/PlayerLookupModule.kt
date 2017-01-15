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
package me.st28.flexseries.flexlib.player.lookup

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import me.st28.flexseries.flexlib.FlexLib
import me.st28.flexseries.flexlib.logging.LogHelper
import me.st28.flexseries.flexlib.plugin.FlexModule
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit

class PlayerLookupModule(plugin: FlexLib) : FlexModule<FlexLib>(plugin, "player-lookup", "Player UUID and name lookup and cache"), Listener {

    private lateinit var cache: Cache<UUID, CacheEntry>

    private val nameToUuids: ConcurrentMap<String, UUID> = ConcurrentHashMap()

    private lateinit var resolver: Resolver
    private lateinit var storage: Storage

    override fun handleEnable() {
        val maxSize = config.getLong("cache.max size", -1)
        val timeout = config.getLong("cache.timeout", 600)

        LogHelper.info(this, "Cache max size: ${if (maxSize > 0) maxSize else "unlimited"}")
        LogHelper.info(this, "Cache timeout: $timeout second(s)")

        val builder = CacheBuilder.newBuilder().expireAfterAccess(timeout, TimeUnit.SECONDS)
        if (maxSize > 0) {
            builder.maximumSize(maxSize)
        }

        cache = builder.removalListener<UUID, CacheEntry> {
            nameToUuids.remove((it.value as CacheEntry).name)
        }.build()

        /* Load storage settings */
        val storageSec = config.getConfigurationSection("storage")
        val storageType = storageSec.getString("type")
        when (storageType) {
            "sqlite" -> storage = Storage_SQLite()
            else -> throw IllegalArgumentException("Invalid storage type '$storageType'")
        }
        storage.enable(this, storageSec.getConfigurationSection(storageType))
        LogHelper.info(this, "Using $storage for storage.")

        /* Load lookup settings */
        val lookupSec = config.getConfigurationSection("lookup")
        if (lookupSec.getBoolean("enabled")) {
            val resolverName = lookupSec.getString("resolver")
            resolver = when (resolverName) {
                Resolver.RESOLVER_Mojang -> Resolver_Mojang()
                Resolver.RESOLVER_MCAPIca -> Resolver_MCAPIca()
                else -> throw IllegalArgumentException("Invalid lookup resolver '$resolverName'")
            }

            resolver.loadConfig(lookupSec.getConfigurationSection(resolver.name))
            LogHelper.info(this, "Lookup enabled, using resolver: ${resolver.name}")
        }
    }

    override fun handleDisable() {
        storage.disable()
    }

    fun getUuid(name: String): UUID? {
        var found = nameToUuids[name.toLowerCase()]
        if (found != null) {
            return found
        }

        try {
            var entry = storage.getEntry(name)
            if (entry == null) {
                // Perform lookup
                entry = resolver.lookup(name)
            }

            if (entry == null) {
                // If still null, invalid completely
                return null
            }

            found = entry.uuid

            cache.put(entry.uuid, entry)
            nameToUuids.put(entry.name.toLowerCase(), entry.uuid)
            storage.update(entry.uuid, entry.name)
        } catch (ex: Exception) {
            LogHelper.severe(this, "An exception occurred while retrieving UUID for name '$name'", ex)
        }
        return found
    }

    fun getName(uuid: UUID): String? {
        try {
            return cache.get(uuid, {
                var entry = storage.getEntry(uuid)
                if (entry == null) {
                    // Perform lookup
                    entry = resolver.lookup(uuid)
                }

                if (entry == null) {
                    // If still null, invalid completely
                    throw RuntimeException()
                }

                nameToUuids.put(entry.name.toLowerCase(), uuid)
                storage.update(entry.uuid, entry.name)

                entry
            }).name
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val uuid = e.player.uniqueId
        val name = e.player.name

        storage.update(uuid, name)
        nameToUuids.put(name.toLowerCase(), uuid)
        cache.put(uuid, CacheEntry(uuid, name))
    }

}

internal data class CacheEntry(val uuid: UUID, val name: String)
