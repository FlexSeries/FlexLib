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
package me.st28.flexseries.flexlib.message

import com.stealthyone.mcb.mcml.MCMLBuilder
import me.st28.flexseries.flexlib.FlexLib
import me.st28.flexseries.flexlib.player.PlayerReference
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import org.bukkit.command.CommandSender
import kotlin.reflect.KClass

class Message {

    companion object {

        fun get(plugin: KClass<out FlexPlugin>, name: String, vararg replacements: Any?): Message {
            return FlexPlugin.getPluginModuleSafe(plugin, MessageModule::class)?.getMessage(name, *replacements)
                ?: Message(name, replacements)
        }

        fun getGlobal(name: String, vararg replacements: Any?): Message {
            return get(FlexLib::class, name, *replacements)
        }

        fun plain(message: String): Message {
            return Message(message)
        }

        fun processed(message: String, vararg replacements: String): Message {
            return Message(processedRaw(message, replacements))
        }

        fun processedRaw(message: String, vararg replacements: Any?): String {
            return FlexPlugin.getGlobalModule(MasterMessageModule::class).processMessage(message).format(*replacements)
        }

        fun processedObjectRaw(type: String, vararg replacements: Any?): String {
            val format = FlexPlugin.getGlobalModule(MasterMessageModule::class).listElementFormats[type] ?: "Unknown format: '$type'"

            return processedRaw(MessageModule.setupPatternReplace(format), *replacements)
        }

    }

    private val message: String
    private val replacements: Array<out Any?>

    constructor(message: String, vararg replacements: Any? = emptyArray()) {
        this.message = message
        this.replacements = replacements
    }

    fun getProcessedMessage(): String = String.format(message, replacements)

    fun sendTo(player: PlayerReference, vararg replacements: Any?) {
        val online = player.online
        if (online != null) {
            sendTo(online, *replacements)
        }
    }

    fun sendTo(sender: CommandSender, vararg replacements: Any?) {
        sendTo(arrayListOf(sender), *replacements)
    }

    fun sendTo(senders: Collection<CommandSender>, vararg replacements: Any?) {
        val replacementCount = this.replacements.size + replacements.size
        if (replacementCount == 0) {
            MCMLBuilder(message).toFancyMessage().send(senders)
            return
        }

        MCMLBuilder(message.format(*this.replacements)).toFancyMessage().send(senders)
    }

}

// Extension method
fun CommandSender.sendMessage(message: Message) {
    message.sendTo(this)
}
