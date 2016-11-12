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
package me.st28.flexseries.flexlib.command

import me.st28.flexseries.flexlib.command.argument.ArgumentConfig
import me.st28.flexseries.flexlib.command.argument.ArgumentParseException
import me.st28.flexseries.flexlib.message.Message
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.defaultType

/**
 * An executor for a command.
 */
internal class CommandExecutor {

    private val command: BasicCommand
    private val obj: Any
    private val function: KFunction<Any>
    private val reverseCount: Int
    private val requiresContext: Boolean

    /* Command meta */
    var description: String = ""
        internal set
    var permission: String = ""
        internal set
    var isPlayerOnly: Boolean = false
        internal set

    private val arguments: MutableList<ArgumentConfig> = ArrayList()

    constructor(meta: CommandHandler, isPlayerOnly: Boolean, command: BasicCommand, obj: Any, function: KFunction<Any>, reverseCount: Int = 0) {
        println("Creating executor...")

        this.command = command
        this.obj = obj
        this.function = function
        this.reverseCount = reverseCount

        this.description = meta.description
        this.permission = meta.permission
        this.isPlayerOnly = isPlayerOnly

        var tempRequiresContext: Boolean = false

        // Load arguments
        // - Always skip first
        // - Skip second if it is a CommandContext
        for ((index, p) in function.parameters.withIndex()) {
            if (p.name == null) {
                println("Skipping null parameter name")
                continue
            }

            // Skip first CommandSender/Player
            if (index == 1) {
                println("Skipping CommandSender/Player")
                continue
            } else if (index == 2 && p.type == CommandContext::class.defaultType) {
                println("Skipping CommandContext")
                tempRequiresContext = true
                continue
            }

            println("Found parameter: $p")

            arguments.add(ArgumentConfig(p))
        }

        requiresContext = tempRequiresContext
    }

    fun getUsage(context: CommandContext? = null): String {
        // TODO: Get labels from context

        val labels = Stack<String>()
        var base: BasicCommand = command
        while (base.parent != null) {
            base = base.parent!!
            labels.add(base.label)
        }

        val sb = StringBuilder(labels.joinToString(" ", prefix = "/"))

        // Add reverse parameters
        for (i in 0 until reverseCount) {
            sb.append(" ").append(arguments[i].getUsage())
        }

        // Add label
        sb.append(" ").append(command.label)

        // Add parameters
        for (i in reverseCount until arguments.size) {
            sb.append(" ").append(arguments[i].getUsage())
        }

        return sb.toString()
    }

    fun getRequiredArgs(): Int {
        return arguments
                .filter { it.isRequired }
                .sumBy {
                    it.getParser()?.consumed ?: 1
                    // Should this throw an error if the parser isn't found?
                }
    }

    fun execute(context: CommandContext, offset: Int): Any? {
        val params: MutableList<Any?> = ArrayList()

        // Add sender to params
        if (isPlayerOnly) {
            params.add(context.sender as Player)
        } else {
            params.add(context.sender)
        }

        // Add context to params, where applicable
        if (requiresContext) {
            params.add(context)
        }

        val args = Stack<String>()
        args.addAll(context.getArgs(offset).reversed())

        // Make sure enough arguments were provided.
        if (args.size < getRequiredArgs()) {
            // Not enough arguments, show usage
            return Message.getGlobal("error.command_usage", getUsage(context))
        }

        // TODO: Get arguments
        for (ac in arguments) {
            val parser = ac.getParser() ?: return Message.getGlobal("error.command.unknown_argument_type", ac.type)

            if (args.size < parser.consumed) {
                // Not enough arguments
                if (!ac.isRequired) {
                    // Argument isn't required

                    // Get default argument or just add null to the params list
                    if (ac.default != null) {
                        params.add(parser.parse(context, ac, arrayOf(ac.default)))
                    } else {
                        params.add(null)
                    }
                    continue
                } else {
                    // Argument is required
                    if (ac.default != null) {
                        val parsed = parser.parse(context, ac, arrayOf(ac.default))

                        if (parsed == null) {
                            // Even default is null, so give error
                            println("MISSING REQUIRED ARGUMENT")
                            return null
                        }

                        params.add(parsed)
                    }
                    continue
                }
            }

            val parsed = parser.parse(context, ac, args.take(parser.consumed).toTypedArray())

            if (parsed == null && ac.isRequired) {
                println("NULL PARSED")
                // If this happens, argument must be parsed asynchronously (where applicable).
                // Otherwise, throw an exception
                return null
            }

            params.add(parsed)
        }

        return function.call(obj, *params.toTypedArray())
    }

}