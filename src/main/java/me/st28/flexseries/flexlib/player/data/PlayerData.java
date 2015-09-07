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
package me.st28.flexseries.flexlib.player.data;

import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.storage.flatfile.YamlFileManager;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerData {

    private final UUID uuid;

    private final YamlFileManager file;

    private PlayerLoader loader;

    /**
     * These are saved/loaded automatically. Should be configuration serializable.
     */
    private final Map<String, Object> customData = new HashMap<>();

    public PlayerData(UUID uuid, YamlFileManager file) {
        this.uuid = uuid;
        this.file = file;
    }

    public UUID getUuid() {
        return uuid;
    }

    public PlayerLoader getLoader() {
        return loader;
    }

    public void setLoader(PlayerLoader loader) {
        if (this.loader != null) {
            throw new IllegalStateException("Loader is already set.");
        }
        this.loader = loader;
    }

    public void load() {
        ConfigurationSection customSec = file.getConfig().getConfigurationSection("custom");
        if (customSec != null) {
            loadCustomData(customSec, null);
        }
    }

    private void loadCustomData(ConfigurationSection currentSec, String currentKey) {
        for (String key : currentSec.getKeys(false)) {
            if (currentSec.get(key) instanceof ConfigurationSection) {
                if (currentKey == null) {
                    loadCustomData(currentSec.getConfigurationSection(key), key);
                } else {
                    loadCustomData(currentSec.getConfigurationSection(key), currentKey + "." + key);
                }
            } else {
                if (currentKey == null) {
                    customData.put(key, currentSec.get(key));
                } else {
                    customData.put(currentKey + "." + key, currentSec.get(key));
                }
            }
        }
    }

    public YamlFileManager getFile() {
        return file;
    }

    public FileConfiguration getConfig() {
        return file.getConfig();
    }

    public ConfigurationSection getConfigSection(Class<? extends PlayerDataProvider> provider) {
        ConfigurationSection section = file.getConfig().getConfigurationSection(provider.getCanonicalName());

        if (section == null) {
            section = file.getConfig().createSection(provider.getCanonicalName());
        }

        return section;
    }

    public <T> T getCustomData(String key, Class<T> type) {
        return getCustomData(key, type, null);
    }

    public <T> T getCustomData(String key, Class<T> type, T defaultValue) {
        Validate.notNull(key, "Key cannot be null.");
        Validate.notNull(type, "Type cannot be null.");

        T returnValue = (T) customData.get(key);
        return returnValue != null ? returnValue : defaultValue;
    }

    public <T> T getCustomData(Class<? extends FlexPlugin> plugin, String key, Class<T> type) {
        return getCustomData(plugin, key, type, null);
    }

    public <T> T getCustomData(Class<? extends FlexPlugin> plugin, String key, Class<T> type, T defaultValue) {
        Validate.notNull(plugin, "Plugin cannot be null.");
        Validate.notNull(key, "Key cannot be null.");
        Validate.notNull(type, "Type cannot be null.");

        T returnValue = (T) customData.get(plugin.getCanonicalName() + "-" + key);
        return returnValue != null ? returnValue : defaultValue;
    }

    public boolean containsCustomData(String key) {
        Validate.notNull(key, "Key cannot be null.");
        return customData.containsKey(key);
    }

    public void setCustomData(String key, Object data) {
        Validate.notNull(key, "Key cannot be null.");

        if (data == null) {
            customData.remove(key);
        } else {
            customData.put(key, data);
        }
    }

    public void setCustomData(Class<? extends FlexPlugin> plugin, String key, Object data) {
        Validate.notNull(plugin, "Plugin cannot be null.");
        Validate.notNull(key, "Key cannot be null.");

        String fullKey = plugin.getCanonicalName() + "-" + key;
        if (data == null) {
            customData.remove(fullKey);
        } else {
            customData.put(fullKey, data);
        }
    }

}