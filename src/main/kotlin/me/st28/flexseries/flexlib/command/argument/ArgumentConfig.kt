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
 *
 * An argument is considered "required" when one or more of the following conditions is met:
 * - The argument type is not nullable
 * - Default annotation is set
 */
class ArgumentConfig(p: KParameter) : GenericDataContainer() {

    val name: String
    val type: KType

    /* Annotations */
    val default: Default?
    val multiArg: MultiArg?

    private val isMarkedNullable: Boolean
    val isRequired: Boolean
        get() {
            return !isMarkedNullable && default == null
            /*return if (default == null) {
                return !isMarkedNullable
            } else {
                //minArgs < getParser()!!.consumed
                val parser = getParser()!!
                if (parser.consumed == 1) {
                    false
                } else {
                    minArgs < getParser()!!.defaultMinArgs
                }
            }*/
        }

    internal val minArgs: Int
        get() {
            if (default != null) {
                return if (default.minArgs != -1) {
                    default.minArgs
                } else {
                    getParser()!!.defaultMinArgs
                }
            }

            // Irrelevant
            return 0
        }

    init {
        this.name = p.name!!
        this.type = p.type

        // Get annotations
        default = (p.annotations.firstOrNull { it is Default } as Default?)
        multiArg = (p.annotations.firstOrNull { it is MultiArg } as MultiArg?)

        // Nullable type = not required
        // Nullable + default = not required
        this.isMarkedNullable = p.type.isMarkedNullable

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
        val parser = getParser()!!

        // If the parser consumes more than one, we have a more complicated usage message to build
        if (parser.consumed > 1) {
            val sb = StringBuilder()

            for (i in 0 until parser.consumed) {
                val isOptional = i >= minArgs

                if (i > 0) {
                    sb.append(" ")
                }

                sb.append(if (isOptional) {
                    "["
                } else {
                    "<"
                })

                sb.append(if (i == 0) {
                    // First iteration = base label
                    name
                } else {
                    if (multiArg != null && multiArg.labels.size >= i) {
                        // Get label from multiArg
                        multiArg.labels[i - 1]
                    } else if (parser.defaultLabels != null && parser.defaultLabels.size >= i) {
                        // Get label from parser
                        parser.defaultLabels[i - 1]
                    } else {
                        // Generic label name
                        "arg$i"
                    }
                })

                sb.append(if (isOptional) {
                    "]"
                } else {
                    ">"
                })
            }

            return sb.toString()
        }

        // Parser only consumes one argument, simple usage message
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
