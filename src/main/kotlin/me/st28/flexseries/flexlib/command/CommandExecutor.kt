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
import me.st28.flexseries.flexlib.logging.LogHelper
import me.st28.flexseries.flexlib.message.Message
import me.st28.flexseries.flexlib.message.list.ListBuilder
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import me.st28.flexseries.flexlib.util.SchedulerUtils
import org.bukkit.Bukkit
import java.util.*
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

    fun execute(context: CommandContext, offset: Int) {
        // Get session
        val session = FlexPlugin.getGlobalModule(CommandModule::class)!!.createSession(command, context)
        if (session == null) {
            Message.getGlobal("error.command.already_executing").sendTo(context.sender)
            return
        }

        session.offset = offset

        val args = Stack<String>()
        args.addAll(context.getRelativeArgs().reversed())

        // Make sure enough arguments were provided.
        if (args.size < getRequiredArgs()) {
            // Not enough arguments, show usage
            Message.getGlobal("error.command_usage", getUsage(context)).sendTo(context.sender)
            return
        }

        if (arguments.isEmpty()) {
            // No arguments, run command
            finishExecution(session)
        } else {
            // Get arguments
            handleNextArgument(session, context, args, 0)
        }
    }

    /**
     * Handles an argument config at a time.
     *
     * @param session The session for the command.
     */
    private fun handleNextArgument(session: CommandSession, context: CommandContext, args: Stack<String>, curArg: Int) {
        // Check to see if command execution has been canceled
        if (!session.running) {
            return
        }

        val async = !Bukkit.isPrimaryThread()

        val ac = arguments[curArg]

        val parser = ac.getParser() ?: return session.cancelWith(Message.getGlobal("error.command.unknown_argument_type", ac.type))

        if (args.size < parser.consumed) {
            // Not enough arguments
            if (!ac.isRequired) {
                // Argument isn't required

                // Get default argument or just add null to the params list
                if (ac.default != null) {
                    // Detect synchronous or asynchronous
                    try {
                        if (async) {
                            session.params.add(parser.parseAsync(context, ac, arrayOf(ac.default)))
                        } else {
                            session.params.add(parser.parse(context, ac, arrayOf(ac.default)))
                        }
                    } catch (ex: ArgumentParseException) {
                        return session.cancelWith(ex.errorMessage)
                    }
                } else {
                    session.params.add(null)
                }
            } else {
                // Argument is required

                if (ac.default != null) {
                    val parsed: Any?

                    // Detect synchronous or asynchronous
                    try {
                        if (async) {
                            parsed = parser.parseAsync(context, ac, arrayOf(ac.default))
                        } else {
                            parsed = parser.parse(context, ac, arrayOf(ac.default))
                        }
                    } catch (ex: ArgumentParseException) {
                        return session.cancelWith(ex.errorMessage)
                    }

                    if (parsed == null) {
                        // If default is null and this is async
                        if (async) {
                            println("MISSING REQUIRED ARGUMENT")
                        } else {
                            // Attempt to resolve argument asynchronously
                            SchedulerUtils.runAsync(command.plugin) {
                                handleNextArgument(session, context, args, curArg)
                            }
                        }

                        return
                    }

                    session.params.add(parsed)
                }
            }
        } else {
            // Enough arguments provided

            val consumed = args.takeLast(parser.consumed)

            val parsed = try {
                parser.parse(context, ac, consumed.toTypedArray())
            } catch (ex: ArgumentParseException) {
                return session.cancelWith(ex.errorMessage)
            }

            if (parsed == null && ac.isRequired) {
                println("NULL PARSED")

                if (parser.async) {
                    println("MISSING REQUIRED ARGUMENT")
                } else {
                    // Attempt to resolve argument asynchronously

                    SchedulerUtils.runAsync(command.plugin) {
                        handleNextArgument(session, context, args, curArg)
                    }
                }

                return
            }

            // Drop consumed arguments
            kotlin.repeat(parser.consumed) { args.pop() }

            session.params.add(parsed)
        }

        // Finish command execution or handle next argument synchronously
        SchedulerUtils.runSync(command.plugin) {
            if (curArg + 1 == arguments.size) {
                // We're done with argument config
                finishExecution(session)
            } else {
                // Execute next argument
                handleNextArgument(session, context, args, curArg + 1)
            }
        }
    }

    /**
     * Finishes the execution of the command.
     * This function should be called synchronously.
     */
    private fun finishExecution(session: CommandSession) {
        // Check to see if command execution has been canceled
        if (!session.running) {
            return
        }

        val sender = session.getSender() ?: return
        val params = session.params

        // Add sender to params
        params.add(0, sender)

        // Add context to params, where applicable
        if (requiresContext) {
            params.add(1, CommandContext(sender, session.label, session.args, session.offset))
        }

        var ret: Any?
        try {
            ret = function.call(obj, *params.toTypedArray())
        } catch (ex: Exception) {
            // Catch any other exception
            LogHelper.severe(command.plugin, "An exception occurred during execution of command", ex)
            ret = Message.getGlobal("error.internal_error")
        }

        // Send return value, if possible, to sender
        if (ret is Message) {
            ret.sendTo(sender)
        } else if (ret is String) {
            sender.sendMessage(ret)
        } else if (ret is ListBuilder) {
            ret.sendTo(sender)
        } else if (ret != null) {
            LogHelper.warning(command.plugin, "Unknown command return type '${ret.javaClass.kotlin.qualifiedName}'")
        }

        session.running = false
    }

}