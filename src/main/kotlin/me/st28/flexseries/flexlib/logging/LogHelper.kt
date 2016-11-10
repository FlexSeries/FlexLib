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
package me.st28.flexseries.flexlib.logging

import me.st28.flexseries.flexlib.plugin.FlexModule
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import org.bukkit.Bukkit
import java.util.logging.Level

object LogHelper {

    private fun log(
        plugin: FlexPlugin,
        identifier: String? = null,
        suffix: String? = null,
        level: Level,
        message: String,
        ex: Exception? = null
    ) {
        val prefix: StringBuilder = StringBuilder()

        prefix.append("[")
        prefix.append(plugin.name)
        if (identifier != null) {
            prefix.append("/").append(identifier)
        }

        if (suffix != null) {
            prefix.append(" ").append(suffix)
        }

        prefix.append("]")

        Bukkit.getLogger().log(level, "$prefix $message", ex)
    }

    fun debug(plugin: FlexPlugin, message: String, ex: Exception? = null) {
        if (!plugin.isDebugEnabled) {
            return
        }

        log(
            plugin,
            suffix = "DEBUG",
            level = Level.INFO,
            message = message,
            ex = ex
        )
    }

    fun debug(module: FlexModule<*>, message: String, ex: Exception? = null) {
        if (!module.plugin.isDebugEnabled) {
            return
        }

        log(
            module.plugin,
            identifier = module.name,
            suffix = "DEBUG",
            level = Level.INFO,
            message = message,
            ex = ex
        )
    }

    fun info(plugin: FlexPlugin, message: String, ex: Exception? = null) {
        log(
            plugin,
            level = Level.INFO,
            message = message,
            ex = ex
        )
    }

    fun info(module: FlexModule<*>, message: String, ex: Exception? = null) {
        log(
            module.plugin,
            identifier = module.name,
            level = Level.INFO,
            message = message,
            ex = ex
        )
    }

    fun warning(plugin: FlexPlugin, message: String, ex: Exception? = null) {
        log(
            plugin,
            level = Level.WARNING,
            message = message,
            ex = ex
        )
    }

    fun warning(module: FlexModule<*>, message: String, ex: Exception? = null) {
        log(
            module.plugin,
            identifier = module.name,
            level = Level.WARNING,
            message = message,
            ex = ex
        )
    }

    fun severe(plugin: FlexPlugin, message: String, ex: Exception? = null) {
        log(
            plugin,
            level = Level.SEVERE,
            message = message,
            ex = ex
        )
    }

    fun severe(module: FlexModule<*>, message: String, ex: Exception? = null) {
        log(
            module.plugin,
            identifier = module.name,
            level = Level.SEVERE,
            message = message,
            ex = ex
        )
    }

}