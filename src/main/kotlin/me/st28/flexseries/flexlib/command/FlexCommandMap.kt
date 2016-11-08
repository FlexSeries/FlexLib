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
import org.bukkit.command.Command
import org.bukkit.command.CommandMap
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.declaredFunctions
import kotlin.reflect.declaredMemberFunctions

/**
 * Holds a {@link FlexPlugin}'s command mappings.
 */
class FlexCommandMap {

    private companion object {

        var bukkit_commandMap: CommandMap? = null
        var bukkit_registerMethod: Method? = null

        /**
         * Registers a FlexCommand's Bukkit command with Bukkit's plugin manager via reflection.
         */
        private fun registerBukkitCommand(plugin: FlexPlugin, command: FlexCommand) {
            val pluginManager = Bukkit.getPluginManager()

            try {
                if (bukkit_registerMethod == null) {
                    val commandMap = pluginManager.javaClass.getDeclaredField("commandMap")
                    commandMap.isAccessible = true

                    bukkit_commandMap = commandMap.get(pluginManager) as CommandMap
                    bukkit_registerMethod = bukkit_commandMap!!.javaClass.getDeclaredMethod("register", String::class.java, Command::class.java)
                }

                bukkit_registerMethod!!.invoke(bukkit_commandMap!!, plugin.name, command.bukkitCommand)
            } catch (ex: Exception) {
                LogHelper.severe(plugin, "An exception occurred while registering command with Bukkit", ex)
            }
        }

    }

    val plugin: FlexPlugin

    constructor(plugin: FlexPlugin) {
        this.plugin = plugin
    }

    /**
     * Registers an object containing methods annotated with {@link CommandHandler} that represent
     * command handlers.
     */
    fun register(obj: Any) {
        val commandModule = FlexPlugin.getGlobalModule(CommandModule::class)!!
        //obj.javaClass.kotlin.declaredFunctions
        for (f in obj.javaClass.kotlin.declaredMemberFunctions) {
            val meta: CommandHandler = f.annotations.find { it is CommandHandler } as CommandHandler? ?: continue

            val commandPath = meta.command.split(" ")

            // 1) Get base command (or create and register it if it doesn't exist)
            var base = commandModule.getCommand(plugin.javaClass.kotlin, commandPath[0]) as FlexCommand?
            if (base == null) {
                // Base doesn't exist, create and register it with the command module.
                base = FlexCommand(plugin, commandPath[0])
                commandModule.registerCommand(plugin.javaClass.kotlin, base)
                registerBukkitCommand(plugin, base)
            }

            // 2) Iterate through labels until subcommand is found (creating along the way)
            var subcmd: BasicCommand = base
            for (i in 1 until commandPath.size) {
                val curLabel = commandPath[i].toLowerCase()

                var temp = subcmd.subcommands[curLabel]
                if (temp == null) {
                    // Subcommand doesn't exist, create and register under the parent
                    temp = BasicCommand(plugin, curLabel)
                    subcmd.registerSubcommand(temp)
                }
                subcmd = temp
            }

            // 3) Update final command's executor and meta
            subcmd.setMeta(meta, obj, f as KFunction<Unit>)

            // Set default
            if (meta.defaultSubcommand) {
                if (subcmd.parent == null) {
                    return
                }

                if (subcmd.parent!!.defaultSubcommand.isNotEmpty()) {
                    LogHelper.warning(plugin, "Multiple default subcommands are defined for command '${subcmd.parent!!.label}'")
                }
                subcmd.parent!!.defaultSubcommand = subcmd.label
            }
        }
    }

}