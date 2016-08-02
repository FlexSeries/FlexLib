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

import me.st28.flexseries.flexlib.FlexLib;
import me.st28.flexseries.flexlib.command.FlexCommandMap;
import me.st28.flexseries.flexlib.event.plugin.PluginReloadedEvent;
import me.st28.flexseries.flexlib.logging.LogHelper;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class FlexPlugin extends JavaPlugin {

    /**
     * Convenience method for retrieving modules from FlexLib.
     *
     * @return The specified global module.
     *         Null if the module is not registered under FlexLib.
     */
    public static <T extends FlexModule> T getGlobalModule(Class<T> module) {
        return FlexPlugin.getPluginModule(FlexLib.class, module);
    }

    /**
     * @return The specified module from the specified FlexPlugin implementation.
     *         Null if the module is not registered under the specified plugin.
     */
    public static <T extends FlexModule> T getPluginModule(Class<? extends FlexPlugin> plugin, Class<T> module) {
        return JavaPlugin.getPlugin(plugin).getModule(module);
    }

    // ------------------------------------------------------------------------------------------ //

    private PluginStatus status = PluginStatus.PENDING;

    private boolean hasConfig = false;
    private boolean isDebugEnabled = false;

    private final FlexCommandMap commandMap = new FlexCommandMap(this);

    private final Map<Class<? extends FlexModule>, FlexModule> modules = new LinkedHashMap<>();

    @Override
    public final void onLoad() {
        status = PluginStatus.LOADING;

        handleLoad();
    }

    @Override
    public final void onEnable() {
        final long startTime = System.currentTimeMillis();

        status = PluginStatus.ENABLING;

        // Register self as listener where applicable
        if (this instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) this, this);
        }

        // Determine if there is a configuration file or not, and save it if there is
        if (getResource("config.yml") != null) {
            saveDefaultConfig();
            getConfig().options().copyDefaults(false);
            saveConfig();

            hasConfig = true;
            reloadConfig();
        }

        // Load modules
        for (FlexModule<?> module : modules.values()) {
            module.enable();
        }

        // Handle implementation-specific enable tasks
        try {
            handleEnable();
        } catch (Exception ex) {
            status = PluginStatus.ENABLED_ERROR;
            LogHelper.severe(this,
                    "An exception occurred while enabling, functionality may be reduced.", ex);
        }

        status = PluginStatus.ENABLED;
        LogHelper.info(this, String.format("%s v%s by %s ENABLED (%dms)", getName(),
                getDescription().getVersion(), getDescription().getAuthors(),
                System.currentTimeMillis() - startTime));
    }

    @Override
    public final void reloadConfig() {
        super.reloadConfig();

        if (!hasConfig) {
            return;
        }

        handleConfigReload(getConfig());
    }

    public final void reloadAll() {
        try {
            status = PluginStatus.RELOADING;

            reloadConfig();

            for (FlexModule module : modules.values()) {
                if (module.getStatus().isEnabled()) {
                    module.reload();
                }
            }

            handleReload(false);

            status = PluginStatus.ENABLED;
            getServer().getPluginManager().callEvent(new PluginReloadedEvent(this));
        } catch (Exception ex) {
            status = PluginStatus.ENABLED_ERROR;
            throw new RuntimeException("An exception occurred while reloading", ex);
        }
    }

    public final void saveAll(boolean async) {
        saveAll(async, false);
    }

    public final void saveAll(boolean async, boolean isFinalSave) {
        try {
            for (FlexModule module : modules.values()) {
                if (module.getStatus().isEnabled()) {
                    module.save(async, isFinalSave);
                }
            }

            handleSave(async, isFinalSave);

            if (hasConfig) {
                saveConfig();
            }
        } catch (Exception ex) {
            throw new RuntimeException("An exception occurred while saving", ex);
        }
    }

    @Override
    public final void onDisable() {
        status = PluginStatus.DISABLING;

        try {
            saveAll(false, true);
            handleDisable();
        } catch (Exception ex) {
            throw new RuntimeException("An exception occured while disabling", ex);
        }
    }

    public final PluginStatus getStatus() {
        return status;
    }

    /**
     * @return True if the plugin is in debug mode.
     */
    public final boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    /**
     * Enables or disables debug mode.
     */
    public final void setDebugEnabled(boolean state) {
        isDebugEnabled = state;
    }

    /**
     * Registers a module. Modules can only be registered while the plugin is loading.
     *
     * @param module The module to register.
     *               Must not be null.
     * @return True if the module was successfully registered.
     *         False if the module is already registered.
     * @throws IllegalStateException Thrown if the plugin is not in its loading stage.
     */
    protected final boolean registerModule(FlexModule module) {
        Validate.notNull(module, "Module cannot be null");

        if (status != PluginStatus.LOADING) {
            throw new IllegalStateException("No longer accepting module registrations");
        }

        final Class<? extends FlexModule> clazz = module.getClass();
        if (modules.containsKey(clazz)) {
            return false;
        }
        modules.put(clazz, module);
        LogHelper.info(this, "Registered module '" + module.getName() +
                "' (" + module.getClass().getCanonicalName() + ")");
        return true;
    }

    protected final FlexCommandMap getCommandMap() {
        return commandMap;
    }

    public final <T extends FlexModule> T getModule(Class<T> clazz) {
        Validate.notNull(clazz, "Class cannot be null");
        return (T) modules.get(clazz);
    }

    // ------------------------------------------------------------------------------------------ //
    // Implementation Methods Below                                                               //
    // ------------------------------------------------------------------------------------------ //

    protected void handleLoad() { }

    protected void handleEnable() { }

    protected void handleConfigReload(FileConfiguration config) { }

    protected void handleReload(boolean isFirstReload) { }

    protected void handleSave(boolean async, boolean isFinalSave) { }

    protected void handleDisable() { }

}