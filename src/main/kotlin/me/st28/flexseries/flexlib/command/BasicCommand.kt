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
import me.st28.flexseries.flexlib.command.argument.ArgumentResolveException
import me.st28.flexseries.flexlib.command.argument.ArgumentResolver
import me.st28.flexseries.flexlib.logging.LogHelper
import me.st28.flexseries.flexlib.message.Message
import me.st28.flexseries.flexlib.message.sendMessage
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import me.st28.flexseries.flexlib.util.SchedulerUtils
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.reflect.KFunction

/**
 * The base FlexLib command logic.
 */
open class BasicCommand {

    /* Immutable command properties */
    val plugin: FlexPlugin
    val label: String
    val aliases: MutableList<String> = ArrayList()

    /* Command meta */
    var description: String = ""
    var permission: String = ""
    var isPlayerOnly: Boolean = false

    /* Execution-related */
    //var executor: KFunction<Unit>? = null
    var executor: ((CommandContext) -> Unit)? = null
    var argumentConfig: Array<ArgumentConfig> = emptyArray()
    var autoArgumentConfig: Array<ArgumentConfig> = emptyArray()

    /* Parent and child commands */
    var parent: BasicCommand? = null
    var defaultSubcommand: String = ""
    val subcommands: MutableMap<String, BasicCommand> = HashMap()

    internal constructor(plugin: FlexPlugin, label: String) {
        this.plugin = plugin
        this.label = label.toLowerCase()
    }

    /**
     * Sets the aliases for this command and updates them for the parent command, if set.
     */
    internal fun setAliases(aliases: List<String>) {
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
     * Updates the meta for this command.
     *
     * @param meta The information to set for this command.
     * @param handler The handler for this command's logic.
     */
    open internal fun setMeta(meta: CommandHandler, obj: Any, handler: KFunction<Unit>) {
        permission = meta.permission
        isPlayerOnly = meta.playerOnly
        argumentConfig = ArgumentConfig.parse(meta.args, false)
        autoArgumentConfig = ArgumentConfig.parse(meta.autoArgs, true)
        setAliases(meta.aliases.toList())

        executor = { context: CommandContext ->
            try {
                handler.call(obj, context.sender!!, context)
            } catch (ex: Exception) {
                Message.getGlobal("error.internal_error").sendTo(context.sender!!)
                LogHelper.severe(plugin, "An exception occurred while running command ${context.curArgs.joinToString { " " }}", ex)
            }
        }
    }

    /**
     * Returns the usage string for this command within a given {@link CommandContext}.
     */
    open fun getUsage(context: CommandContext): String {
        val sb = StringBuilder()
        sb.append("/").append(context.label)

        for (i in 0 until context.level) {
            sb.append(" ").append(context.rawArgs[i])
        }

        for (ac in argumentConfig) {
            sb.append(" ").append(ac.getUsage(context))
        }

        return sb.toString()
    }

    /**
     * Returns the number of required arguments for this command within a given {@link CommandContext}.
     */
    fun getRequiredArgs(context: CommandContext): Int {
        var count = 0
        argumentConfig.forEach { if (it.isRequired(context)) ++ count }
        return count
    }

    /**
     * Executes this command.
     *
     * @param sender The CommandSender executing the command.
     * @param label The label or alias used to execute this command.
     * @param args The arguments provided for the command's execution.
     * @param offset The depth of this command as a subcommand.
     */
    fun execute(sender: CommandSender, label: String, args: Array<String>, offset: Int) {
        // If the next argument is a valid subcommand, execute it
        if (args.size > offset && subcommands.containsKey(args[offset].toLowerCase())) {
            subcommands[args[offset].toLowerCase()]!!.execute(sender, label, args, offset + 1)
            return
        }

        // If this is a dummy command (doesn't have any logic on its own), then attempt to run its
        // default subcommand
        if (executor == null) {
            // Try default subcommand
            if (defaultSubcommand.isNotEmpty()) {
                val subcmd = subcommands[defaultSubcommand]
                if (subcmd != null) {
                    subcmd.execute(sender, label, args, offset)
                    return
                }
            }

            // Unknown command
            Message.getGlobal("error.unknown_subcommand").sendTo(sender, if (args.size > offset) args[offset] else defaultSubcommand)
            return
        }

        CommandExecutionHandler(CommandContext(this, sender, label, args, offset)).run()
    }

}

/**
 * Contains the logic for resolving a command's arguments and running its logic
 */
private class CommandExecutionHandler(context: CommandContext) {

    private companion object {

        val PATTERN_PERMISSION_VAR: Pattern = Pattern.compile("\\{(n:)?([a-zA-Z0-9-_]+)\\}")

    }

    private val command: BasicCommand
    private val context: CommandContext

    private var permission: String = ""
    private val permissionVariables: MutableMap<String, String> = HashMap() // Argument name, full replacement key

    init {
        this.command = context.command
        this.context = context
    }

