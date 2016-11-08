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
package me.st28.flexseries.flexlib

import me.st28.flexseries.flexlib.command.CommandModule
import me.st28.flexseries.flexlib.commands.CommandLibTest
import me.st28.flexseries.flexlib.commands.FlexPluginCommands
import me.st28.flexseries.flexlib.message.MasterMessageModule
import me.st28.flexseries.flexlib.message.MessageModule
import me.st28.flexseries.flexlib.permission.PermissionHelper
import me.st28.flexseries.flexlib.player.lookup.PlayerLookupModule
import me.st28.flexseries.flexlib.plugin.FlexPlugin

class FlexLib : FlexPlugin() {

    override fun handleLoad() {
        registerModule(CommandModule(this))
        registerModule(MasterMessageModule(this))
        registerModule(MessageModule(this))
        registerModule(PlayerLookupModule(this))
    }

    override fun handleEnable() {
        isDebugEnabled = true

        commandMap.register(CommandLibTest)
        commandMap.register(FlexPluginCommands)
    }

    override fun handleReload(isFirstReload: Boolean) {
        if (isFirstReload) {
            return
        }
        PermissionHelper.reload(config.getConfigurationSection("permission helper"))
    }

}