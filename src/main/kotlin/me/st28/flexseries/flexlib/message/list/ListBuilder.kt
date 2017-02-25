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
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.util.*

class ListBuilder {

    private val masterModule: MasterMessageModule

    private var pageItems: Int
    private var nextPageCommand: String = ""

    private val messages: MutableList<Message> = ArrayList()

    private var page: Int = 0
    private var pageCount: Int = 0
    private var index: Int = 0

    private var emptyMessage: String = "&c&oNothing here"

    init {
        masterModule = FlexPlugin.getGlobalModule(MasterMessageModule::class)!!
        pageItems = masterModule.listPageItems
    }

    /**
     * Sets the page information for the builder.
     *
     * @param page The page to send. Starts at 1 since this is mostly supplied by user input.
     * @param elemCount The total number of elements.
     */
    fun page(page: Int, elemCount: Int): ListBuilder {
        this.page = page - 1
        this.pageCount = Math.ceil(elemCount / pageItems.toDouble()).toInt()
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

    fun emptyMessage(message: String): ListBuilder {
        this.emptyMessage = message
        return this
    }

    fun header(name: String, vararg replacements: String): ListBuilder {
        if (messages.isNotEmpty()) {
            throw IllegalStateException("Messages have already been added")
        }

        val module = FlexPlugin.getGlobalModule(MasterMessageModule::class)
        val header = module.listHeaderFormats[name]
        val defaultHeader = module.listHeaderFormats["DEFAULT"]

        if (header != null) {
            messages.add(Message.plain(header.getFormatted(page + 1, pageCount, defaultHeader, *replacements)))
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
        val format = FlexPlugin.getGlobalModule(MasterMessageModule::class).listElementFormats[type]
                ?: "Unknown format: '$type'"

        messages.add(Message.processed(String.format(MessageModule.setupPatternReplace(format), *replacements)))
        return this
    }

    /**
     * Retrieves elements from a specified populator.
     */
    fun elements(type: String, populator: (Int) -> Array<String>): ListBuilder {
        val index = page * pageItems
        for (i in 0 until pageItems) {
            try {
                element(type, (index + 1 + i).toString(), *populator(index + i))
            } catch (ex: IndexOutOfBoundsException) {
                // We're done
                break
            }
        }
        return this
    }

    /**
     * Similar to setMessagesString, except with direct Message objects
     */
    fun messages(populator: (Int) -> Message): ListBuilder {
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
    fun messagesString(populator: (Int) -> String): ListBuilder {
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
     * Similar to [messagesString] except with direct [Message] objects.
     */
    fun messages(collection: Collection<Message>): ListBuilder {
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
    fun messagesString(collection: Collection<String>): ListBuilder {
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
        if (messages.size <= 1) {
            message(emptyMessage)
        }
        messages.forEach { it.sendTo(senders) }
    }

}