    /**
     * Begins execution of the command.
     */
    fun run() {
        val sender = context.sender!!

        // Test permission (if set)
        if (!command.permission.isEmpty()) {
            val matcher: Matcher = PATTERN_PERMISSION_VAR.matcher(command.permission)
            while (matcher.find()) {
                // If group(1) is set, indicates that variable is an argument name
                if (matcher.group(1) != null) {
                    permissionVariables.put(matcher.group(1), matcher.group(0))
                    continue
                }

                val type = matcher.group(2)
                var arg: String = ""
                for (ac in command.argumentConfig) {
                    if (ac.type == type) {
                        arg = ac.name
                    }
                }

                if (arg.isEmpty()) {
                    sendMessage(Message.getGlobal("error.command_unknown_argument", type))
                    return
                }

                permissionVariables.put(arg, matcher.group(0))
            }

            if (permissionVariables.isNotEmpty()) {
                permission = command.permission
            } else if (!sender.hasPermission(command.permission)) {
                sendMessage(Message.getGlobal("error.no_permission"))
                return
            }
        }

        // Check if sender is a player (if command is limited to players only)
        if (command.isPlayerOnly && sender !is Player) {
            sendMessage(Message.getGlobal("error.must_be_player"))
            return
        }

        // Check if there are arguments
        if (command.argumentConfig.isNotEmpty()) {
            if (context.curArgs.size < command.getRequiredArgs(context)) {
                sendMessage(Message.getGlobal("error.command_usage", command.getUsage(context)))
                return
            }

            handleArgument(0)
            return
        }

        // Check if there are auto arguments
        if (command.autoArgumentConfig.isNotEmpty()) {
            handleAutoArgument(0)
            return
        }

        // If no arguments or auto arguments, just run the command
        command.executor!!.invoke(context)
    }

    private fun handleArgument(index: Int) {
        val config = command.argumentConfig[index]
        val resolver: ArgumentResolver<*>? = ArgumentResolver.getResolver(config.type)
        if (resolver == null) {
            sendMessage(Message.getGlobal("error.command_unknown_argument", config.type))
            return
        }

        var resolved: Any? = null
        try {
            if (index >= context.curArgs.size) {
                resolved = resolver.getDefault(context, config)
            } else {
                resolved = resolver.resolve(context, config, context.curArgs[index])
            }
        } catch (ex: ArgumentResolveException) {
            sendMessage(ex.errorMessage)
            return
        } catch (ex: UnsupportedOperationException) { }

        if (resolved == null && resolver.isAsync) {
            SchedulerUtils.runAsync(command.plugin, {
                var asyncResolved: Any? = null
                try {
                    if (index >= context.curArgs.size) {
                        asyncResolved = resolver.getDefaultAsync(context, config)
                    } else {
                        asyncResolved = resolver.resolveAsync(context, config, context.curArgs[index])
                    }
                } catch (ex: ArgumentResolveException) {
                    sendMessage(ex.errorMessage)
                    return@runAsync
                } catch (ex: UnsupportedOperationException) { }

                handleArgument0(resolver, config, asyncResolved, index)
            })
            return
        }

        handleArgument0(resolver, config, resolved, index)
    }

    private fun handleArgument0(resolver: ArgumentResolver<*>, config: ArgumentConfig, value: Any?, index: Int) {
        val argName = config.name

        if (value != null) {
            context.setArgument(argName, value)
        }

        // If pending permission check, attempt to finish permission string
        if (permissionVariables.containsKey(argName)) {
            permission = permission.replace(permissionVariables[argName]!!, (resolver as ArgumentResolver<Any?>).getPermissionString(value))
            permissionVariables.remove(argName)

            // If empty, no more permission variables are pending. Perform permission check.
            if (permissionVariables.isEmpty()) {
                println("Testing permission: $permission")

                SchedulerUtils.runSync(command.plugin, {
                    if (!context.sender!!.hasPermission(command.permission)) {
                        sendMessage(Message.getGlobal("error.no_permission"))
                        return@runSync
                    }
                    handleArgument1(config, value, index)
                })
                return
            }
        }

        handleArgument1(config, value, index)
    }

    private fun handleArgument1(config: ArgumentConfig, value: Any?, index: Int) {
        if (index >= context.curArgs.size - 1) {
            // Done, run command

            if (command.autoArgumentConfig.isNotEmpty()) {
                // Handle auto arguments, if any
                handleAutoArgument(0)
                return
            }

            executeCommand()
        } else {
            // Parse next argument
            handleArgument(index + 1)
        }
    }

    private fun handleAutoArgument(index: Int) {
        val config = command.autoArgumentConfig[index]
        val resolver = ArgumentResolver.getResolver(config.type)
        if (resolver == null) {
            sendMessage(Message.getGlobal("error.command_unknown_argument", config.type))
            return
        }

        var resolved: Any?
        try {
            resolved = resolver.getDefault(context, config)
        } catch (ex: ArgumentResolveException) {
            sendMessage(ex.errorMessage)
            return
        }

        if (resolved == null && resolver.isAsync) {
            SchedulerUtils.runAsync(command.plugin, {
                var asyncResolved: Any?
                try {
                    asyncResolved = resolver.getDefaultAsync(context, config)
                } catch (ex: ArgumentResolveException) {
                    sendMessage(ex.errorMessage)
                    return@runAsync
                }

                handleAutoArgument0(config, asyncResolved, index)
            })
            return
        }

        handleAutoArgument0(config, resolved, index)
    }

    private fun handleAutoArgument0(config: ArgumentConfig, value: Any?, index: Int) {
        if (value != null) {
            context.setArgument(config.name, value)
        }

        if (index >= command.autoArgumentConfig.size - 1) {
            // Done, run command
            executeCommand()
        } else {
            // Parse next auto argument
            handleAutoArgument(index + 1)
        }
    }

    private fun executeCommand() {
        SchedulerUtils.runSync(command.plugin, {
            command.executor!!.invoke(context)
        })
    }

    private fun sendMessage(message: Message) {
        SchedulerUtils.runSync(command.plugin, {
            context.sender?.sendMessage(message)
        })
    }

}
