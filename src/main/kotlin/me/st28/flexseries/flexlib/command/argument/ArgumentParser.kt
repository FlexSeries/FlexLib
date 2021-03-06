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
package me.st28.flexseries.flexlib.command.argument

import me.st28.flexseries.flexlib.command.CommandContext
import me.st28.flexseries.flexlib.command.CommandModule
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import me.st28.flexseries.flexlib.util.UuidUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.util.*
import kotlin.reflect.KClass

/**
 * Notes
 * - Parsers that consume more than one argument can operate with less than the number of consumed
 *   arguments assuming [defaultMinArgs] is greater than zero.
 *
 * @param consumed The number of raw arguments consumed by this parser. Default = 1
 * @param defaultMinArgs The default number of minimum arguments this parser consumes (for parsers
 *                       that consume more than one argument). Default = 0 (required = consumed)
 * @param async True if this parser implements [parseAsync].
 */
abstract class ArgumentParser<out T : Any>(
        val consumed: Int = 1,
        val defaultMinArgs: Int = 0,
        val async: Boolean = false,
        val defaultLabels: Array<String>? = null)
{

   companion object {

       /**
        * Helper method for registering an argument parser.
        *
        * @see CommandModule.registerArgumentParser
        */
       fun <T: Any> register(type: KClass<T>, parser: ArgumentParser<T>): Boolean {
           return FlexPlugin.getGlobalModule(CommandModule::class).registerArgumentParser(type, parser)
       }

   }

    /**
     * Attempts to parse the raw input into an argument of the implementation's type parameter.
     *
     * @param context The [CommandContext] in which the command is being executed.
     * @param raw The raw input supplied by the sender. Contains [consumed] element(s).
     * @return A value based on the given input.
     *         Null if data required for the argument wasn't found. This should only happen if the
     *         implementation supports async parsing.
     * @throws ArgumentParseException Should be thrown when the argument failed to be parsed due to
     *         invalid or illegal input.
     */
    abstract fun parse(context: CommandContext, config: ArgumentConfig, raw: Array<String>): T?

    /**
     * Attempts to parse the raw input into an argument of the implementation's type parameter.
     * This method is called synchronously and is free to do database and other blocking calls.
     *
     * @param context The [CommandContext] in which the command is being executed.
     * @param raw The raw input supplied by the sender. Contains [consumed] element(s).
     * @return A value based on the given input.
     * @throws ArgumentParseException Should be thrown when the argument failed to be parsed due to
     *         invalid or illegal input.
     */
    open fun parseAsync(context: CommandContext, config: ArgumentConfig, raw: Array<String>): T {
        throw UnsupportedOperationException("Parser does not support async parsing")
    }

    /**
     * Retrieves tab options for this argument based on a given context and input.
     *
     * @param max The max number of tab completions this method should return.
     * @return Null if there are no tab completions.
     */
    open fun getTabOptions(context: CommandContext, config: ArgumentConfig, input: String, max: Int): List<String>? {
        val names: MutableList<String> = ArrayList()
        for ((index, p) in Bukkit.getOnlinePlayers().withIndex()) {
            if (index > max) {
                break
            }
            names.add(p.name)
        }
        return names
    }

}

object StringParser : ArgumentParser<String>() {

    override fun parse(context: CommandContext, config: ArgumentConfig, raw: Array<String>): String? {
        return raw[0]
    }

}

object IntParser : ArgumentParser<Int>() {

    override fun parse(context: CommandContext, config: ArgumentConfig, raw: Array<String>): Int? {
        return try {
            raw[0].toInt()
        } catch (ex: NumberFormatException) {
            throw ArgumentParseException("error.input_not_integer")
        }
    }

}

object DoubleParser : ArgumentParser<Double>() {

    override fun parse(context: CommandContext, config: ArgumentConfig, raw: Array<String>): Double? {
        return try {
            raw[0].toDouble()
        } catch (ex: NumberFormatException) {
            throw ArgumentParseException("error.input_not_number")
        }
    }

}

object BigDecimalParser : ArgumentParser<BigDecimal>() {

    override fun parse(context: CommandContext, config: ArgumentConfig, raw: Array<String>): BigDecimal? {
        val numerical = try {
            raw[0].toDouble()
        } catch (ex: NumberFormatException) {
            throw ArgumentParseException("error.input_not_number")
        }
        return BigDecimal.valueOf(numerical)
    }

}

object PlayerParser : ArgumentParser<Player>() {

    override fun parse(context: CommandContext, config: ArgumentConfig, raw: Array<String>): Player? {
        if (raw[0] == "" || raw[0] == "{sender}") {
            if (context.sender !is Player) {
                throw ArgumentParseException("error.must_be_player")
            }
            return context.sender
        }

        // 1) Try UUID
        try {
            return Bukkit.getPlayer(UuidUtils.fromString(raw[0]))
        } catch (ex: IllegalArgumentException) {
            // Not a UUID
        }

        // 2) Try name
        return Bukkit.getPlayerExact(raw[0])
            ?: throw ArgumentParseException("error.player_not_found", raw[0])
    }

}
