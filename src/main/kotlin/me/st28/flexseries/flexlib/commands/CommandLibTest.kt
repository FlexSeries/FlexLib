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
package me.st28.flexseries.flexlib.commands

import me.st28.flexseries.flexlib.command.CommandContext
import me.st28.flexseries.flexlib.command.CommandHandler
import org.bukkit.command.CommandSender

object CommandLibTest {

    @CommandHandler(
        "hello"
    )
    fun hello(sender: CommandSender, context: CommandContext) {
        sender.sendMessage("Hello World!")
    }

    @CommandHandler(
        "hello3 test"
    )
    fun hello3_test(sender: CommandSender, context: CommandContext) {
        sender.sendMessage("Hello3 test")
    }

    @CommandHandler(
        "hello3 test2",
        aliases = arrayOf("test3")
    )
    fun hello3_test2(sender: CommandSender, context: CommandContext) {
        sender.sendMessage("Hello3 ${context.label}")
    }

    @CommandHandler(
        "hello3 def",
        defaultSubcommand = true
    )
    fun hello3_def(sender: CommandSender, context: CommandContext) {
        sender.sendMessage("default subcommand")
    }

}
