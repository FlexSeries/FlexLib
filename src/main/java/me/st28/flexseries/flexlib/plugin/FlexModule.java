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
package me.st28.flexseries.flexlib.plugin;

import me.st28.flexseries.flexlib.logging.LogHelper;
import me.st28.flexseries.flexlib.storage.flatfile.YamlFileManager;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

/**
 * Represents a manager for a {@link FlexPlugin}.
 *
 * @param <T> The {@link FlexPlugin} that owns this module.
 */
public abstract class FlexModule<T extends FlexPlugin> {

    private final static Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z0-9-_]+");

    private final T plugin;
    private final String name;
    private final String description;
    private ModuleStatus status = ModuleStatus.PENDING;

    private File dataFolder;
    private YamlFileManager configFile;

    protected FlexModule(T plugin, String name, String description) {
        Validate.notNull(plugin, "Plugin cannot be null");
        Validate.isTrue(NAME_PATTERN.matcher(name).matches(), "Name must be alphanumeric with only dashes and underscores");

        this.plugin = plugin;
        this.name = name;

        if (description == null) {
            this.description = "(no description set)";
        } else {
            this.description = description;
        }
    }

    public final T getPlugin() {
        return plugin;
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description;
    }

    public final ModuleStatus getStatus() {
        return status;
    }

    /**
     * @return The data folder for this module. Will be created if it doesn't already exist.
     */
    public final File getDataFolder() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        return dataFolder;
    }

    /**
     * @return The configuration file for this module.
     *         Null if this module doesn't have a configuration file.
     */
    public final FileConfiguration getConfig() {
        return configFile.getConfig();
    }

    /**
     * Reloads the configuration file for this module.
     * Fails silently if this module doesn't have a configuration file.
     */
    public final void reloadConfig() {
        if (configFile != null) {
            configFile.reload();
        }
    }

    public final void saveConfig() {
        if (configFile != null) {
            configFile.save();
        }
    }

    public InputStream getResource(String name) {
        return plugin.getResource("modules/" + this.name + "/" + name);
    }

    /**
     * Enables the module.
     */
    public final void enable() {
        switch (status) {
            case PENDING:
            case DISABLED:
            case DISABLED_DEPENDENCY:
            case DISABLED_ERROR:
                break;

            default:
                throw new IllegalStateException("Module has already been enabled.");
        }

        // TODO: Check for dependencies

        status = ModuleStatus.LOADING;

        final long startTime = System.currentTimeMillis();

        dataFolder = new File(plugin.getDataFolder() + File.separator + name);

        // TODO: Don't save config if it contains no content
        configFile = new YamlFileManager(plugin.getDataFolder() + File.separator + "config-" + name + ".yml");
        if (configFile.isEmpty()) {
            FileConfiguration config = configFile.getConfig();

            InputStream def = plugin.getResource("modules/" + name + "/config.yml");
            if (def != null) {
                config.addDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(def)));
                config.options().copyDefaults(true);
                saveConfig();
            }
        }

        try {
            reloadConfig();
            handleReload(true);
            handleEnable();
            handleReload(false);
        } catch (Exception ex) {
            status = ModuleStatus.DISABLED_ERROR;
            throw new RuntimeException("An exception occurred while enabling module '" + name + "'", ex);
        }

        // Register as listener where applicable
        if (this instanceof Listener) {
            plugin.getServer().getPluginManager().registerEvents((Listener) this, plugin);
            LogHelper.debug(this, "Registered listener");
        }

        LogHelper.info(this, String.format("Module enabled (%dms)", System.currentTimeMillis() - startTime));
    }

    /**
     * Reloads the module.
     */
    public final void reload() {
        status = ModuleStatus.RELOADING;

        reloadConfig();

        try {
            handleReload(false);
        } catch (Exception ex) {
            status = ModuleStatus.ENABLED_ERROR;
            throw new RuntimeException("An exception occurred while reloading module '" + name + "'", ex);
        }

        status = ModuleStatus.ENABLED;
    }

    /**
     * @see #save(boolean, boolean)
     */
    public final void save(boolean async) {
        save(async, false);
    }

    /**
     * Saves the module.
     *
     * @param async True if the module should be saved asynchronously (where applicable).
     * @param isFinalSave True if the module is being saved for the final time prior to being disabled.
     */
    public final void save(boolean async, boolean isFinalSave) {
        handleSave(async, isFinalSave);
    }

    /**
     * Disables the module.
     */
    public final void disable() {
        status = ModuleStatus.UNLOADING;

        // Unregister self as listener where applicable
        if (this instanceof Listener) {
            HandlerList.unregisterAll((Listener) this);
            LogHelper.debug(this, "Unregistered listener");
        }

        try {
            handleSave(false, true);
            saveConfig();
            handleDisable();
        } catch (Exception ex) {
            status = ModuleStatus.DISABLED_ERROR;
            throw new RuntimeException("An exception occurred while disabling module '" + name + "'", ex);
        }

        status = ModuleStatus.DISABLED;
        LogHelper.info(this, "Module disabled");
    }

    /**
     * Should be implemented by module implementations to handle custom enable tasks.
     */
    protected void handleEnable() { }

    /**
     * Should be implemented by module implementations to handle custom reload tasks.
     * @param isFirstReload True if this method is being called for the first time for a module.
     *                      The first reload will be called prior to the plugin being enabled.
     */
    protected void handleReload(boolean isFirstReload) { }

    protected void handleSave(boolean async, boolean isFinalSave) { }

    protected void handleDisable() { }

}