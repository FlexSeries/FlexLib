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
import me.st28.flexseries.flexlib.message.sendMessage
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import me.st28.flexseries.flexlib.util.SchedulerUtils
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

internal class CommandSession {

    private val plugin: FlexPlugin

    /* Carry-over session settings */
    val label: String
    val args: Array<String>
    var offset: Int = 0

    private val playerUuid: UUID?
    private val sender: CommandSender?

    var running: Boolean = true

    val params: MutableList<Any?> = ArrayList()

    constructor(plugin: FlexPlugin, context: CommandContext) {
        this.plugin = plugin

        this.label = context.label
        this.args = context.args

        if (context.sender is Player) {
            playerUuid = context.sender.uniqueId
            this.sender = null
        } else {
            playerUuid = null
            this.sender = context.sender
        }
    }

    fun getSender(): CommandSender? {
        if (playerUuid != null) {
            return Bukkit.getPlayer(playerUuid)
        } else {
            return sender
        }
    }

    fun cancelWith(message: Message) {
        running = false
        SchedulerUtils.runSync(plugin) {
            sender?.sendMessage(message)
        }
    }

    /**
     * @return Null if the sender is offline or this session has been canceled.
     */
    fun toContext(): CommandContext? {
        val sender = getSender()
        if (!running || sender == null) {
            return null
        }
        return CommandContext(sender, label, args, offset)
    }

}