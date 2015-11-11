package me.st28.flexseries.flexlib.player.uuidtracker;

import me.st28.flexseries.flexlib.log.LogHelper;
import me.st28.flexseries.flexlib.storage.flatfile.YamlFileManager;
import me.st28.flexseries.flexlib.utils.ArgumentCallback;
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
        file = new YamlFileManager(manager.getPlugin().getDataFolder() + File.separator + "playerUuids.yml");
    }

    @Override
    void loadAll(ArgumentCallback<Set<UuidEntry>> callback) {
        new TaskChain().add(new AsyncGenericTask() {
            @Override
            protected void run() {
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

                callback.call(returnSet);
            }
        }).execute();
    }

    @Override
    void loadSingle(UUID uuid, ArgumentCallback<UuidEntry> callback) {
        // Not implemented. Plugin does not support shared UUID file between instances.
    }

    @Override
    void save(boolean async, Set<UuidEntry> entries) {
        new TaskChain().add(new VariableGenericTask(async) {
            @Override
            protected void run() {
                final ConfigurationSection config = file.getConfig();

                for (UuidEntry entry : entries) {
                    config.set(entry.uuid.toString() + ".current", entry.currentName);

                    final ConfigurationSection section = config.createSection(entry.uuid.toString() + ".names");

                    for (Entry<String, Long> nameEntry : entry.names.entrySet()) {
                        section.set(nameEntry.getKey(), nameEntry.getValue());
                    }
                }

                file.save();
            }
        }).execute();
    }

}