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
package me.st28.flexseries.flexlib.player.uuidtracker;

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.plugin.module.FlexModule;
import me.st28.flexseries.flexlib.plugin.module.ModuleDescriptor;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides a name to UUID and vice versa lookup for players <i>that have joined the server before.</i>
 * This class should be used to avoid hitting the Mojang API UUID lookup limit.
 */
public final class PlayerUuidTracker extends FlexModule<FlexLib> implements Listener {

    private UuidTrackerStorageHandler storageHandler;

    private final Map<UUID, UuidEntry> rawData = new ConcurrentHashMap<>();

    private final Map<UUID, String> uuidsToNames = new HashMap<>();
    private final Map<String, UUID> namesToUuids = new HashMap<>();

    public PlayerUuidTracker(FlexLib plugin) {
        super(
                plugin,
                "uuid_tracker",
                "Name to UUID and vice versa indexes for players that join the server",
                new ModuleDescriptor()
                        .setGlobal(true)
                        .setSmartLoad(false)
        );
    }

    @Override
    protected void handleEnable() {
        FileConfiguration config = getConfig();
        String storageType = config.getString("storage.type", "YAML").toUpperCase();
        switch (storageType) {
            case "YAML":
                storageHandler = new YamlStorageHandler(this);
                break;

            case "MYSQL":
                storageHandler = new MySqlStorageHandler(this);
                break;

            default:
                throw new IllegalArgumentException("Invalid storage type: '" + storageType + "'");
        }

        storageHandler.enable();

        for (UuidEntry entry : storageHandler.loadAll()) {
            rawData.put(entry.uuid, entry);
        }

        for (UuidEntry entry : rawData.values()) {
            UUID uuid = entry.uuid;

            for (Entry<String, Long> nameEntry : entry.names.entrySet()) {
                final String name = nameEntry.getKey();
                final UUID existingUuid = namesToUuids.get(name.toLowerCase());

                if (existingUuid != null && !existingUuid.equals(uuid)) {
                    UuidEntry existingData = rawData.get(existingUuid);

                    if (existingData != null && existingData.names.containsKey(name)) {
                        if (nameEntry.getValue() < existingData.names.get(name)) {
                            // This name is old, ignore it.
                            continue;
                        }
                    }
                }

                namesToUuids.put(name.toLowerCase(), uuid);
            }

            uuidsToNames.put(uuid, entry.currentName);
        }
    }

    @Override
    protected void handleSave(boolean async) {
        storageHandler.save(async, new HashSet<>(rawData.values()));
    }

    private void updatePlayer(Player player) {
        updatePlayer(player.getUniqueId(), player.getName());
    }

    private void updatePlayer(UUID uuid, String name) {
        if (!rawData.containsKey(uuid)) {
            rawData.put(uuid, new UuidEntry(uuid));
        }

        UuidEntry entry = rawData.get(uuid);

        entry.currentName = name;
        entry.names.put(name, System.currentTimeMillis());

        // Update only name + time
        UuidEntry toUpdate = new UuidEntry(uuid);
        toUpdate.names.put(name, System.currentTimeMillis());
        storageHandler.queueUpdate(toUpdate);

        uuidsToNames.put(uuid, name);
        namesToUuids.put(name.toLowerCase(), uuid);
    }

    /**
     * @return The cached UUID for the given name.
     */
    public UUID getLatestUuid(String name) {
        Validate.notNull(name, "Name cannot be null.");
        return namesToUuids.get(name.toLowerCase());
    }

    /**
     * @return All cached UUIDs.
     */
    public Collection<UUID> getAllUuids() {
        return Collections.unmodifiableCollection(uuidsToNames.keySet());
    }

    /**
     * @return The cached name for the given UUID.
     */
    public String getLatestName(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null.");
        return uuidsToNames.get(uuid);
    }

    /**
     * @return All of the names that have been tied to a given UUID.
     */
    public Set<String> getAllNames(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null.");

        Set<String> names = new HashSet<>();

        UuidEntry entry = rawData.get(uuid);
        names.addAll(entry.names.keySet());

        return names;
    }

    /**
     * @return All cached names.
     */
    public Set<String> getAllNames() {
        Set<String> returnSet = new HashSet<>();

        for (UuidEntry entry : rawData.values()) {
            returnSet.addAll(entry.names.keySet());
        }

        return returnSet;
    }

    /**
     * @return All of the latest names.
     */
    public Set<String> getAllLatestNames() {
        return new HashSet<>(uuidsToNames.values());
    }

    /**
     * @return All of the latest names and their UUIDs.
     */
    public Map<String, UUID> getAllLatestNameUuids() {
        return Collections.unmodifiableMap(namesToUuids);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
        UUID uuid = e.getUniqueId();

        if (uuid == null || e.getName() == null) {
            return;
        }

        UuidEntry entry = storageHandler.loadSingle(uuid);
        if (entry != null) {
            rawData.put(uuid, entry);
        }

        updatePlayer(uuid, e.getName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        updatePlayer(e.getPlayer());
    }

}