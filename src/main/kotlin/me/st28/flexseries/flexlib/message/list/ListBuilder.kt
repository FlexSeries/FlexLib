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
package me.st28.flexseries.flexlib.message.list

import me.st28.flexseries.flexlib.message.MasterMessageModule
import me.st28.flexseries.flexlib.message.Message
import me.st28.flexseries.flexlib.message.MessageModule
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import org.bukkit.command.CommandSender
import java.util.*

class ListBuilder {

    private var pageItems: Int
    private var nextPageCommand: String = ""

    private val messages: MutableList<Message> = ArrayList()

    private var page: Int = 0
    private var pageCount: Int = 0

    init {
        pageItems = FlexPlugin.getGlobalModule(MasterMessageModule::class)!!.listPageItems
    }

    fun page(page: Int, count: Int): ListBuilder {
        this.page = page
        this.pageCount = count
        return this
    }

    fun pageItems(count: Int): ListBuilder {
        this.pageItems = count
        return this
    }

    fun nextPageCommand(command: String): ListBuilder {
        this.nextPageCommand = command
        return this
    }

    fun header(name: String, vararg replacements: String): ListBuilder {
        if (messages.isNotEmpty()) {
            throw IllegalStateException("Messages have already been added")
        }

        val module = FlexPlugin.getGlobalModule(MasterMessageModule::class)!!
        val header = module.listHeaderFormats[name]
        val defaultHeader = module.listHeaderFormats["DEFAULT"]

        if (header != null) {
            messages.add(Message.plain(header.getFormatted(page, pageCount, defaultHeader, *replacements)))
        }
        return this
    }

    fun message(message: String): ListBuilder {
        messages.add(Message.plain(message))
        return this
    }

    fun message(message: Message): ListBuilder {
        messages.add(message)
        return this
    }

    fun element(type: String, vararg replacements: String): ListBuilder {
        val format = FlexPlugin.getGlobalModule(MasterMessageModule::class)!!.listElementFormats[type] ?: "Unknown format: '$type'"
        messages.add(Message.plain(String.format(MessageModule.setupPatternReplace(format), *replacements)))
        return this
    }

    /**
     * Similar to setMessagesString, except with direct Message objects
     */
    fun setMessages(populator: (Int) -> Message): ListBuilder {
        val index = page * pageItems
        for (i in 0 until pageItems) {
            try {
                messages.add(populator.invoke(index + i))
            } catch (ex: IndexOutOfBoundsException) {
                // We're done
                break
            }
        }
        return this
    }

    /**
     * Sets the messages for a specified page with a closure that receives the current index.
     * Should throw an [IndexOutOfBoundsException] upon reaching the final element.
     *
     * @param page The current page, starting at 0.
     */
    fun setMessagesString(populator: (Int) -> String): ListBuilder {
        val index = page * pageItems
        for (i in 0 until pageItems) {
            try {
                messages.add(Message.plain(populator.invoke(index + i)))
            } catch (ex: IndexOutOfBoundsException) {
                // We're done
                break
            }
        }
        return this
    }

    /**
     * Similar to [setMessagesString] except with direct [Message] objects.
     */
    fun setMessages(collection: Collection<Message>): ListBuilder {
        val index = page * pageItems
        for (i in 0 until pageItems) {
            try {
                messages.add(collection.elementAt(index))
            } catch (ex: IndexOutOfBoundsException) {
                // We're done
                break
            }
        }
        return this
    }

    /**
     * Sets the messages for a specified page based on a given collection.
     */
    fun setMessagesString(collection: Collection<String>): ListBuilder {
        val index = page * pageItems
        for (i in 0 until pageItems) {
            try {
                messages.add(Message.plain(collection.elementAt(index)))
            } catch (ex: IndexOutOfBoundsException) {
                // We're done
                break
            }
        }
        return this
    }

    fun sendTo(vararg sender: CommandSender) {
        val senders = sender.asList()
        messages.forEach { it.sendTo(senders) }
    }

}