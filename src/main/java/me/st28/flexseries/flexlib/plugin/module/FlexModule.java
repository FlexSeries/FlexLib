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
package me.st28.flexseries.flexlib.plugin.module;

import me.st28.flexseries.flexlib.log.LogHelper;
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

    public final void onSave(boolean async) {
        try {
            handleSave(async);
        } catch (Exception ex) {
            LogHelper.severe(this, "An exception occurred while saving module '" + name + "'", ex);
        }

        saveConfig();
    }

    public final void onDisable() {
        status = ModuleStatus.UNLOADING;

        onSave(false);

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
     * Handles custom module disable tasks. This will only be called once.<br />
     * {@link #handleSave(boolean)} will be called automatically, so this should not call it again.
     */
    protected void handleDisable() {}

}