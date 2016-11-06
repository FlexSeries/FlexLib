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
package me.st28.flexseries.flexlib.command.argument

import me.st28.flexseries.flexlib.command.CommandContext
import me.st28.flexseries.flexlib.util.GenericDataContainer
import org.bukkit.entity.Player
import java.util.*

class ArgumentConfig : GenericDataContainer {

    companion object {

        val PATTERN_INFO: Regex = Regex("([a-zA-Z0-9-_]+) ([a-zA-Z0-9-_:]+) (always|player|nonplayer|optional)")
        val PATTERN_INFO_AUTO: Regex = Regex("([a-zA-Z0-9-_]+) ([a-zA-Z0-9-_:]+)")
        val PATTERN_OPTION: Regex = Regex("-([a-zA-Z0-9-_]+)(?:=([a-zA-Z0-9-_]+))?")

        fun parse(raw: Array<String>, isAuto: Boolean): Array<ArgumentConfig> {
            if (raw.size == 1 && raw[0].isEmpty()) {
                return arrayOf()
            }

            var ret: MutableList<ArgumentConfig> = ArrayList()
            for (i in 0 .. raw.size) {
                if (isAuto) {
                    ret.add(ArgumentConfig(raw[i]))
                } else {
                    ret.add(ArgumentConfig(raw[i], i))
                }
            }
            return ret.toTypedArray()
        }

    }

    val index: Int
    private val required: RequiredState
    val name: String
    val type: String

    private constructor(raw: String) {
        index = -1
        required = RequiredState.ALWAYS

        val matcher = PATTERN_INFO_AUTO.matchEntire(raw) ?: throw IllegalArgumentException("Invalid auto argument syntax '$raw'")

        name = matcher.groupValues[1]
        type = matcher.groupValues[2]

        parseOptions(raw)
    }

    private constructor(raw: String, index: Int) {
        this.index = index

        val matcher = PATTERN_INFO.matchEntire(raw) ?: throw IllegalArgumentException("Invalid argument syntax '$raw'")

        name = matcher.groupValues[1]
        type = matcher.groupValues[2]
        required = RequiredState.valueOf(matcher.groupValues[3].toUpperCase())


        parseOptions(raw)
    }

    private fun parseOptions(raw: String) {
        PATTERN_OPTION.findAll(raw).forEach {
            var value: Any? = null

            val rawValue: String = it.groupValues[2]
            if (rawValue.isEmpty()) {
                data.put(it.groupValues[1], null)
                return
            }

            // Boolean
            value = when (rawValue) {
                "true" -> value
                "false" -> value
                else -> null
            }

            // Integer
            if (value == null) {
                try {
                    value = rawValue.toInt()
                } catch (ex: NumberFormatException) { }
            }

            // Floating point
            if (value == null) {
                try {
                    value = rawValue.toDouble()
                } catch (ex: NumberFormatException) { }
            }

            // Default to string if value hasn't been set yet at this point
            data.put(it.groupValues[1], value ?: rawValue)
        }
    }

    fun getUsage(context: CommandContext): String {
        return String.format(if (isRequired(context)) "<%s>" else "[%s]", name)
    }

    fun isRequired(context: CommandContext): Boolean {
        return when (required) {
            RequiredState.ALWAYS -> true
            RequiredState.PLAYER -> context.getSender() is Player
            RequiredState.NONPLAYER -> context.getSender() !is Player
            RequiredState.OPTIONAL -> false
        }
    }

}

enum class RequiredState {

    ALWAYS,
    PLAYER,
    NONPLAYER,
    OPTIONAL

}
