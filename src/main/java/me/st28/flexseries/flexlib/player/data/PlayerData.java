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
     * These are not saved/loaded automatically.
     */
    private final Map<String, Object> globalObjects = new HashMap<>();

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

    public <T> T getGlobalObject(String key, Class<T> type, T defaultValue) {
        T returnValue = (T) globalObjects.get(key);
        return returnValue != null ? returnValue : defaultValue;
    }

    /**
     * @return True if no changes were made.
     */
    public boolean setGlobalObject(String key, Object object) {
        Validate.notNull(key, "Key cannot be null.");

        if (object == null) {
            return globalObjects.remove(key) == null;
        }

        return globalObjects.put(key, object) != null;
    }

}