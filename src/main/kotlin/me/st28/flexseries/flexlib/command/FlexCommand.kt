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

import me.st28.flexseries.flexlib.message.Message
import me.st28.flexseries.flexlib.message.list.ListBuilder
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

/**
 * A [BasicCommand] that is registered and called by the Bukkit command API.
 * This class is used exclusively for base commands.
 *
 * @param plugin The plugin this command is registered under.
 * @param label The primary label for this command.
 */
class FlexCommand(plugin: FlexPlugin, label: String) : BasicCommand(plugin, label) {

    /**
     * The Bukkit wrapper that executes this command.
     */
    internal val bukkitCommand: Command

    init {
        // Create the Bukkit command that executes this FlexCommand
        bukkitCommand = WrappedBukkitCommand()
    }

    override fun setAliases(aliases: List<String>) {
        super.setAliases(aliases)

        bukkitCommand.aliases.clear()
        bukkitCommand.aliases.addAll(aliases)

        // Re-register with Bukkit
        FlexCommandMap.bukkit_commandMap.register(plugin.name.toLowerCase(), bukkitCommand)
    }

    internal inner class WrappedBukkitCommand : Command(label, "(description)", "(usage)", aliases) {

        val flexCommand: FlexCommand = this@FlexCommand

        override fun execute(sender: CommandSender, label: String, args: Array<String>): Boolean {
            val ret = this@FlexCommand.execute(CommandContext(sender, label, args, 0), 0) ?: return true

            if (ret is Message) {
                ret.sendTo(sender)
            } else if (ret is String) {
                sender.sendMessage(ret)
            } else if (ret is ListBuilder) {
                ret.sendTo(sender)
            }
            return true
        }

    }

}
