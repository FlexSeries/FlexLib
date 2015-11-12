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