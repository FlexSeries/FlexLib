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
package me.st28.flexseries.flexlib.player.lookup;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.logging.LogHelper;
import me.st28.flexseries.flexlib.player.lookup.Resolver.Resolver_MCAPIca;
import me.st28.flexseries.flexlib.player.lookup.Resolver.Resolver_MCAPInet;
import me.st28.flexseries.flexlib.player.lookup.Storage.SqliteStorage;
import me.st28.flexseries.flexlib.plugin.FlexModule;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public final class PlayerLookupModule extends FlexModule<FlexLib> implements Listener {

    private Cache<UUID, CacheEntry> cache;
    private final ConcurrentMap<String, UUID> nameToUuids = new ConcurrentHashMap<>();

    private Resolver resolver;
    private Storage storage;

    public PlayerLookupModule(FlexLib plugin) {
        super(plugin, "player-lookup", "Player UUID and name cache and lookup");
    }

    @Override
    protected void handleEnable() {
        final FileConfiguration config = getConfig();

        final long maxSize = config.getLong("cache.max size", -1);
        final long timeout = config.getLong("cache.timeout", 600);

        LogHelper.info(this, "Cache max size: " + (maxSize > 0 ? maxSize : "(unlimited)"));
        LogHelper.info(this, "Cache timeout: " + timeout + " second(s)");

        CacheBuilder builder = CacheBuilder.newBuilder()
                .expireAfterAccess(timeout, TimeUnit.SECONDS);

        if (maxSize > 0) {
            builder.maximumSize(maxSize);
        }

        cache = builder.removalListener(notification -> {
            nameToUuids.remove(((CacheEntry) notification.getValue()).name);
        }).build();

        // Load storage settings
        final ConfigurationSection storageSec = config.getConfigurationSection("storage");
        final String storageType = storageSec.getString("type");
        switch (storageType) {
            case "sqlite":
                storage = new SqliteStorage();
                break;

            default:
                throw new IllegalArgumentException("Invalid storage type '" + storageType + "'");
        }

        LogHelper.info(this, "Using " + storage.getName() + " for storage.");
        try {
            storage.enable(this, storageSec.getConfigurationSection(storage.getName()));
        } catch (Exception ex) {
            throw new RuntimeException("An exception occurred while enabling the storage manager", ex);
        }

        // Load lookup settings
        final ConfigurationSection lookupSec = config.getConfigurationSection("lookup");
        if (lookupSec.getBoolean("enabled")) {
            final String resolverName = lookupSec.getString("resolver");
            switch (resolverName) {
                case Resolver_MCAPInet.NAME:
                    resolver = new Resolver_MCAPInet();
                    break;

                case Resolver_MCAPIca.NAME:
                    resolver = new Resolver_MCAPIca();
                    break;

                default:
                    throw new IllegalArgumentException("Invalid lookup resolver '" + resolverName + "'");
            }

            LogHelper.info(this, "Lookup enabled, using resolver: " + resolverName);
        }
    }

    @Override
    protected void handleReload(boolean isFirstReload) {
        if (resolver != null) {
            resolver.loadConfig(getConfig().getConfigurationSection("lookup." + resolver.getName()));
        }
    }

    @Override
    protected void handleDisable() {
        try {
            storage.disable();
        } catch (Exception ex) {
            LogHelper.severe(this, "An exception occurred while disabling the storage manager", ex);
        }

        cache.cleanUp();
    }

    /**
     * Returns the UUID associated with a given name.
     * This method should be called asynchronously since a database lookup or HTTP lookup may occur.
     *
     * @return The UUID associated with the given name.
     *         Null if no UUID was found for the name.
     */
    public UUID getUuid(String name) {
        Validate.notNull(name, "Name cannot be null");

        UUID found = nameToUuids.get(name.toLowerCase());
        if (found != null) {
            return found;
        }

        try {
            CacheEntry entry = storage.getEntry(name);
            if (entry == null && resolver != null) {
                // Lookup
                entry = resolver.lookup(name);
            }

            if (entry == null) {
                // If still null, invalid completely
                return null;
            }

            found = entry.uuid;

            cache.put(entry.uuid, entry);
            nameToUuids.put(entry.name.toLowerCase(), entry.uuid);
            storage.update(entry.uuid, entry.name);
        } catch (Exception ex) {
            LogHelper.severe(this, "An exception occurred while retrieving UUID for name '" + name + "'", ex);
        }
        return found;
    }

    /**
     * Returns the name associated with a given UUID.
     * This method should be called asynchronously since a database lookup or HTTP lookup may occur.
     *
     * @return The name associated with the given UUID.
     *         Null if no name was found for the UUID.
     */
    public String getName(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null");

        try {
            return cache.get(uuid, () -> {
                CacheEntry entry = storage.getEntry(uuid);
                if (entry == null && resolver != null) {
                    // Lookup
                    entry = resolver.lookup(uuid);
                }

                if (entry == null) {
                    // If still null, invalid completely
                    throw new RuntimeException();
                }

                nameToUuids.put(entry.name.toLowerCase(), uuid);
                storage.update(entry.uuid, entry.name);
                return entry;
            }).name;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public Map<UUID, String> getUuidToNameCache() {
        Map<UUID, String> map = new HashMap<>((int) cache.size());
        for (Entry<UUID, CacheEntry> entry : cache.asMap().entrySet()) {
            map.put(entry.getKey(), entry.getValue().name);
        }
        return map;
    }

    public Map<String, UUID> getNameToUuidCache() {
        return new HashMap<>(nameToUuids);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();

        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            try {
                storage.update(p.getUniqueId(), p.getName());
            } catch (Exception ex) {
                LogHelper.severe(this, "An exception occurred while updating player '" + p.getName() + "'", ex);
            }
        });
    }

}