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

import me.st28.flexseries.flexlib.plugin.FlexPlugin
import java.util.*

/**
 * The base FlexLib command framework command handler.
 */
open class BasicCommand {

    /* Immutable command properties */
    val plugin: FlexPlugin
    val label: String
    val aliases: MutableList<String> = ArrayList()

    /* Parent and child commands */
    internal var parent: BasicCommand? = null
    private var defaultSubcommand: String = ""
    internal val subcommands: MutableMap<String, BasicCommand> = HashMap()
    internal val reverseSubcommands: MutableList<Triple<Int, String, BasicCommand>> = ArrayList() // Offset, label, command

    internal val executors: MutableList<CommandExecutor> = ArrayList()

    internal constructor(plugin: FlexPlugin, label: String) {
        this.plugin = plugin
        this.label = label.toLowerCase()
    }

    /**
     * Sets the aliases for this command and updates them for the parent command, if applicable.
     */
    internal fun setAliases(aliases: List<String>) {
        this.aliases.clear()
        this.aliases.addAll(aliases)
        parent?.registerSubcommand(this)
        // TODO: Call this method somewhere
    }

    /**
     * Registers a subcommand under this command.
     */
    internal fun registerSubcommand(command: BasicCommand) {
        command.parent = this
        subcommands.put(command.label.toLowerCase(), command)
        for (alias in command.aliases) {
            subcommands.put(alias.toLowerCase(), command)
        }
    }

    /**
     * Executes this command.
     * Attempts to find a matching [CommandExecutor] and executes it if found.
     *
     * @param context The [CommandContext] in which the command is being executed.
     * @param offset The depth of this command in the execution chain.
     */
    fun execute(context: CommandContext, offset: Int): Any? {
        val args = context.args
        val curArgs = context.getArgs(offset)

        // 1) If the next argument is a valid subcommand, execute it.
        if (args.size > offset && subcommands.containsKey(args[offset].toLowerCase())) {
            return subcommands[args[offset].toLowerCase()]!!.execute(context, offset + 1)
        }

        // 2) Check for reverse subcommands
        for ((cOffset, cLabel, command) in reverseSubcommands) {
            if (cOffset >= curArgs.size) {
                // Not enough arguments, skip
                continue
            }

            if (curArgs[cOffset] == cLabel) {
                // Matching subcommand found, attempt to execute
                return command.execute(context, offset + 1)
            }
        }

        // 3) If this is a dummy command (doesn't have any logic on its own), then attempt to run
        //    its default subcommand
        //if ()
        // TODO: Run default subcommand

        // 4) Find appropriate CommandExecutor and run this command's logic.
        val applicable = executors.filter { curArgs.size >= it.getRequiredArgs() }
        println("Cur arg count: ${curArgs.size}")
        println("Executors: ${executors.size}")
        println("Applicable executors: ${applicable.size}")

        if (applicable.isEmpty()) {
            // No executors found
            println("NO EXECUTOR FOUND, THIS SHOULDN'T HAPPEN")

            // TODO: Show usage and description
            return executors.joinToString("\n") { it.getUsage(context) }
        } else if (applicable.size == 1) {
            // Easy, only one applicable command executor was found
            return applicable[0].execute(context, offset)
        } else {
            // More complicated, more than one applicable command executor was found
            println("> 1 COMMAND EXECUTOR FOUND")
            // TODO: Handle
            return null
        }
    }

}