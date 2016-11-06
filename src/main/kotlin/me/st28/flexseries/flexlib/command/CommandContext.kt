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

import me.st28.flexseries.flexlib.player.PlayerReference
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass

class CommandContext {

    val command: BasicCommand
    private val sender: CommandSender?
    val player: PlayerReference?
    val labels: MutableList<String> = ArrayList()
    val rawArgs: Array<String>
    val curArgs: Array<String>
    val level: Int

    private val arguments: MutableMap<String, Any?> = HashMap()

    constructor(command: BasicCommand, sender: CommandSender, label: String, args: Array<String>, level: Int) {
        this.command = command
        if (sender is Player) {
            this.sender = null
            this.player = PlayerReference(sender)
        } else {
            this.sender = sender
            this.player = null
        }

        labels.add(label)
        this.rawArgs = args
        this.curArgs = if (args.isEmpty()) emptyArray<String>() else rawArgs.toList().subList(level, rawArgs.size).toTypedArray()
        this.level = level
    }

    fun getLabel(): String {
        return labels[0]
    }

    fun getCurrentLabel(): String {
        return labels[level]
    }

    fun getSender(): CommandSender? {
        return if (player != null) player.getPlayer() else sender
    }

    fun <T: Any> getArgument(name: String): T? {
        return arguments.get(name) as T
    }

    fun <T : Any> getArgument(type: KClass<T>) : T? {
        for (v in arguments.values) {
            if (v == null) {
                continue
            }

            if (v.javaClass.kotlin == type) {
                return v as T
            }
        }
        return null
    }

    fun setArgument(name: String, value: Any?) = arguments.put(name, value)

    fun getSession(name: String? = null): CommandSession? {
        when {
            name != null -> return getArgument(name)
            else -> return getArgument(CommandSession::class)
        }
    }

}