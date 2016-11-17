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
import sun.rmi.runtime.Log
import java.io.File
import java.util.*
import kotlin.reflect.KClass

abstract class FlexPlugin : JavaPlugin() {

    companion object {

        fun <T : FlexModule<*>> getGlobalModule(module: KClass<T>): T? {
            return JavaPlugin.getPlugin(FlexLib::class.java)!!.getModule(module)
        }

        fun <T : FlexModule<*>> getPluginModule(plugin: KClass<out FlexPlugin>, module: KClass<in T>) : T? {
            return JavaPlugin.getPlugin(plugin.java)!!.getModule(module)
        }

    }

    var status: PluginStatus = PluginStatus.PENDING
        private set

    var hasConfig = false
        private set

    var isDebugEnabled = false

    lateinit var commandMap: FlexCommandMap
        private set

    val modules: MutableMap<KClass<in FlexModule<*>>, FlexModule<*>> = LinkedHashMap()

    private var autosaveRunnable: BukkitRunnable? = null

    final override fun onLoad() {
        commandMap = FlexCommandMap(this)
        status = PluginStatus.LOADING

        if (getResource("messages.yml") != null) {
            registerModule(MessageModule(this))
        }

        handleLoad()
    }

    final override fun onEnable() {
        val startTime = System.currentTimeMillis()

        status = PluginStatus.ENABLING

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
        for (module in modules.values) {
            module.enable()
        }

        /* Handle implementation specific enable tasks */
        try {
            handleReload(true)
            handleEnable()
            handleReload(false)
        } catch (ex: Exception) {
            status = PluginStatus.ENABLED_ERROR
            LogHelper.severe(this, "An exception occurred while enabling, functionality may be reduced.", ex)
        }

        status = PluginStatus.ENABLED
        LogHelper.info(this, String.format("%s v%s by %s ENABLED (%dms)",
            name,
            description.version,
            description.authors,
            System.currentTimeMillis() - startTime)
        )
    }

    final override fun reloadConfig() {
        super.reloadConfig()

        if (!hasConfig) {
            return
        }

        isDebugEnabled = config.getBoolean("debug", false)

        autosaveRunnable?.cancel()

        var autosaveInterval = config.getInt("autosave interval", 0)
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
            status = PluginStatus.RELOADING

            reloadConfig()

            for (module in modules.values) {
                if (module.status.isEnabled()) {
                    module.reload()
                }
            }

            handleReload(false)

            status = PluginStatus.ENABLED
            server.pluginManager.callEvent(PluginReloadedEvent(this))
        } catch (ex: Exception) {
            status = PluginStatus.ENABLED_ERROR
            throw RuntimeException("An exception occurred while reloading", ex)
        }
    }

    fun saveAll(async: Boolean, finalSave: Boolean = false) {
        for (module in modules.values) {
            if (module.status.isEnabled()) {
                try {
                    module.save(async, finalSave)
                } catch (ex: Exception) {
                    LogHelper.severe(this, "An exception occurred while saving module '${module.name}'", ex)
                }
            }
        }

        try {
            handleSave(async, finalSave)
        } catch (ex: Exception) {
            LogHelper.severe(this, "An exception occurred while saving", ex)
        }

        if (hasConfig) {
            saveConfig()
        }
    }

    final override fun onDisable() {
        status = PluginStatus.DISABLING

        saveAll(false, true)

        try {
            handleDisable()
        } catch (ex: Exception) {
            LogHelper.severe(this, "An exception occurred while disabling", ex)
        }

        for (module in modules.values.reversed()) {
            if (module.status.isEnabled()) {
                try {
                    module.disable()
                } catch (ex: Exception) {
                    LogHelper.severe(this, "An exception occurred while disabling module '${module.name}'", ex)
                }
            }
        }
    }

    /**
     * Registers a module. Modules can only be registered while the plugin is loading.
     *
     * @param module The module to register.
     * @return True if the module was successfully registered.
     *         False if the module is already registered.
     * @throws IllegalStateException Thrown if the plugin is not in its loading state.
     */
    protected fun registerModule(module: FlexModule<*>) : Boolean {
        if (status != PluginStatus.LOADING) {
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

    fun <T: FlexModule<*>> getModule(module: KClass<in T>) : T? {
        return modules[module] as T?
    }

    // ------------------------------------------------------------------------------------------ //
    // Implementation Methods Below                                                               //
    // ------------------------------------------------------------------------------------------ //

    protected open fun handleLoad() { }

    protected open fun handleEnable() { }

    protected open fun handleConfigReload(config: FileConfiguration) { }

    protected open fun handleReload(isFirstReload: Boolean) { }

    protected open fun handleSave(async: Boolean, isFinalSave: Boolean) { }

    protected open fun handleDisable() { }

}