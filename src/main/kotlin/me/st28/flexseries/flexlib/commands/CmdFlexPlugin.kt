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
package me.st28.flexseries.flexlib.commands

import me.st28.flexseries.flexlib.command.CommandHandler
import me.st28.flexseries.flexlib.message.Message
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

object CmdFlexPlugin {

    @CommandHandler(
        "flexreload",
        description = "Reloads a FlexPlugin or specified module",
        permission = "flexlib.reload"
    )
    fun reload(sender: CommandSender, plugin: String, module: String?): Message {
        val found = Bukkit.getPluginManager().plugins.firstOrNull { it.name.equals(plugin, true) }
            ?: return Message.getGlobal("error.plugin_not_found", plugin)

        if (found !is FlexPlugin) {
            return Message.getGlobal("error.plugin_not_flexplugin", found.name)
        }

        val foundModule = if (module == null) {
            null
        } else {
            found.modules.values.firstOrNull { it.name.equals(module, true) }
        }

        return if (foundModule == null) {
            // Reload plugin
            found.reloadAll()
            Message.getGlobal("notice.plugin_reloaded", found.name)
        } else {
            // Reload module

            foundModule.reload()
            Message.getGlobal("notice.module_reloaded", found.name, foundModule.name)
        }
    }

}