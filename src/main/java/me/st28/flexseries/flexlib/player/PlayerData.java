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
package me.st28.flexseries.flexlib.player;

import me.st28.flexseries.flexlib.player.data.PlayerDataProvider;
import me.st28.flexseries.flexlib.player.data.PlayerLoader;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.storage.flatfile.YamlFileManager;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.net.Inet4Address;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

public final class PlayerData {

    private boolean isLoaded = false;

    long lastAccessed;

    private final UUID uuid;

    private final YamlFileManager file;

    private PlayerLoader loader;

    Long firstJoin = null;
    Long lastLogin = null;
    Long lastLogout = null;

    String lastIp;
    final Set<String> ips = new HashSet<>();

    /**
     * These are saved/loaded automatically. Should be configuration serializable.
     */
    private final Map<String, Object> customData = new HashMap<>();

    /**
     * Keys of objects in {@link #customData} that should not be saved.
     */
    private final Set<String> transientData = new HashSet<>();

    public PlayerData(UUID uuid, YamlFileManager file) {
        this.uuid = uuid;
        this.file = file;
        updateLastAccessed();
    }

    void updateLastAccessed() {
        lastAccessed = System.currentTimeMillis();
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
        if (isLoaded) {
            return;
        }

        isLoaded = true;

        ConfigurationSection config = getConfig();

        if (config.isSet("firstJoin")) {
            firstJoin = config.getLong("firstJoin");
        }

        if (config.isSet("lastlogin")) {
            lastLogin = config.getLong("lastlogin");
        }

        if (config.isSet("lastLogout")) {
            lastLogout = config.getLong("lastLogout");
        }

        lastIp = config.getString("ip.last");
        ips.addAll(config.getStringList("ip.previous"));

        // Load custom data
        ConfigurationSection customSec = config.getConfigurationSection("custom");
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

    public void save() {
        ConfigurationSection config = file.getConfig();

        config.set("firstJoin", firstJoin);
        config.set("lastLogin", lastLogin);
        config.set("lastLogout", lastLogout);
        config.set("ip.last", lastIp);
        config.set("ip.previous", new ArrayList<>(ips));

        // Save custom data
        ConfigurationSection customSec = config.getConfigurationSection("custom");
        if (customSec == null) {
            customSec = config.createSection("custom");
        }

        for (Entry<String, Object> entry : customData.entrySet()) {
            if (!transientData.contains(entry.getKey())) {
                customSec.set(entry.getKey(), entry.getValue());
            }
        }

        file.save();
    }

    public YamlFileManager getFile() {
        return file;
    }

    public FileConfiguration getConfig() {
        return file.getConfig();
    }

    public Timestamp getFirstJoin() {
        updateLastAccessed();
        return firstJoin == null ? null : new Timestamp(firstJoin);
    }

    public void setFirstJoin(Timestamp firstJoin) {
        updateLastAccessed();
        Validate.notNull(this.firstJoin, "First join is already set.");
        this.firstJoin = firstJoin.getTime();
    }

    public Timestamp getLastLogin() {
        updateLastAccessed();
        return lastLogin == null ? null : new Timestamp(lastLogin);
    }

    public void setLastLogin(Timestamp lastLogin) {
        updateLastAccessed();
        this.lastLogin = lastLogin.getTime();
    }

    public Timestamp getLastLogout() {
        updateLastAccessed();
        return lastLogout == null ? null : new Timestamp(lastLogout);
    }

    public void setLastLogout(Timestamp lastLogout) {
        updateLastAccessed();
        this.lastLogout = lastLogout.getTime();
    }

    public String getLastIp() {
        updateLastAccessed();
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        updateLastAccessed();
        this.lastIp = lastIp;
        if (!ips.contains(lastIp)) {
            ips.add(lastIp);
        }
    }

    public Set<String> getIps() {
        updateLastAccessed();
        return Collections.unmodifiableSet(ips);
    }

    public ConfigurationSection getDirectSection(Class<? extends FlexPlugin> plugin) {
        updateLastAccessed();

        ConfigurationSection section = file.getConfig().getConfigurationSection("directCustom." + plugin.getCanonicalName());

        if (section == null) {
            section = file.getConfig().createSection("directCustom." + plugin.getCanonicalName());
        }

        return section;
    }

    public <T> T getCustomData(String key, Class<T> type) {
        updateLastAccessed();
        return getCustomData(key, type, null);
    }

    public <T> T getCustomData(String key, Class<T> type, T defaultValue) {
        updateLastAccessed();
        Validate.notNull(key, "Key cannot be null.");
        Validate.notNull(type, "Type cannot be null.");

        T returnValue = (T) customData.get(key);
        return returnValue != null ? returnValue : defaultValue;
    }

    public <T> T getCustomData(Class<? extends FlexPlugin> plugin, String key, Class<T> type) {
        updateLastAccessed();
        return getCustomData(plugin, key, type, null);
    }

    public <T> T getCustomData(Class<? extends FlexPlugin> plugin, String key, Class<T> type, T defaultValue) {
        updateLastAccessed();
        Validate.notNull(plugin, "Plugin cannot be null.");
        Validate.notNull(key, "Key cannot be null.");
        Validate.notNull(type, "Type cannot be null.");

        T returnValue = (T) customData.get(plugin.getCanonicalName() + "-" + key);
        return returnValue != null ? returnValue : defaultValue;
    }

    public boolean containsCustomData(String key) {
        updateLastAccessed();
        Validate.notNull(key, "Key cannot be null.");
        return customData.containsKey(key);
    }

    public void setCustomData(String key, Object data) {
        updateLastAccessed();
        Validate.notNull(key, "Key cannot be null.");

        if (data == null) {
            customData.remove(key);
        } else {
            customData.put(key, data);
        }
    }

    public void setCustomData(String key, Object data, boolean isTransient) {
        updateLastAccessed();
        setCustomData(key, data);

        if (isTransient) {
            setTransientData(key);
        }
    }

    public void setCustomData(Class<? extends FlexPlugin> plugin, String key, Object data) {
        updateLastAccessed();
        Validate.notNull(plugin, "Plugin cannot be null.");
        Validate.notNull(key, "Key cannot be null.");

        String fullKey = plugin.getCanonicalName() + "-" + key;
        if (data == null) {
            customData.remove(fullKey);
        } else {
            customData.put(fullKey, data);
        }
    }

    public void setCustomData(Class<? extends FlexPlugin> plugin, String key, Object data, boolean isTransient) {
        updateLastAccessed();
        setCustomData(plugin, key, data);

        if (isTransient) {
            setTransientData(key);
        }
    }

    public void setTransientData(String key) {
        updateLastAccessed();
        Validate.notNull(key, "Key cannot be null.");
        transientData.add(key);
    }

}