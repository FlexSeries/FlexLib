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

import me.st28.flexseries.flexlib.logging.LogHelper
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KFunction
import kotlin.reflect.declaredMemberFunctions
import kotlin.reflect.defaultType

/**
 * Handles command registration for a [FlexPlugin].
 */
class FlexCommandMap(val plugin: FlexPlugin) {

    internal companion object {

        val bukkit_commandMap: CommandMap
        //val bukkit_registerMethod: Method

        init {
            val pluginManager = Bukkit.getPluginManager()
            val commandMap = pluginManager.javaClass.getDeclaredField("commandMap")
            commandMap.isAccessible = true

            bukkit_commandMap = commandMap.get(pluginManager) as CommandMap
            //bukkit_registerMethod = bukkit_commandMap.javaClass.getDeclaredMethod("register", String::class.java, Command::class.java)
        }

        /**
         * Registers a FlexCommand's Bukkit command with Bukkit's plugin manager via reflection.
         */
        private fun registerBukkitCommand(plugin: FlexPlugin, command: FlexCommand) {
            try {
                //bukkit_registerMethod.invoke(bukkit_commandMap, plugin.name.toLowerCase(), command.bukkitCommand)
                bukkit_commandMap.register(plugin.name.toLowerCase(), command.bukkitCommand)
            } catch (ex: Exception) {
                LogHelper.severe(plugin, "An exception occurred while registering command with Bukkit", ex)
            }
        }

    }

    /**
     * Registers an object containing methods annotated with [CommandHandler] that represent
     * command handlers.
     */
    fun register(obj: Any) {
        val commandModule = FlexPlugin.getGlobalModule(CommandModule::class)!!
        outer@ for (f in obj.javaClass.kotlin.declaredMemberFunctions) {
            val meta: CommandHandler = f.annotations.find { it is CommandHandler } as CommandHandler? ?: continue

            // Parameter 0 of the function is the instance of the class (object in this case)

            // 1) Determine if function is player only based on the first parameter.

            // 2) Determine if function is player only based on the first parameter. Otherwise, it
            //    must be a CommandSender.
            val playerOnly: Boolean = when (f.parameters[1].type) {
                Player::class.defaultType -> true
                CommandSender::class.defaultType -> false
                else -> {
                    LogHelper.severe(plugin, "Invalid command handler '${f.name}': first parameter is not a CommandSender or Player")
                    continue@outer
                }
            }

            // 3) Get base command (or create and register it if it doesn't exist)
            val commandPath: Stack<String> = Stack()
            commandPath.addAll(meta.command.split(" ").reversed())

            if (commandPath.peek() == "%") {
                LogHelper.severe(plugin, "Base command can not have reversed arguments")
                continue@outer
            }

            val baseLabel = commandPath.pop()

            var base = commandModule.getBaseCommand(plugin.javaClass.kotlin, baseLabel)
            if (base == null) {
                // Base doesn't exist, create and register it with the command module.
                base = FlexCommand(plugin, baseLabel)
                commandModule.registerCommand(base)
                registerBukkitCommand(plugin, base)
            }

            // 2) Iterate through labels until subcommand is found (creating along the way)
            var subcmd: BasicCommand = base
            var offset = 0
            while (commandPath.isNotEmpty()) {
                val curLabel = commandPath.pop()
                // Check for reverse subcommands
                if (curLabel == "%") {
                    // Find offset
                    while (commandPath.peek() == "%") {
                        ++offset
                        commandPath.pop()
                    }

                    val newLabel = commandPath.pop()

                    // Look for reverse subcommand
                    var temp: BasicCommand? = null
                    for (entry in subcmd.reverseSubcommands) {
                        if (entry.first == offset && entry.second == newLabel) {
                            // Found existing
                            //subcmd = entry.third
                            temp = entry.third
                            break
                        }
                    }

                    if (temp == null) {
                        // Command not found, create it
                        temp = BasicCommand(plugin, newLabel)
                        subcmd.reverseSubcommands.add(Triple(offset, newLabel, BasicCommand(plugin, newLabel)))
                    }

                    subcmd = temp
                    continue
                }

                /* No reverse subcommand, look for normal subcommands */
                offset = 0
                var temp: BasicCommand? = subcmd.subcommands[curLabel]
                if (temp == null) {
                    // Subcommand doesn't exist, create it now
                    temp = BasicCommand(plugin, curLabel)
                    subcmd.registerSubcommand(temp)
                }
                subcmd = temp
            }

            // 3) Set command executor
            //subcmd.setMeta(meta, playerOnly, obj, f as KFunction<Any>)
            subcmd.executors.add(CommandExecutor(meta, playerOnly, subcmd, obj, f as KFunction<Any>, offset))

            // 4) Set default
            if (meta.isDefault) {
                subcmd.parent?.defaultSubcommand = subcmd.label
            }
        }
    }

    fun getCommand(path: String): BasicCommand? {
        val split = ("${plugin.name.toLowerCase()}:$path").split(" ")
        var found: BasicCommand = (bukkit_commandMap.getCommand(split[0]) as? FlexCommand.WrappedBukkitCommand)?.flexCommand
                ?: return null

        for ((index, name) in split.withIndex()) {
            if (index == 0) {
                continue
            }

            found = found.subcommands[name] ?: return null
        }

        return found
    }

}
