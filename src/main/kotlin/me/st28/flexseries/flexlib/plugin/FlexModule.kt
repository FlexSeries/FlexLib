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
package me.st28.flexseries.flexlib.plugin

import me.st28.flexseries.flexlib.logging.LogHelper
import me.st28.flexseries.flexlib.plugin.storage.flatfile.YamlFileManager
import org.apache.commons.lang.Validate
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.regex.Pattern

/**
 * Represents a module of a FlexPlugin.
 *
 * Module lifestyle:
 * 1) Module is registered under a FlexPlugin
 * 2) Assuming the module is disabled due to the configuration, the module is enabled.
 * 3) handleEnable is called, followed by handleReload
 *     - Each module is reloaded immediately after being enabled.
 *
 * Other notes:
 * - If an implementation implements Bukkit's Listener interface, the module will automatically be
 *   registered as a Listener.
 * - Modules will not enable if an exception is encountered during handleEnable, but they will still
 *   enable (with [hasError] = true) if an exception is encountered during handleReload.
 * - Modules will also fail to enable if the configuration has invalid YAML syntax.
 * - Modules (and any related commands, etc.) should be able to function at least partially if a
 *   reload fails, and they should be able to recover if valid configuration is given on next reload.
 * - FlexPlugins can enable or disable plugins while the server is running. Because of this, implementations
 *   should take care to **cleanup fully** on disable to help prevent memory leaks.
 */
abstract class FlexModule<out T : FlexPlugin>(
        val plugin: T,
        val name: String,
        val description: String)
{

    companion object {

        val NAME_PATTERN: Pattern = Pattern.compile("[a-zA-Z0-9-_]+")

    }

    /**
     * The status of the module.
     */
    var status: ModuleStatus = ModuleStatus.DISABLED
        internal set

    /**
     * True if the module encountered errors while enabling, reloading, or disabling.
     */
    var hasError: Boolean = false
        internal set

    /**
     * The data folder for the module.
     * Located at plugins/(Plugin name)/(Module name)
     */
    var dataFolder: File
        get() {
            if (!field.exists()) {
                field.mkdirs()
            }
            return field
        }
        private set

    /**
     * The configuration file for the module.
     */
    private var hasConfig: Boolean = false
    private val configFile: YamlFileManager? = null
        get() {
            if (field == null) {
                field = YamlFileManager(plugin.dataFolder.path + File.separator + "config-$name.yml")
                hasConfig = true
            }
            return field
        }

    /**
     * The primary configuration section for this module.
     */
    protected val config: FileConfiguration
        get() = configFile!!.config

    init {
        Validate.isTrue(NAME_PATTERN.matcher(name).matches(), "Name must be alphanumeric with only dashes and underscores")

        dataFolder = File(plugin.dataFolder.path + File.separator + name)
    }

    /**
     * Enables the module.
     *
     * @return True if the module was successfully enabled.
     *         False if the module encountered errors while loading.
     *
     * @throws IllegalStateException Thrown if the module is already enabled.
     */
    internal fun enable(): Boolean {
        if (status == ModuleStatus.DISABLED_CONFIG) {
            throw IllegalStateException("Module '$name' is disabled via configuration and cannot be enabled.")
        } else if (status.isEnabled) {
            throw IllegalStateException("Module '$name' is already enabled")
        }

        hasError = false

        val startTime = System.currentTimeMillis()

        // Setup default config
        plugin.getResource("modules/$name/config.yml")?.use {
            InputStreamReader(it).use {
                config.addDefaults(YamlConfiguration.loadConfiguration(it))
                config.options().copyDefaults(false)
                configFile!!.save()
            }
        }

        try {
            // Reload configuration file
            reloadConfig()

            handleEnable()
        } catch (ex: Exception) {
            status = ModuleStatus.DISABLED
            hasError = true
            LogHelper.severe(this, "An exception occurred while enabling module '$name'", ex)
            return false
        }

        status = ModuleStatus.ENABLED

        handleReload()

        // Register as listener where applicable
        if (this is Listener) {
            plugin.server.pluginManager.registerEvents(this, plugin)
            LogHelper.debug(this, "Registered listener")
        }

        LogHelper.info(this, "Module enabled (%dms)".format(System.currentTimeMillis() - startTime))
        return hasError
    }

    internal fun reload() {
        try {
            reloadConfig()
            handleReload()
        } catch (ex: Exception) {
            hasError = true
            LogHelper.severe(this, "An exception occurred while reloading module '$name'", ex)
            return
        }

        hasError = false
        status = ModuleStatus.ENABLED
    }

    /**
     * Reloads the configuration file.
     */
    internal fun reloadConfig() {
        if (hasConfig) {
            // Create data folder if it doesn't exist.
            dataFolder
            configFile!!.save()
        }
    }

    /**
     * Saves the module.
     */
    internal fun save(async: Boolean) {
        try {
            handleSave(async)
        } catch (ex: Exception) {
            hasError = true
            LogHelper.severe(this, "An exception occurred while saving module '$name'", ex)
        }
    }

    /**
     * Disables the module.
     */
    internal fun disable() {
        // Unregister self as listener where applicable
        if (this is Listener) {
            HandlerList.unregisterAll(this)
            LogHelper.debug(this, "Unregistered listener")
        }

        // Final save
        save(false)

        // Disable
        try {
            handleDisable()
        } catch (ex: Exception) {
            status = ModuleStatus.DISABLED
            hasError = true
            LogHelper.severe(this, "An exception occurred while disabling module '$name'", ex)
            return
        }

        status = ModuleStatus.DISABLED
        hasError = false
        LogHelper.debug(this, "Module disabled")
    }


    // ========================================================================================== //
    // API Methods                                                                                //
    // ========================================================================================== //

    /**
     * @return A resource with the given name located under this module's folder in the plugin jar.
     */
    fun getResource(name: String): InputStream {
        return plugin.getResource("modules/${this.name}/$name")
    }


    // ========================================================================================== //
    // Implementation-specific Methods                                                            //
    // ========================================================================================== //

    /**
     * Handles module-specific enable tasks.
     *
     * Should consist of tasks that should only occur once throughout the lifespan of a module, such as:
     * - Initializing a database connection pool
     * - Initializing variables
     */
    protected open fun handleEnable() { }

    /**
     * Handles module-specific reload tasks.
     *
     * Should consist of tasks that are affected by configuration (in general). The configuration is
     * always reloaded immediately prior to this method being called, so new configuration values
     * will be present when this method is executed.
     */
    protected open fun handleReload() { }

    /**
     * Handles module-specific save tasks.
     *
     * In an effort to allow plugins and modules to have autosaving functionality, this method should
     * save asynchronously where possible (assuming [async] is true).
     *
     * @param async True if the module should save data asynchronously (where applicable).
     *              Will be true in many cases, except when the plugin is disabling all modules.
     */
    protected open fun handleSave(async: Boolean) { }

    /**
     * Handles module-specific disable tasks.
     *
     * handleSave is always called prior to this, so this method shouldn't need to handle saving any
     * data.
     *
     * This method may be called when [hasError] = true, so care should be taken to avoid using any
     * uninitialized variables, among other things.
     *
     * Should consist of cleanup tasks that should only occur at the end of a module's lifespan, such as:
     * - Closing database connections
     */
    protected open fun handleDisable() { }

}
