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
package me.st28.flexseries.flexlib.util

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

object SchedulerUtils {

    fun runSync(plugin: JavaPlugin, function: () -> Unit) {
        if (Bukkit.isPrimaryThread()) {
            function.invoke()
        } else {
            Bukkit.getScheduler().runTask(plugin, function)
        }
    }

    fun runAsync(plugin: JavaPlugin, function: () -> Unit) {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, function)
        } else {
            function.invoke()
        }
    }

    fun runAsap(plugin: JavaPlugin, function: () -> Unit, async: Boolean) {
        if (async) {
            runAsync(plugin, function)
        } else {
            runSync(plugin, function)
        }
    }

}