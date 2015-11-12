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
        // TODO: Implement MySQL support and add async saving for YAML
        switch (storageType) {
            case "YAML":
                storageHandler = new YamlStorageHandler(this);
                break;

            case "MYSQL":
                storageHandler = new MySqlStorageHandler(this);
                throw new UnsupportedOperationException("MySQL support is not currently implemented.");

            default:
                throw new IllegalArgumentException("Invalid storage type: '" + storageType + "'");
        }

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

                    if (existingData != null) {
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
        final UUID uuid = player.getUniqueId();
        final String name = player.getName();

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

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
        UUID uuid = e.getUniqueId();

        if (uuid == null || e.getName() == null) {
            return;
        }

        UuidEntry entry = storageHandler.loadSingle(uuid);
        if (entry != null) {
            rawData.put(uuid, entry);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        updatePlayer(e.getPlayer());
    }

}