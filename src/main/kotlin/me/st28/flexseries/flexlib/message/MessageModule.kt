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
import org.apache.commons.lang.StringEscapeUtils
import java.io.File
import java.util.*
import java.util.regex.Pattern

class MessageModule<out T : FlexPlugin>(plugin: T) : FlexModule<T>(plugin, "messages", "Manages a plugin's messages") {

    companion object {

        val PATTERN_VARIABLE: Pattern = Pattern.compile("\\{([0-9]+)\\}")

        fun setupPatternReplace(input: String): String {
            return PATTERN_VARIABLE.matcher(input).replaceAll("%$1\\\$s")
        }

    }

    private val messages: MutableMap<String, String> = HashMap()

    private val tags: MutableMap<String, String> = HashMap()

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

        for ((key, value) in messageFile.config.getValues(true)) {
            if (value is String) {
                val curValue = StringEscapeUtils.unescapeJava(value)

                // Check if key is a tag
                if (key == "tag") {
                    tags.put("", curValue)
                    continue
                } else if (key.endsWith(".tag")) {
                    tags.put(key.replace(".tag", ""), curValue)
                    continue
                }

                messages.put(key, curValue)
            }
        }
        LogHelper.info(this, "Loaded ${messages.size} message(s) and ${tags.size} tag(s)")
    }

    private fun getMessageTag(name: String): String {
        var tag: String?
        var curKey = name
        var lastIndex: Int
        do {
            lastIndex = curKey.lastIndexOf(".")

            // Check if the tag exists
            tag = tags[curKey]

            // Update curKey
            if (lastIndex != -1) {
                curKey = curKey.substring(0, lastIndex)
            }
        } while (lastIndex != -1)

        // Try base tag, otherwise return empty string
        return tag ?: tags[""] ?: ""
    }

    fun getMessage(name: String, vararg replacements: Any?): Message {
        val message: String
        if (messages.containsKey(name)) {
            val masterManager = FlexPlugin.getGlobalModule(MasterMessageModule::class)!!
            message = masterManager.processMessage(setupPatternReplace(messages[name]!!))
        } else {
            message = name
        }

        return Message(message.replace("{TAG}", getMessageTag(name)), *replacements)
    }

}