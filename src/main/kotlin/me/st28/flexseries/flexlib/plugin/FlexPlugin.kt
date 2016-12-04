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

import me.st28.flexseries.flexlib.FlexLib
import me.st28.flexseries.flexlib.command.FlexCommandMap
import me.st28.flexseries.flexlib.event.plugin.PluginReloadedEvent
import me.st28.flexseries.flexlib.logging.LogHelper
import me.st28.flexseries.flexlib.message.MessageModule
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.util.*
import kotlin.reflect.KClass

/**
 * Represents a plugin using FlexLib's plugin and module framework.
 *
 * Notes:
 * - handleLoad should be used to register modules.
 * - handleEnable should be used to register commands.
 * - In most cases, handleDisable, handleConfigReload, and handleSave shouldn't need to be called due
 *   to most functionality being implemented in modules rather than the main plugin file.
 */
abstract class FlexPlugin : JavaPlugin() {

    companion object {

        /**
         * Retrieves a global module.
         * This method performs a null check.
         *
         * @throws NullPointerException Thrown if the module is not registered under FlexLib.
         */
        fun <T : FlexModule<*>> getGlobalModule(module: KClass<T>): T {
            return JavaPlugin.getPlugin(FlexLib::class.java)!!.getModule(module)!!
        }

        /**
         * Retrieves a global module.
         *
         * @return The found module.
         *         Null if the module is not registered under FlexLib.
         */
        fun <T : FlexModule<*>> getGlobalModuleSafe(module: KClass<T>): T? {
            return JavaPlugin.getPlugin(FlexLib::class.java)!!.getModule(module)
        }

        /**
         * Retrieves a plugin module.
         * This method performs a null check.
         *
         * @throws NullPointerException Thrown if the module is not registered under the plugin.
         */
        fun <T : FlexModule<*>> getPluginModule(plugin: KClass<out FlexPlugin>, module: KClass<in T>) : T {
            return JavaPlugin.getPlugin(plugin.java)!!.getModule(module)!!
        }

        /**
         * Retrieves a plugin module.
         *
         * @return The found module.
         *         Null if the module is not registered under the plugin.
         */
        fun <T : FlexModule<*>> getPluginModuleSafe(plugin: KClass<out FlexPlugin>, module: KClass<T>): T? {
            return JavaPlugin.getPlugin(plugin.java)!!.getModule(module)
        }

    }

    private var isAcceptingModuleRegistrations: Boolean = true

    var hasConfig = false
        private set

    var isDebugEnabled = false

    lateinit var commandMap: FlexCommandMap
        private set

    internal val modules: MutableMap<KClass<in FlexModule<*>>, FlexModule<*>> = LinkedHashMap()

    private var autosaveRunnable: BukkitRunnable? = null

    final override fun onLoad() {
        commandMap = FlexCommandMap(this)

        // Register a message module for the plugin if a messages.yml file was found in the jar.
        if (getResource("messages.yml") != null) {
            registerModule(MessageModule(this))
        }

        handleLoad()
    }

    final override fun onEnable() {
        // Prevent any new modules from being registered with the plugin
        isAcceptingModuleRegistrations = false

        val startTime = System.currentTimeMillis()

        /* Register self as listener where applicable */
        if (this is Listener) {
            Bukkit.getPluginManager().registerEvents(this, this)
        }

        /* Determine if there is a configuration file or not, and save it if there is */
        if (getResource("config.yml") != null) {
            saveDefaultConfig()
            config.options().copyDefaults(false)
            saveConfig()

            hasConfig = true
        } else if (File(dataFolder.path + File.separator + "config.yml").exists()) {
            hasConfig = true
        }

        if (hasConfig) {
            reloadConfig()
        }

        /* Load modules */
        modules.values.forEach { it.enable() }

        /* Handle implementation specific enable tasks */
        try {
            handleEnable()
            handleReload()
        } catch (ex: Exception) {
            LogHelper.severe(this, "An exception occurred while enabling, functionality may be reduced.", ex)
        }

        LogHelper.info(this, "%s v%s by %s ENABLED (%dms)".format(
                name,
                description.version,
                description.authors,
                System.currentTimeMillis() - startTime
        ))
    }

