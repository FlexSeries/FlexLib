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
package me.st28.flexseries.flexlib.plugin;

import me.st28.flexseries.flexlib.log.LogHelper;
import me.st28.flexseries.flexlib.plugin.module.*;
import me.st28.flexseries.flexlib.utils.StringConverter;
import me.st28.flexseries.flexlib.utils.StringUtils;
import me.st28.flexseries.flexlib.utils.TimeUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * A plugin using FlexLib's plugin library.
 *
 * A FlexPlugin's features are separated into {@link FlexModule}s.
 */
public class FlexPlugin extends JavaPlugin {

    /**
     * References to each FlexPlugin's autosaving runnable (where applicable).
     */
    private static final Map<Class<? extends FlexPlugin>, BukkitRunnable> AUTOSAVE_RUNNABLES = new HashMap<>();

    /**
     * An index of all global {@link FlexModule}s as defined in each module's {@link ModuleDescriptor}.
     */
    private static final Map<Class<? extends FlexModule>, FlexModule> GLOBAL_MODULES = new HashMap<>();

    /**
     * @return An unmodifiable collection of all global {@link FlexModule}s.
     */
    public static Collection<FlexModule> getGlobalModules() {
        return Collections.unmodifiableCollection(GLOBAL_MODULES.values());
    }

    /**
     * Retrieves the instance of a global module.
     *
     * @param module The class of the module.
     * @return The module matching the class.
     * @throws IllegalArgumentException Thrown if the provided module class is not registered.
     * @throws ModuleDisabledException Thrown if the module is disabled.
     */
    public static <T extends FlexModule> T getGlobalModule(Class<T> module) {
        Validate.notNull(module, "Module class cannot be null.");

        if (!GLOBAL_MODULES.containsKey(module)) {
            throw new IllegalArgumentException("No global module '" + module.getCanonicalName() + "' is registered.");
        }

        FlexModule moduleInst = GLOBAL_MODULES.get(module);
        ModuleStatus status = moduleInst.getStatus();

        if (!status.isEnabled()) {
            throw new ModuleDisabledException(moduleInst);
        }

        return (T) moduleInst;
    }

    /**
     * Retrieves a FlexPlugin's instance of a given module.
     *
     * @param plugin The class of the plugin.
     * @param module The class of the module.
     * @return The module matching the class and plugin.
     * @throws IllegalArgumentException Thrown if the provided module class is not registered.
     * @throws ModuleDisabledException Thrown if the module is disabled.
     */
    public static <T extends FlexModule> T getPluginModule(Class<? extends FlexPlugin> plugin, Class<T> module) {
        Validate.notNull(plugin, "Plugin class cannot be null.");
        Validate.notNull(module, "Module class cannot be null.");

        FlexPlugin pluginInst = JavaPlugin.getPlugin(plugin);
        if (pluginInst == null) {
            throw new IllegalArgumentException("Plugin '" + plugin.getCanonicalName() + "' not found.");
        }

        FlexModule moduleInst = pluginInst.modules.get(module);
        if (moduleInst == null) {
            throw new IllegalArgumentException("No module '" + module.getCanonicalName() + "' is registered under plugin '" + pluginInst.getName() + "'");
        }

        ModuleStatus status = moduleInst.getStatus();

        if (!status.isEnabled()) {
            throw new ModuleDisabledException(moduleInst);
        }

        return (T) moduleInst;
    }

    // ------------------------------------------------------------------------------------------ //

    private PluginStatus status;

    private boolean hasConfig = false;

    private final Map<Class<? extends FlexModule>, FlexModule> modules = new HashMap<>();

    @Override
    public final void onLoad() {
        status = PluginStatus.LOADING;

        try {
            handleLoad();
        } catch (Exception ex) {
            status = PluginStatus.LOADING_ERROR;
            throw new RuntimeException("An exception occurred while loading.", ex);
        }
    }

    @Override
    public final void onEnable() {
        if (status == PluginStatus.LOADING_ERROR) {
            LogHelper.severe(this, "Encountered errors while loading. To help prevent damage, the plugin will now be disabled.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        status = PluginStatus.ENABLING;

        long loadStartTime = System.currentTimeMillis();

        // Register self as a Listener if implemented.
        if (this instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) this, this);
        }

        // Determine if this plugin has a configuration file or not, and save it if there is one.
        if (getResource("config.yml") != null) {
            saveDefaultConfig();
            getConfig().options().copyDefaults(true);
            saveConfig();

            hasConfig = true;
            reloadConfig();
        }

        // LOAD MODULES //

        // Check for unused modules
        Set<FlexModule> unused = new HashSet<>(modules.values());

        for (FlexModule module : modules.values()) {
            ModuleDescriptor descriptor = module.getDescriptor();

            if (!descriptor.smartLoad()) {
                unused.remove(module);
            }

            Set<ModuleReference> allDependencies = new HashSet<>();
            allDependencies.addAll(descriptor.getSoftDependencies());
            allDependencies.addAll(descriptor.getHardDependencies());

            for (ModuleReference reference : allDependencies) {
                FlexModule refModule = reference.getModule();

                if (refModule != null) {
                    unused.remove(refModule);
                }
            }
        }

        // TODO: Only ignore loading if no plugins require it (not just this plugin).
        if (!unused.isEmpty()) {
            LogHelper.info(this, "Smart load stopped " + unused.size() + " unused module(s) from loading.");
            LogHelper.debug(this, "Unused modules: " + StringUtils.collectionToSortedString(unused, new StringConverter<FlexModule>() {
                @Override
                public String toString(FlexModule object) {
                    return object.getName();
                }
            }));
        }

        Set<FlexModule> checked = new HashSet<>();
        for (FlexModule module : modules.values()) {
            if (unused.contains(module) || checked.contains(module) || module.getStatus() != ModuleStatus.PENDING) {
                continue;
            }

            checked.add(module);
            loadModule(unused, checked, module);
        }

        // LOAD MODULES //

        try {
            handleEnable();
        } catch (Exception ex) {
            status = PluginStatus.ENABLING_ERROR;
            throw new RuntimeException("An exception occurred while enabling.", ex);
        }

        status = PluginStatus.ENABLED;
        LogHelper.info(this, String.format("%s v%s by %s ENABLED (%dms)", getName(), getDescription().getVersion(), getDescription().getAuthors(), System.currentTimeMillis() - loadStartTime));
    }

