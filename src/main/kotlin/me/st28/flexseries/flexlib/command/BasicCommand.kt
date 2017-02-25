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

import me.st28.flexseries.flexlib.message.list.ListBuilder
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import org.apache.commons.lang.mutable.Mutable
import java.util.*

/**
 * The base FlexLib command framework command handler.
 */
open class BasicCommand {

    // Used for session tracking
    internal val uuid: UUID = UUID.randomUUID()

    /* Immutable command properties */
    val plugin: FlexPlugin
    val label: String
    val aliases: MutableList<String> = ArrayList()

    /* Parent and child commands */
    internal var parent: BasicCommand? = null
    internal var defaultSubcommand: String? = null
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
    open fun setAliases(aliases: List<String>) {
        this.aliases.clear()
        this.aliases.addAll(aliases)
        parent?.registerSubcommand(this)
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
        if (args.size > offset) {
            val subLabel = args[offset].toLowerCase()
            if (subLabel == "help") {
                // Help command
                return showHelp(context)
            } else if (subcommands.containsKey(subLabel)) {
                // Registered subcommand
                ++context.offset
                return subcommands[args[offset].toLowerCase()]!!.execute(context, offset + 1)
            }
        }

        // 2) Check for reverse subcommands
        // TODO: Reverse subcommand execution (on hold)
        /*for ((cOffset, cLabel, command) in reverseSubcommands) {
            if (cOffset >= curArgs.size) {
                // Not enough arguments, skip
                continue
            }

            if (curArgs[cOffset] == cLabel) {
                // Matching subcommand found, attempt to execute
                return command.execute(context, offset + 1)
            }
        }*/


        // 3) Find appropriate CommandExecutor and run this command's logic.
        val applicable = executors.filter {
            curArgs.size >= it.getRequiredArgs()
        }

        if (applicable.isEmpty()) {
            // No executors found (dummy command)

            if (subcommands.isEmpty()) {
                // If no subcommands, show usage
                return executors.joinToString("\n") { it.getUsage(context) }
            }

            // There are subcommands, try default subcommand if set
            val foundDefault = subcommands[defaultSubcommand]
            if (foundDefault != null) {
                return foundDefault.execute(context, offset)
            }

            // Default to help command
            return showHelp(context)

            // TODO: Show usage and description
        } else if (applicable.size == 1) {
            // Easy, only one applicable command executor was found
            return applicable[0].execute(context, offset)
        } else {
            // More complicated, more than one applicable command executor was found
            // TODO: Handle
            return null
        }
    }

    private fun showHelp(context: CommandContext): ListBuilder {
        val builder = ListBuilder()

        val page: Int = try {
            Integer.parseInt(context.getArgs(context.offset + 1)[0])
        } catch (ex: Exception) {
            1
        }

        val rawPath: MutableList<String> = ArrayList()
        rawPath.add(label)
        var temp = parent
        while (temp != null) {
            rawPath.add(temp.label)
            temp = temp.parent
        }

        val path = rawPath.reversed().joinToString(" ", "/")

        val fullHelp: MutableList<Pair<String, String>> = ArrayList()

        // Helper function
        val addEntry = fun (executor: CommandExecutor) {
            fullHelp.add(Pair(executor.getUsage(context), if (executor.description.isEmpty())
                        "(no description set)" else executor.description))
        }

        // Add own executors
        executors.forEach(addEntry)

        // Add subcommand executors
        subcommands.values.forEach { it.executors.forEach(addEntry) }

        builder.page(page, fullHelp.count())

        builder.header("help", path)

        builder.elements("command", { index -> fullHelp[index].toList().toTypedArray() })

        return builder
    }

}
