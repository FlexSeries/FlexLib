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
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit

class PlayerLookupModule(plugin: FlexLib) : FlexModule<FlexLib>(plugin, "player-lookup", "Player UUID and name lookup and cache") {

    private lateinit var cache: Cache<UUID, CacheEntry>

    private val nameToUuids: ConcurrentMap<String, UUID> = ConcurrentHashMap()

    private lateinit var resolver: Resolver
    private lateinit var storage: Storage

    override fun handleEnable() {
        val config = getConfig()

        val maxSize = config.getLong("cache.max size", -1)
        val timeout = config.getLong("cache.timeout", 600)

        LogHelper.info(this, "Cache max size: ${if (maxSize > 0) maxSize else "unlimited"}")
        LogHelper.info(this, "Cache timeout: $timeout second(s)")

        val builder = CacheBuilder.newBuilder().expireAfterAccess(timeout, TimeUnit.SECONDS)
        if (maxSize > 0) {
            builder.maximumSize(maxSize)
        }

        cache = builder.removalListener {
            nameToUuids.remove((it.value as CacheEntry).name)
        }.build()

        // Load storage settings
        val storageSec = config.getConfigurationSection("storage")
        val storageType = storageSec.getString("type")
        when (storageType) {
            "sqlite" -> storage = Storage_SQLite()
            else -> throw IllegalArgumentException("Invalid storage type '$storageType'")
        }
    }

    fun getUuid(name: String): UUID? {
        var found = nameToUuids[name.toLowerCase()]
        if (found != null) {
            return found
        }

        try {
            var entry = storage.getEntry(name)
            if (entry == null && resolver != null) {
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
            return cache.get(uuid, Callable<CacheEntry> {
                var entry = storage.getEntry(uuid)
                if (entry == null && resolver != null) {
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

}

internal data class CacheEntry(val uuid: UUID, val name: String)
