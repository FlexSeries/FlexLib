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
import me.st28.flexseries.flexlib.message.Message
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import me.st28.flexseries.flexlib.util.SchedulerUtils
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.reflect.KFunction

// TODO: Refactor the heck out of this monstrosity of a command library (it's pretty bad)
open class BasicCommand internal constructor(plugin: FlexPlugin, label: String) {

    val plugin: FlexPlugin
    val label: String
    val aliases: MutableList<String> = ArrayList()

    var description: String? = null
    var permission: String? = null
    var isPlayerOnly: Boolean = false

    var executor: KFunction<Unit>? = null
    //var executor: ((sender: CommandSender, context: CommandContext) -> Unit)? = null
    var argumentConfig: Array<ArgumentConfig> = arrayOf()
    var autoArgumentConfig: Array<ArgumentConfig> = arrayOf()

    var parent: BasicCommand? = null
    var defaultSubcommand: String? = null
    val subcommands: MutableMap<String, BasicCommand> = HashMap()

    init {
        this.plugin = plugin
        this.label = label.toLowerCase()
    }

    internal fun setAliases(aliases: List<String>) {
        this.aliases.addAll(aliases)
        parent?.registerSubcommand(this)
    }

    internal fun registerSubcommand(command: BasicCommand) {
        command.parent = this
        subcommands.put(command.label.toLowerCase(), command)
        for (alias in command.aliases) {
            subcommands.put(alias.toLowerCase(), command)
        }
    }

    open internal fun setMeta(meta: CommandHandler?, handler: KFunction<Unit>?) {
    //open internal fun setMeta(meta: CommandHandler?, handler: ((sender: CommandSender, context: CommandContext) -> Unit)?) {
        if (meta != null) {
            permission = meta.permission
            isPlayerOnly = meta.playerOnly
        }

        executor = handler
    }

    open fun getUsage(context: CommandContext): String {
        val sb = StringBuilder()
        sb.append("/").append(context.labels[0])

        for (i in 0 .. context.level) {
            sb.append(" ").append(context.rawArgs[i])
        }

        for (ac in argumentConfig) {
            sb.append(" ").append(ac.getUsage(context))
        }

        return sb.toString()
    }

    fun getRequiredArgs(context: CommandContext): Int {
        var count = 0
        argumentConfig.forEach { if (it.isRequired(context)) ++count }
        return count
    }

    fun execute(sender: CommandSender, label: String, args: Array<String>, offset: Int) {
        if (args.size > offset && subcommands.containsKey(args[offset].toLowerCase())) {
            subcommands[args[offset].toLowerCase()]!!.execute(sender, label, args, offset + 1)
            return
        }

        if (executor == null) {
            // Try default subcommand
            if (defaultSubcommand != null) {
                subcommands[defaultSubcommand as String]?.execute(sender, label, args, offset)
                return
            }

            // Unknown command
            Message.getGlobal("error.unknown_subcommand").sendTo(sender, if (args.size > offset) args[offset] else defaultSubcommand)
            return
        }

        CommandExecutionHandler(CommandContext(this, sender, label, args, offset)).run()
    }

}

private class CommandExecutionHandler(context: CommandContext) {

    companion object {
        val PATTERN_PERMISSION_VAR: Pattern = Pattern.compile("\\{(n:)?([a-zA-Z0-9-_]+)\\}")
    }

    private val command: BasicCommand
    private val context: CommandContext

    private var permission: String? = null
    private val permissionVariables: MutableMap<String, String> = HashMap()

    init {
        this.command = context.command
        this.context = context
    }

    fun run() {
        val sender = context.getSender()

        // Test permission (if set)
        if (!command.permission.isNullOrEmpty()) {
            val matcher: Matcher = PATTERN_PERMISSION_VAR.matcher(command.permission)
            while (matcher.find()) {
                // If group(1) is set, indicates that variable is an argument name
                if (matcher.group(1) != null) {
                    permissionVariables.put(matcher.group(1), matcher.group(0))
                } else {
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
            }

            if (!permissionVariables.isEmpty()) {
                permission = command.permission
            } else if (!sender!!.hasPermission(command.permission)) {
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
        //command.executor!!.invoke(context.getSender()!!, context)
        command.executor!!.call(context.getSender()!!, context)
    }

    private fun handleArgument(index: Int) {
        val config = command.argumentConfig[index]
        val resolver: ArgumentResolver<*>? = ArgumentResolver.getResolver(config.type)
        if (resolver == null) {
            sendMessage(Message.getGlobal("error.command_unknown_argument", config.type))
            return
        }

        var resolved: Any?
        try {
            resolved = resolver.resolve(context, config, context.curArgs[index])
        } catch (ex: ArgumentResolveException) {
            sendMessage(ex.errorMessage)
            return
        }

        if (resolved == null && resolver.isAsync) {
            SchedulerUtils.runAsync(command.plugin, Runnable {
                var asyncResolved: Any?
                try {
                    asyncResolved = resolver.resolveAsync(context, config, context.curArgs[index])
                } catch (ex: ArgumentResolveException) {
                    sendMessage(ex.errorMessage)
                    return@Runnable
                }

                handleArgument0(resolver, config, asyncResolved, index)
            })
            return
        }

        handleArgument0(resolver, config, resolved, index)
    }

    private fun handleArgument0(resolver: ArgumentResolver<*>, config: ArgumentConfig, value: Any?, index: Int) {
        val argName = config.name

        context.setArgument(argName, value)

        // If pending permission check, attempt to finish permission string
        if (permissionVariables.containsKey(argName)) {
            permission = permission!!.replace(permissionVariables[argName]!!, (resolver as ArgumentResolver<Any?>).getPermissionString(value))
            permissionVariables.remove(argName)

            // If empty, no more permission variables are pending. Perform permission check.
            if (permissionVariables.isEmpty()) {
                SchedulerUtils.runSync(command.plugin, Runnable {
                    if (!context.getSender()!!.hasPermission(command.permission)) {
                        sendMessage(Message.getGlobal("error.no_permission"))
                        return@Runnable
                    }
                    handleArgument1(config, value, index)
                })
                return
            }
        }

        handleArgument1(config, value, index)
    }

    private fun handleArgument1(config: ArgumentConfig, value: Any?, index: Int) {
        if (index == context.curArgs.size - 1) {
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
            SchedulerUtils.runAsync(command.plugin, Runnable {
                var asyncResolved: Any?
                try {
                    asyncResolved = resolver.getDefaultAsync(context, config)
                } catch (ex: ArgumentResolveException) {
                    sendMessage(ex.errorMessage)
                    return@Runnable
                }

                handleAutoArgument0(config, asyncResolved, index)
            })
            return
        }

        handleAutoArgument0(config, resolved, index)
    }

    private fun handleAutoArgument0(config: ArgumentConfig, value: Any?, index: Int) {
        context.setArgument(config.name, value)

        if (index == command.autoArgumentConfig.size - 1) {
            // Done, run command
            executeCommand()
        } else {
            // Parse next auto argument
            handleAutoArgument(index + 1)
        }
    }

    private fun executeCommand() {
        SchedulerUtils.runSync(command.plugin, Runnable {
            //command.executor!!.invoke(context.getSender()!!, context)
            command.executor!!.call(context.getSender()!!, context)
        })
    }

    private fun sendMessage(message: Message) {
        SchedulerUtils.runSync(command.plugin, Runnable {
            val sender = context.getSender()
            if (sender != null) {
                message.sendTo(sender)
            }
        })
    }

}