    final override fun reloadConfig() {
        super.reloadConfig()

        if (!hasConfig) {
            return
        }

        isDebugEnabled = config.getBoolean("debug", false)

        autosaveRunnable?.cancel()

        val autosaveInterval = config.getInt("autosave interval", 0)
        if (autosaveInterval == 0) {
            autosaveRunnable = null
            LogHelper.warning(this, "Autosaving disabled. It is recommended to enable it to help prevent data loss!")
        } else {
            autosaveRunnable = object: BukkitRunnable() {
                override fun run() {
                    saveAll(true)
                }
            }

            autosaveRunnable?.runTaskTimer(this, autosaveInterval * 1200L, autosaveInterval * 1200L)
            LogHelper.info(this, "Autosaving enabled. Saving every $autosaveInterval minute(s).")
        }

        handleConfigReload(config)
    }

    fun reloadAll() {
        try {
            reloadConfig()
        } catch (ex: Exception) {
            LogHelper.severe(this, "An exception occurred while reloading config.yml", ex)
        }

        modules.filterValues { it.status.isEnabled }.forEach { it.value.reload() }

        try {
            handleReload()
        } catch (ex: Exception) {
            LogHelper.severe(this, "An exception occurred while handling custom reload tasks", ex)
        }

        server.pluginManager.callEvent(PluginReloadedEvent(this))
    }

    internal fun save(async: Boolean) {
        try {
            handleSave(async)
        } catch (ex: Exception) {
            LogHelper.severe(this, "An exception occurred while saving", ex)
        }
    }

    fun saveAll(async: Boolean) {
        modules.filterValues { it.status.isEnabled }.forEach { it.value.save(async) }

        save(async)
    }

    final override fun onDisable() {
        save(false)

        try {
            handleDisable()
        } catch (ex: Exception) {
            LogHelper.severe(this, "An exception occurred while disabling", ex)
        }

        // Disable modules in reverse order
        modules.values.reversed().filter { it.status.isEnabled }.forEach { it.disable() }
    }

    /**
     * Registers a module. Modules can only be registered while the plugin is loading.
     *
     * @param module The module to register.
     * @return True if the module was successfully registered.
     *         False if the module is already registered.
     *
     * @throws IllegalStateException Thrown if the plugin is not in its loading state.
     */
    protected fun registerModule(module: FlexModule<*>) : Boolean {
        if (!isAcceptingModuleRegistrations) {
            throw IllegalStateException("No longer accepting module registrations")
        }

        val clazz = module.javaClass.kotlin
        if (modules.containsKey(clazz)) {
            return false
        }

        modules.put(clazz, module)
        LogHelper.info(this, "Registered module '${module.name}' (${module.javaClass.canonicalName})")
        return true
    }

    fun <T : FlexModule<*>> getModule(module: KClass<in T>) : T? {
        return modules[module] as T?
    }

    /**
     * Overloaded operator to allow for retrieving a plugin module by its class as a subscript.
     * Ex. `plugin[MessageModule::class]`
     *
     * This method will perform a null check and should only be used if the module is known to be
     * registered under this plugin.
     */
    operator fun <T : FlexModule<*>> get(module: KClass<in T>): T {
        return getModule(module)!!
    }


    // ========================================================================================== //
    // Implementation-specific Methods                                                            //
    // ========================================================================================== //

    /**
     * In most cases, should only be used to register modules.
     *
     * @see [JavaPlugin.onLoad]
     */
    protected open fun handleLoad() { }

    /**
     * In most cases, should only be used to register commands.
     *
     * @see JavaPlugin.onEnable
     */
    protected open fun handleEnable() { }

    /**
     * @see JavaPlugin.reloadConfig
     */
    protected open fun handleConfigReload(config: FileConfiguration) { }

    /**
     * Handles plugin-specific reload tasks.
     */
    protected open fun handleReload() { }

    /**
     * Handles plugin-specific save tasks.
     */
    protected open fun handleSave(async: Boolean) { }

    /**
     * @see JavaPlugin.onDisable
     */
    protected open fun handleDisable() { }

}
