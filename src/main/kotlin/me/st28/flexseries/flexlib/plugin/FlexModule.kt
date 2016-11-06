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
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.regex.Pattern

abstract class FlexModule<out T : FlexPlugin>(plugin: T, name: String, description: String) {

    companion object {
        val NAME_PATTERN: Pattern = Pattern.compile("[a-zA-Z0-9-_]+")
    }

    val plugin: T
    val name: String
    val description: String
    var status: ModuleStatus = ModuleStatus.PENDING
        private set

    var dataFolder: File
        get() {
            if (!dataFolder.exists()) {
                dataFolder.mkdirs()
            }
            return dataFolder
        }

        private set

    private lateinit var config: YamlFileManager

    init {
        this.plugin = plugin
        this.name = name
        this.description = description

        Validate.isTrue(NAME_PATTERN.matcher(name).matches(), "Name must be alphanumeric with only dashes and underscores")

        dataFolder = File(plugin.dataFolder.path + File.separator + name)
    }

    /**
     * Enables the module.
     */
    fun enable(): Boolean {
        when (status) {
            ModuleStatus.PENDING,
            ModuleStatus.DISABLED,
            ModuleStatus.DISABLED_DEPENDENCY,
            ModuleStatus.DISABLED_ERROR
            -> {
                return false
            }
        }

        val startTime = System.currentTimeMillis()
        status = ModuleStatus.LOADING

        config = YamlFileManager(plugin.dataFolder.path + File.separator + name)
        if (config.isEmpty()) {
            val configHandle = config.config

            val def = plugin.getResource("modules/$name/config.yml")
            if (def != null) {
                configHandle.addDefaults(YamlConfiguration.loadConfiguration(InputStreamReader(def)))
                configHandle.options().copyDefaults(true)
                config.save()
            }
        }

        try {
            config.reload()
            handleReload(true)
            handleEnable()
            handleReload(false)
        } catch (ex: Exception) {
            status = ModuleStatus.DISABLED_ERROR
            throw RuntimeException("An exception occurred while enabling module '$name'", ex)
        }

        // Register as listener where applicable
        if (this is Listener) {
            plugin.server.pluginManager.registerEvents(this, plugin)
            LogHelper.debug(this, "Registered listener")
        }

        status = ModuleStatus.ENABLED
        LogHelper.info(this, String.format("Module enabled (%dms)", System.currentTimeMillis() - startTime))
        return true
    }

    fun reload() {
        status = ModuleStatus.RELOADING

        config.reload()

        try {
            handleReload(false)
        } catch (ex: Exception) {
            status = ModuleStatus.ENABLED_ERROR
            throw RuntimeException("An exception occurred while reloading module '$name'", ex)
        }

        status = ModuleStatus.ENABLED
    }

    fun save(async: Boolean, isFinalSave: Boolean = false) {
        handleSave(async, isFinalSave)
    }

    fun disable() {
        status = ModuleStatus.UNLOADING

        // Unregister self as listener where applicable
        if (this is Listener) {
            HandlerList.unregisterAll(this)
            LogHelper.debug(this, "Unregistered listener")
        }

        try {
            handleSave(false, true)
            config.save()
            handleDisable()
        } catch (ex: Exception) {
            status = ModuleStatus.DISABLED_ERROR
            throw RuntimeException("An exception occurred while disabling module '$name'", ex)
        }

        status = ModuleStatus.DISABLED
        LogHelper.debug(this, "Module disabled")
    }

    fun getConfig(): FileConfiguration {
        return config.config
    }

    fun getResource(name: String): InputStream {
        return plugin.getResource("modules/${this.name}/$name")
    }

    /**
     * Handles module-specific enable tasks.
     */
    protected open fun handleEnable() { }

    /**
     * Handles module-specific reload tasks.
     * @param isFirstReload True if this method is being called for the first time for a module.
     *                      The first reload will be called prior to the plugin being enabled.
     */
    protected open fun handleReload(isFirstReload: Boolean) { }

    /**
     * Handles module-specific save tasks.
     * @param async True if the module should save data asynchronously (where applicable).
     * @param isFinalSave True if the method is being called for the final time before the plugin is
     *                    disabled.
     */
    protected open fun handleSave(async: Boolean, isFinalSave: Boolean) { }

    /**
     * Handles module-specific disable tasks.
     */
    protected open fun handleDisable() { }

}