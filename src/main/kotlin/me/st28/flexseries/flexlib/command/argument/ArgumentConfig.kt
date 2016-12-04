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

import me.st28.flexseries.flexlib.command.CommandModule
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import me.st28.flexseries.flexlib.util.GenericDataContainer
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * Stores an argument's configuration for a command.
 */
class ArgumentConfig(p: KParameter) : GenericDataContainer() {

    val name: String
    val type: KType
    val isRequired: Boolean
    val default: Default?

    init {
        this.name = p.name!!
        this.type = p.type

        // Check for default raw value
        default = (p.annotations.firstOrNull { it is Default } as Default?)

        // Nullable type = not required
        // Nullable + default = not required
        this.isRequired = !p.type.isMarkedNullable && default == null

        println("Argument")
        println(" Name: $name")
        println(" Type: $type")
        println(" Required: $isRequired")
        if (default != null) {
            println(" Default: ${default.value}")
            println(" Default min args: ${default.minArgs}")
        }

        // TODO: Options
    }

    fun getUsage(): String {
        return if (isRequired) {
            "<$name>"
        } else {
            "[$name]"
        }
    }

    fun getParser(): ArgumentParser<Any>? {
        return FlexPlugin.getGlobalModule(CommandModule::class).getArgumentParser(type)
    }

}