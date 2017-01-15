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

import com.stealthyone.mcml2.BukkitItemJsonSerializer
import com.stealthyone.mcml2.McmlParser
import me.st28.flexseries.flexlib.FlexLib
import me.st28.flexseries.flexlib.player.PlayerReference
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import me.st28.flexseries.flexlib.util.translateColorCodes
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.CommandSender
import kotlin.reflect.KClass

class Message(message: String, vararg replacements: Any?) {

    companion object {

        private val parser: McmlParser = McmlParser(BukkitItemJsonSerializer)

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
            val format = FlexPlugin.getGlobalModule(MasterMessageModule::class).objectFormats[type]
                    ?: "Unknown format: '$type'"

            return processedRaw(MessageModule.setupPatternReplace(format), *replacements)
        }

    }

    private val components: Array<out BaseComponent> = parser.parse(message.translateColorCodes(), replacements)

    fun sendTo(player: PlayerReference) {
        val online = player.online
        if (online != null) {
            player.online?.spigot()?.sendMessage(*components)
        }
    }

    fun sendTo(sender: CommandSender) {
        sendTo(arrayListOf(sender))
    }

    fun sendTo(senders: Collection<CommandSender>) {
        senders.forEach { components.send(it) }
    }

}

// Extension method
fun CommandSender.sendMessage(message: Message) {
    message.sendTo(this)
}

fun Array<out BaseComponent>.send(player: PlayerReference) {
    player.online?.spigot()?.sendMessage(*this)
}

fun Array<out BaseComponent>.send(sender: CommandSender) {
    sender.sendMessage(TextComponent(*this).toLegacyText())
}
