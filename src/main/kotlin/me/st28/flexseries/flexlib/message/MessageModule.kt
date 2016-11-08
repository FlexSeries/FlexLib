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
package me.st28.flexseries.flexlib.message

import me.st28.flexseries.flexlib.logging.LogHelper
import me.st28.flexseries.flexlib.plugin.FlexModule
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import me.st28.flexseries.flexlib.plugin.storage.flatfile.YamlFileManager
import java.io.File
import java.util.*
import java.util.regex.Pattern

class MessageModule<out T : FlexPlugin>(plugin: T) : FlexModule<T>(plugin, "messages", "Manages a plugin's messages") {

    companion object {
        val PATTERN_VARIABLE: Pattern = Pattern.compile("\\{([0-9]+)\\}")
    }

    private val messages: MutableMap<String, String> = HashMap()

    override fun handleReload(isFirstReload: Boolean) {
        if (isFirstReload) {
            return
        }

        messages.clear()

        val messageFile = YamlFileManager(plugin.dataFolder.path + File.separator + "messages.yml")
        if (messageFile.isEmpty()) {
            plugin.saveResource("messages.yml", true)
            messageFile.reload()
        }

        for ((key, value) in messageFile.config.getValues(true).entries) {
            if (value is String) {
                messages.put(key, value)
            }
        }
        LogHelper.info(this, "Loaded ${messages.size} message(s)")
    }

    fun getMessage(name: String, vararg replacements: Any?): Message {
        val message: String
        if (messages.containsKey(name)) {
            message = PATTERN_VARIABLE.matcher(messages[name]).replaceAll("%$1\\\$s")
        } else {
            message = name
        }
        return Message(message, *replacements)
    }

}