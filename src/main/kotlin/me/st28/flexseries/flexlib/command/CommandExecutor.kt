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
import me.st28.flexseries.flexlib.util.toInt
import org.bukkit.Bukkit
import java.util.*
import kotlin.reflect.KFunction
import kotlin.reflect.defaultType

/**
 * An executor for a command.
 */
internal class CommandExecutor(
        meta: CommandHandler,
        isPlayerOnly: Boolean,
        command: BasicCommand,
        obj: Any,
        function: KFunction<Any>,
        reverseCount: Int = 0)
{

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

    init {
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

        val labels: MutableList<String> = ArrayList()

        var cur: BasicCommand = command
        do {
            if (cur is FlexCommand && context != null) {
                labels.add(context.label)
            } else {
                labels.add(cur.label)
            }
            cur = cur.parent ?: break
        } while (true)

        val sb = StringBuilder(labels.reversed().joinToString(" ", prefix = "/"))

        // Add parameters
        for (i in reverseCount until arguments.size) {
            sb.append(" ").append(arguments[i].getUsage())
        }

        return sb.toString()
    }

    fun getRequiredArgs(): Int {
        return arguments.sumBy {
            val parser = it.getParser()!!

            if (parser.consumed == 1) {
                return@sumBy it.isRequired.toInt()
            }

            //return parser.consumed - it.minArgs
            return@sumBy it.minArgs
        }
    }

    fun execute(context: CommandContext, offset: Int) {
        // Get session
        val session = FlexPlugin.getGlobalModule(CommandModule::class).createSession(command, context)
        if (session == null) {
            Message.getGlobal("error.command.already_executing").sendTo(context.sender)
            return
        }

        session.offset = offset

        val args = ArrayDeque<String>()
        args.addAll(context.getRelativeArgs())

        println("Using args: ${args.joinToString(", ")}")
        println("Required arg count: ${getRequiredArgs()}")

        // Permission check
        if (permission.isNotEmpty() && !context.sender.hasPermission(permission)) {
            return session.cancelWith(Message.getGlobal("error.no_permission"))
        }

        // Make sure enough arguments were provided.
        if (args.size < getRequiredArgs()) {
            // Not enough arguments, show usage
            return session.cancelWith(Message.getGlobal("error.command_usage", getUsage(context)))
        }

        if (arguments.isEmpty()) {
            // No arguments, run command
            finishExecution(session)
        } else {
            // Get arguments
            handleNextArgument0(session, context, args, 0)
        }
    }

    /**
     * Handles a single argument config entry at a time.
     *
     * @param session The current command session.
     * @param context The context that the command was executed in.
     * @param args The user-provided arguments.
     * @param curArg The current argument config we're working with.
     */
    private fun handleNextArgument0(session: CommandSession, context: CommandContext, args: Deque<String>, curArg: Int) {
        /// 1) Check to see if command execution has been canceled, and return if so
        if (!session.running) {
            return
        }

        // Convenience variable for checking if we're on the main thread or not
        val async = !Bukkit.isPrimaryThread()

        // The current ArgumentConfig we're working with
        val ac = arguments[curArg]

        /// 2) Get the parser for the argument type
        val parser = ac.getParser()
                ?: return session.cancelWith(Message.getGlobal("error.command.unknown_argument_type", ac.type))

        var actualConsumed: Int = 0
        val parsed = if (args.size < parser.consumed) {
            /*
             * ===== Section A =====
             *
             * Not enough arguments were given by the user.
             *
             * Here we do one of two things:
             * 1. Get the default value from the parser if ac has the default annotation.
             *   a. If the default value is null, we will rerun this method asynchronously to parse
             *       the default value asynchronously.
             *   b. If this is run asynchronously and the default value is null, we do not ignore
             *       any ArgumentParseExceptions.
             * 2. Otherwise, do one of the following based on whether the argument is required:
             *   a. Optional: Add null
             *   b. Required: Show usage message
             *
             */

            if (ac.default != null) {
                /// A: 1.a

                val minArgs = ac.minArgs

                if (args.size >= minArgs) {
                    // Enough arguments to fulfill default parser

                    val argsWithDefault: MutableList<String> = ArrayList()

                    // 1) Add user-given argument(s)
                    argsWithDefault.addAll(args.take(parser.consumed - parser.defaultMinArgs))
                    actualConsumed = argsWithDefault.size

                    // 2) If there's not enough arguments to fulfill the parser's consumed count,
                    //    add the last of the default values
                    if (argsWithDefault.size < parser.consumed) {
                        argsWithDefault.addAll(ac.default.value.split(" ").takeLast(parser.consumed - parser.defaultMinArgs))

                        // 3) Fill the rest if the consumed count hasn't been met yet
                        if (argsWithDefault.size < parser.consumed) {
                            kotlin.repeat(parser.consumed - argsWithDefault.size) {
                                argsWithDefault.add("")
                            }
                        }
                    }

                    // Detect synchronous or asynchronous
                    try {
                        if (async) {
                            parser.parseAsync(context, ac, argsWithDefault.toTypedArray())
                        } else {
                            parser.parse(context, ac, argsWithDefault.toTypedArray())
                        }
                    } catch (ex: ArgumentParseException) {
                        if (!async && !ac.isRequired) {
                            // Ignore
                            null
                        } else {
                            return session.cancelWith(ex.errorMessage)
                        }
                    }
                } else {
                    // TODO: Show usage?
                    null
                }
            } else {
                null
            }

            /*if (parsed == null && parser.async && !async) {
                /// A: 1a

                SchedulerUtils.runAsync(command.plugin) {
                    handleNextArgument0(session, context, args, curArg)
                }
                return
            }

            if (parsed == null && ac.isRequired) {
                /// A: 2b

                // TODO: Show usage message
                throw RuntimeException("TODO: USAGE MESSAGE")
            }

            /// A: 2a
            session.params.add(parsed)*/

            /* ===== SECTION A ===== */
            //return
        } else {
            /*
             * ===== Section B =====
             *
             * Enough arguments were given by the user.
             *
             */

            // Consume required number of arguments
            val consumed = args.take(parser.consumed)
            actualConsumed = parser.consumed

            try {
                if (async) {
                    parser.parseAsync(context, ac, consumed.toTypedArray())
                } else {
                    parser.parse(context, ac, consumed.toTypedArray())
                }
            } catch (ex: ArgumentParseException) {
                return session.cancelWith(ex.errorMessage)
            }
        }

        // If the parsed value is null and the parser supports async and we're not async right now,
        // re-run asynchronously.
        if (parsed == null && parser.async && !async) {
            SchedulerUtils.runAsync(command.plugin) {
                handleNextArgument0(session, context, args, curArg)
            }
            return
        }

        // If argument is required and parsed is null, show usage message
        if (parsed == null && ac.isRequired) {
            // TODO: Show usage message
            throw RuntimeException("TODO: USAGE MESSAGE")
        }

        // Drop consumed arguments
        kotlin.repeat(actualConsumed) { args.pop() }

        session.params.add(parsed)

        // Finish command execution or handle next argument synchronously
        SchedulerUtils.runSync(command.plugin) {
            if (curArg + 1 == arguments.size) {
                // We're done with argument config
                finishExecution(session)
            } else {
                // Execute next argument
                handleNextArgument0(session, context, args, curArg + 1)
            }
        }
    }

    /**
     * Handles an argument config at a time.
     *
     * @param session The session for the command.
     */
    /*private fun handleNextArgument(session: CommandSession, context: CommandContext, args: Deque<String>, curArg: Int) {
        // Check to see if command execution has been canceled
        if (!session.running) {
            return
        }

        val async = !Bukkit.isPrimaryThread()

        val ac = arguments[curArg]

        val parser = ac.getParser() ?: return session.cancelWith(Message.getGlobal("error.command.unknown_argument_type", ac.type))

        if (args.size < parser.consumed) {
            // Not enough arguments

            println("NOT ENOUGH ARGUMENTS GIVEN")

            if (!ac.isRequired) {
                // Argument isn't required

                println("ARGUMENT NOT REQUIRED")

                // Get default argument or just add null to the params list
                if (ac.default != null) {
                    val minArgs = ac.minArgs

                    if (args.size >= minArgs) {
                        // Enough arguments to fulfill default parser

                        val argsWithDefault: MutableList<String> = ArrayList()

                        // 1) Add user-given argument(s)
                        argsWithDefault.addAll(args.take(parser.consumed - parser.defaultMinArgs))

                        // 2) If there's not enough arguments to fulfill the parser's consumed count,
                        //    add the last of the default values
                        if (argsWithDefault.size < parser.consumed) {
                            argsWithDefault.addAll(ac.default.value.split(" ").takeLast(parser.consumed - parser.defaultMinArgs))

                            // 3) Fill the rest if the consumed count hasn't been met yet
                            if (argsWithDefault.size < parser.consumed) {
                                kotlin.repeat(parser.consumed - argsWithDefault.size) {
                                    argsWithDefault.add("")
                                }
                            }
                        }

                        // Detect synchronous or asynchronous
                        try {
                            if (async) {
                                session.params.add(parser.parseAsync(context, ac, argsWithDefault.toTypedArray()))
                            } else {
                                session.params.add(parser.parse(context, ac, argsWithDefault.toTypedArray()))
                            }
                        } catch (ex: ArgumentParseException) {
                            return session.cancelWith(ex.errorMessage)
                        }
                    }
                } else {
                    session.params.add(null)
                }
            } else {
                // Argument is required

                println("ARGUMENT REQUIRED")

                if (ac.default != null) {
                    val parsed: Any?

                    // Detect synchronous or asynchronous
                    try {
                        if (async) {
                            parsed = parser.parseAsync(context, ac, arrayOf(ac.default.value))
                        } else {
                            parsed = parser.parse(context, ac, arrayOf(ac.default.value))
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

            println("ENOUGH ARGUMENTS PROVIDED")

            val consumed = args.take(parser.consumed)

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
    }*/

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
        } else if (ret is Unit) {
            // Ignore
        } else if (ret != null) {
            LogHelper.warning(command.plugin, "Unknown command return type '${ret.javaClass.kotlin.qualifiedName}'")
        }

        session.running = false
    }

}
