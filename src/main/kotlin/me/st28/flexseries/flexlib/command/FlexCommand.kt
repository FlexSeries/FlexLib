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
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import kotlin.reflect.KFunction

/**
 * Represents a FlexLib command that is registered with Bukkit.
 * This class is used exclusively for base commands.
 *
 * @param plugin The plugin this command is registered under.
 * @param label The primary label for this command.
 */
class FlexCommand(plugin: FlexPlugin, label: String) : BasicCommand(plugin, label) {

    /**
     * The Bukkit wrapper for executing this command.
     */
    internal val bukkitCommand: Command

    init {
        // Create the Bukkit command that executes this FlexCommand
        bukkitCommand = object: Command(label, "(description)", "(usage)", aliases) {
            override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
                this@FlexCommand.execute(sender, commandLabel, args, 0)
                return true
            }
        }
    }

    override fun getUsage(context: CommandContext): String {
        val sb = StringBuilder()
        sb.append("/").append(context.label)
        for (ac in argumentConfig) {
            sb.append(" ").append(ac.getUsage(context))
        }
        return sb.toString()
    }

    override fun setMeta(meta: CommandHandler, obj: Any, handler: KFunction<Unit>) {
        super.setMeta(meta, obj, handler)

        bukkitCommand.description = if (meta.description.isEmpty()) "(no description set)" else meta.description
        // TODO: Update aliases with Bukkit

        // Default usage message
        if (argumentConfig.isNotEmpty()) {
            val sb = StringBuilder()
            sb.append("/").append(label)
            for (ac in argumentConfig) {
                sb.append(" ").append(ac.getUsage(null))
            }
            bukkitCommand.usage = sb.toString()
        }
    }

}