    private void loadModule(Set<FlexModule> unused, Set<FlexModule> checked, FlexModule current) {
        long moduleStartTime = System.currentTimeMillis();
        LogHelper.info(this, "Loading module '" + current.getName() + "'");

        Set<ModuleReference> requiredDeps = current.getDescriptor().getHardDependencies();

        for (ModuleReference reference : current.getDescriptor().getAllDependencies()) {
            FlexModule module = reference.getModule();
            if (module == null) {
                continue;
            }

            if (unused.contains(module)) {
                continue;
            }

            if (checked.contains(module)) {
                continue;
            }

            checked.add(module);

            if (module.getPlugin() != this) {
                // Don't attempt to load a module that isn't from this plugin.
                continue;
            }

            try {
                loadModule(unused, checked, module);
            } catch (Exception ex) {
                LogHelper.warning(this, "Encountered an exception while trying to load module dependency '" + current.getName() + "'", ex);
            }
        }

        for (ModuleReference requiredDep : requiredDeps) {
            if (requiredDep.getModule() == null) {
                LogHelper.warning(this, "Unable to load module '" + current.getName() + "': dependency '" + requiredDep.toString() + "' not found.");
                current.setStatus(ModuleStatus.DISABLED_DEPENDENCY);
                return;
            } else if (!requiredDep.getModule().getStatus().isEnabled()) {
                LogHelper.warning(this, "Unable to load module '" + current.getName() + "': dependency '" + requiredDep.toString() + "' not enabled.");
                current.setStatus(ModuleStatus.DISABLED_DEPENDENCY);
                return;
            }
        }

        current.onEnable();
        LogHelper.info(this, "Successfully loaded module '" + current.getName() + "' (" + (System.currentTimeMillis() - moduleStartTime) + "ms)");
    }

    @Override
    public final void reloadConfig() {
        super.reloadConfig();

        if (hasConfig) {
            int autosaveInterval = getConfig().getInt("autosave interval", 0);

            if (autosaveInterval == 0) {
                LogHelper.warning(this, "Autosaving disabled. It is recommended to enable it to help prevent data loss!");
            } else {
                if (AUTOSAVE_RUNNABLES.containsKey(getClass())) {
                    AUTOSAVE_RUNNABLES.remove(getClass()).cancel();
                }

                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        saveAll(true);
                    }
                };

                runnable.runTaskTimer(this, autosaveInterval * 1200L, autosaveInterval * 1200L);
                AUTOSAVE_RUNNABLES.put(getClass(), runnable);
                LogHelper.info(this, "Autosaving enabled. Saving every " + TimeUtils.translateSeconds(autosaveInterval * 60) + ".");
            }

            handleConfigReload(getConfig());
        }
    }

    /**
     * Reloads:
     * <ul>
     *     <li>Plugin configuration file</li>
     *     <li>Registered {@link FlexModule}s</li>
     *     <li>Custom reload tasks ({@link #handleReload()})</li>
     * </ul>
     */
    public final void reloadAll() {
        reloadConfig();

        for (FlexModule module : modules.values()) {
            if (module.getStatus().isEnabled()) {
                try {
                    module.onReload();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        try {
            handleReload();
        } catch (Exception ex) {
            status = PluginStatus.ENABLED_ERROR;
            LogHelper.severe(this, "An exception occurred while reloading.", ex);
        }

        Bukkit.getPluginManager().callEvent(new PluginReloadedEvent(this.getClass()));
    }

    public final void saveAll(boolean async) {
        for (FlexModule module : modules.values()) {
            if (module.getStatus().isEnabled()) {
                try {
                    module.onSave(async);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        try {
            handleSave(async);
        } catch (Exception ex) {
            LogHelper.severe(this, "An exception occurred while saving.", ex);
        }

        if (hasConfig) {
            saveConfig();
        }
    }

    @Override
    public final void onDisable() {
        status = PluginStatus.DISABLING;

        saveAll(false);

        try {
            handleDisable();
        } catch (Exception ex) {
            LogHelper.severe(this, "An exception occurred while disabling.", ex);
        }
    }

    public final void registerModule(FlexModule module) {
        Validate.notNull(module, "Module cannot be null.");

        Class<? extends FlexModule> clazz = module.getClass();

        if (module.getDescriptor().isGlobal()) {
            if (GLOBAL_MODULES.containsKey(clazz)) {
                throw new ModuleAlreadyRegisteredException(module);
            }

            GLOBAL_MODULES.put(clazz, module);
        }

        if (status != PluginStatus.LOADING) {
            throw new IllegalStateException("Currently not accepting new module registrations.");
        }

        if (modules.containsKey(clazz)) {
            throw new ModuleAlreadyRegisteredException(module);
        }

        modules.put(clazz, module);
    }

    public final Collection<FlexModule> getModules() {
        return Collections.unmodifiableCollection(modules.values());
    }

    // ------------------------------------------------------------------------------------------ //

    public void handleLoad() {}

    public void handleEnable() {}

    public void handleReload() {}

    public void handleConfigReload(FileConfiguration config) {}

    public void handleSave(boolean async) {}

    public void handleDisable() {}

}