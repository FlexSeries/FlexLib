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

import me.st28.flexseries.flexlib.FlexLib
import me.st28.flexseries.flexlib.logging.LogHelper
import me.st28.flexseries.flexlib.message.list.ListHeader
import me.st28.flexseries.flexlib.plugin.FlexModule
import java.util.*
import java.util.regex.Pattern

class MasterMessageModule(plugin: FlexLib) : FlexModule<FlexLib>(plugin, "messages-master", "Main message formatting module") {

    internal companion object {

        val MOOD_PATTERN: Pattern = Pattern.compile("\\{#(\\S+?)#\\}")
        val OBJECT_PATTERN: Pattern = Pattern.compile("\\{\\$(\\S+?)\\$\\}")
        val OBJECT_VALUE_PATTERN: Pattern = Pattern.compile("\\{\\$(\\S+?) (.+?) \\$\\}")
        val OBJECT_SPLIT_PATTERN: Pattern = Pattern.compile("\\{,\\}")

    }

    private val moodFormats: MutableMap<String, String> = HashMap()
    private val objectFormats: MutableMap<String, String> = HashMap()

    internal var listPageItems: Int = 0
    internal var listLineLength: Int = 0

    internal val listElementFormats: MutableMap<String, String> = HashMap()
    internal val listHeaderFormats: MutableMap<String, ListHeader> = HashMap()

    override fun handleReload() {
        /* Load mood formats */
        moodFormats.clear()
        val moodSec = config.getConfigurationSection("format.mood")
        if (moodSec != null) {
            for ((key, value) in moodSec.getValues(true)) {
                moodFormats.put(key, value as String)
            }
        }

        /* Load object formats */
        objectFormats.clear()
        val objectSec = config.getConfigurationSection("format.object")
        if (objectSec != null) {
            for ((key, value) in objectSec.getValues(true)) {
                objectFormats.put(key, value as String)
            }
        }

        /* Load list configuration */
        listPageItems = config.getInt("list.config.page items", 8)
        listLineLength = config.getInt("list.config.line length", 50)

        // Load headers
        listHeaderFormats.clear()
        val listHeaderSec = config.getConfigurationSection("list.format.header")
        if (listHeaderSec != null) {
            for (name in listHeaderSec.getKeys(false)) {
                try {
                    listHeaderFormats.put(name, ListHeader(listHeaderSec.getConfigurationSection(name)))
                } catch (ex: Exception) {
                    LogHelper.severe(this, "An exception occurred while loading list header '$name'", ex)
                }
            }
        }

        // Load elements
        listElementFormats.clear()
        val listElementSec = config.getConfigurationSection("list.format.element")
        if (listElementSec != null) {
            for ((key, value) in listElementSec.getValues(true)) {
                listElementFormats.put(key, value as String)
            }
        }
    }

    fun processMessage(message: String): String {
        /* Process objects with values */
        val stage1 = StringBuilder(message)

        val matcherStage1 = OBJECT_VALUE_PATTERN.matcher(message)
        var offsetStage1 = 0
        while (matcherStage1.find()) {
            val formatName = matcherStage1.group(1)
            var format = objectFormats[formatName]
                    ?: "(unknown format '$formatName')"

            val replacement = matcherStage1.group(2)

            val split = OBJECT_SPLIT_PATTERN.split(replacement)
            if (split.isNotEmpty()) {
                for (i in 0 until split.size) {
                    format = format.replace("{${i + 1}}", split[i])
                }
            }

            format = format.replace("{?}", replacement)

            stage1.replace(matcherStage1.start() + offsetStage1, matcherStage1.end() + offsetStage1, format)
            offsetStage1 -= matcherStage1.end() - matcherStage1.start() - format.length
        }

        /* Process objects with no values */
        val stage2 = StringBuilder(stage1.toString())

        val matcherStage2 = OBJECT_PATTERN.matcher(stage1.toString())
        while (matcherStage2.find()) {
            stage2.replace(matcherStage2.start(), matcherStage2.end(), objectFormats[matcherStage2.group(1)])
        }

        /* Process moods */
        val stage3 = StringBuilder(stage2.toString())

        val matcherStage3 = MOOD_PATTERN.matcher(stage2.toString())

        var lastMood = ""
        var offsetStage3 = 0
        while (matcherStage3.find()) {
            val curMood = matcherStage3.group(1)
            if (curMood != ".") {
                lastMood = curMood
            }

            val replacement = moodFormats[if (curMood == ".") lastMood else curMood]!!

            stage3.replace(matcherStage3.start() + offsetStage3, matcherStage3.end() + offsetStage3, replacement)
            offsetStage3 -= matcherStage3.end() - matcherStage3.start() - replacement.length
        }

        return stage3.toString()
    }

    /**
     * Registers an object format.
     */
    fun registerObjectFormat(name: String, format: String) {
        objectFormats.put(name, format)
    }

    /**
     * Registers a list element format.
     */
    fun registerElementFormat(name: String, format: String) {
        listElementFormats.put(name, format)
    }

}
