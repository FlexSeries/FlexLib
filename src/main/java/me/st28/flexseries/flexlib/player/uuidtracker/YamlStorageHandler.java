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

import me.st28.flexseries.flexlib.log.LogHelper;
import me.st28.flexseries.flexlib.storage.flatfile.YamlFileManager;
import me.st28.flexseries.flexlib.utils.TaskChain;
import me.st28.flexseries.flexlib.utils.TaskChain.AsyncGenericTask;
import me.st28.flexseries.flexlib.utils.TaskChain.VariableGenericTask;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

class YamlStorageHandler extends UuidTrackerStorageHandler {

    private YamlFileManager file;

    YamlStorageHandler(PlayerUuidTracker manager) {
        super(manager, "YAML");
    }

    @Override
    void enable() {
        // TODO: Make file location configurable
        file = new YamlFileManager(manager.getPlugin().getDataFolder() + File.separator + "playerUuids.yml");
    }

    @Override
    Set<UuidEntry> loadAll() {
        final Set<UuidEntry> returnSet = new HashSet<>();

        final ConfigurationSection config = file.getConfig();

        for (String rawUuid : config.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(rawUuid);
            } catch (Exception ex) {
                LogHelper.warning(manager, "Skipping invalid UUID '" + rawUuid + "'");
                continue;
            }

            UuidEntry entry = new UuidEntry(uuid);

            entry.currentName = config.getString(rawUuid + ".current");

            ConfigurationSection nameSec = config.getConfigurationSection(rawUuid + ".names");
            for (String name : nameSec.getKeys(false)) {
                entry.names.put(name, nameSec.getLong(name));
            }

            returnSet.add(entry);
        }

        return returnSet;
    }

    @Override
    UuidEntry loadSingle(UUID uuid) {
        // Not implemented. Plugin does not support shared UUID file between instances.
        return null;
    }

    @Override
    void queueUpdate(UuidEntry entry) {
        updateEntry(file.getConfig(), entry);
    }

    @Override
    void save(boolean async, Set<UuidEntry> entries) {
        final ConfigurationSection config = file.getConfig();

        for (UuidEntry entry : entries) {
            updateEntry(config, entry);
        }

        new TaskChain().add(new VariableGenericTask(async) {
            @Override
            protected void run() {
                file.save();
            }
        }).execute();
    }

    private void updateEntry(ConfigurationSection config, UuidEntry entry) {
        config.set(entry.uuid.toString() + ".current", entry.currentName);

        final ConfigurationSection section = config.createSection(entry.uuid.toString() + ".names");

        for (Entry<String, Long> nameEntry : entry.names.entrySet()) {
            section.set(nameEntry.getKey(), nameEntry.getValue());
        }
    }

}