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
package me.st28.flexseries.flexlib.plugin.module;

import me.st28.flexseries.flexlib.log.LogHelper;
import me.st28.flexseries.flexlib.player.data.DataProviderDescriptor;
import me.st28.flexseries.flexlib.player.PlayerManager;
import me.st28.flexseries.flexlib.player.data.PlayerDataProvider;
import me.st28.flexseries.flexlib.plugin.FlexPlugin;
import me.st28.flexseries.flexlib.storage.flatfile.YamlFileManager;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.InputStreamReader;

/**
 * Handles a feature of a {@link FlexPlugin}.
 *
 * @param <T> The {@link FlexPlugin} that owns this module.
 */
public abstract class FlexModule<T extends FlexPlugin> {

    private ModuleStatus status = ModuleStatus.PENDING;

    protected final T plugin;
    protected final String name;
    protected final String description;
    protected final ModuleDescriptor descriptor;

    private File dataFolder;

    private YamlFileManager configFile;

    public FlexModule(T plugin, String name, String description, ModuleDescriptor descriptor) {
        Validate.notNull(plugin, "Plugin cannot be null.");
        Validate.notNull(name, "Name cannot be null.");
        Validate.notNull(descriptor, "Descriptor cannot be null.");

        this.plugin = plugin;
        this.name = name;

        if (description == null) {
            this.description = "(no description set)";
        } else {
            this.description = description;
        }

        this.descriptor = descriptor;

        if (this instanceof PlayerDataProvider) {
            descriptor.addHardDependency(new ModuleReference("FlexLib", "players"));
        }

        descriptor.lock();
    }

    /**
     * @return the {@link ModuleStatus} of this module.
     */
    public final ModuleStatus getStatus() {
        return status;
    }

    /**
     * <b>Internal code</b> - should never be called outside of FlexLib.
     */
    public final void setStatus(ModuleStatus status) {
        this.status = status;
    }

    /**
     * @return the {@link FlexPlugin} that owns this module.
     */
    public final T getPlugin() {
        return plugin;
    }

    /**
     * @return the name of this module.
     */
    public final String getName() {
        return name;
    }

    /**
     * @return the description of this module.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * @return the {@link ModuleDescriptor} for this module.
     */
    public final ModuleDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * @return the data folder for this module. Will be created if it doesn't already exist.
     */
    public final File getDataFolder() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        return dataFolder;
    }

    /**
     * @return the configuration file for this module.
     */
    public final FileConfiguration getConfig() {
        if (configFile == null) {
            throw new UnsupportedOperationException("This module does not have a configuration file.");
        }
        return configFile.getConfig();
    }

    /**
     * Reloads the configuration file for this module.
     */
    public final void reloadConfig() {
        if (configFile != null) {
            try {
                configFile.reload();
                FileConfiguration config = configFile.getConfig();

                config.addDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("modules/" + name + "/config.yml"))));
                config.options().copyDefaults(true);
                configFile.save();
                configFile.reload();
            } catch (Exception ex) {
                LogHelper.severe(this, "An exception occurred while reloading the configuration file.", ex);
            }
        }
    }

    /**
     * Saves the module's configuration file.
     */
    public final void saveConfig() {
        if (configFile != null) {
            configFile.save();
        }
    }

    /**
     * Registers this module as a {@link PlayerDataProvider} with the {@link PlayerManager}.
     *
     * @return True if successfully registered.<br />
     *         False if already registered.
     */
    protected final boolean registerPlayerDataProvider(DataProviderDescriptor descriptor) {
        Validate.isTrue(this instanceof PlayerDataProvider, "This module must implement PlayerDataProvider.");
        Validate.notNull(descriptor, "Descriptor cannot be null.");

        return FlexPlugin.getGlobalModule(PlayerManager.class).registerDataProvider((PlayerDataProvider) this, descriptor);
    }

    public final void onEnable() {
        status = ModuleStatus.LOADING;

        dataFolder = new File(plugin.getDataFolder() + File.separator + name);

        if (plugin.getResource("modules/" + name + "/config.yml") != null) {
            configFile = new YamlFileManager(plugin.getDataFolder() + File.separator + "config-" + name + ".yml");
        } else {
            configFile = null;
        }

        reloadConfig();

        try {
            handleEnable();
        } catch (Exception ex) {
            status = ModuleStatus.DISABLED_ERROR;
            throw new RuntimeException("An exception occurred while enabling module '" + name + "'", ex);
        }

        try {
            handleReload();
        } catch (Exception ex) {
            status = ModuleStatus.ENABLED_ERROR;
            throw new RuntimeException("An exception occurred while reloading module '" + name + "'", ex);
        }

        if (this instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) this, plugin);
        }

        status = ModuleStatus.ENABLED;
    }

    public final void onReload() {
        status = ModuleStatus.RELOADING;

        reloadConfig();

        try {
            handleReload();
        } catch (Exception ex) {
            status = ModuleStatus.ENABLED_ERROR;
            throw new RuntimeException("An exception occurred while reloading module '" + name + "'", ex);
        }

        status = ModuleStatus.ENABLED;
    }

    public final void onSave(boolean async, boolean finalSave) {
        try {
            if (finalSave) {
                handleFinalSave();
            } else {
                handleSave(async);
            }
            saveConfig();
        } catch (Exception ex) {
            LogHelper.severe(this, "An exception occurred while saving module '" + name + "'", ex);
        }
    }

    public final void onDisable() {
        status = ModuleStatus.UNLOADING;

        try {
            handleFinalSave();
            saveConfig();
        } catch (Exception ex) {
            LogHelper.severe(this, "An exception occurred while saving module '" + name + "'", ex);
        }

        try {
            handleDisable();
        } catch (Exception ex) {
            status = ModuleStatus.DISABLED_ERROR;
            throw new RuntimeException("An exception occurred while disabling module '" + name + "'", ex);
        }

        status = ModuleStatus.DISABLED;
    }

    /**
     * Handles custom enable tasks. This will only be called once.
     */
    protected void handleEnable() {}

    /**
     * Handles custom reload tasks.
     */
    protected void handleReload() {}

    /**
     * Handles custom module save tasks.
     *
     * @param async If true, should save asynchronously (where applicable).
     */
    protected void handleSave(boolean async) {}

    /**
     * Handles custom module save tasks. This particular method, unlike {@link #handleSave(boolean)},
     * is only called when the plugin is disabling.
     */
    protected void handleFinalSave() {
        handleSave(false);
    }

    /**
     * Handles custom module disable tasks. This will only be called once.<br />
     * {@link #handleSave(boolean)} will be called automatically, so this should not call it again.
     */
    protected void handleDisable() {}

}