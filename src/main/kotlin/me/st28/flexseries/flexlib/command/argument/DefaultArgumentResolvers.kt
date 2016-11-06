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
import me.st28.flexseries.flexlib.logging.LogHelper
import me.st28.flexseries.flexlib.message.Message
import me.st28.flexseries.flexlib.player.PlayerReference
import me.st28.flexseries.flexlib.player.lookup.UnknownPlayerException
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import me.st28.flexseries.flexlib.util.BooleanUtils
import me.st28.flexseries.flexlib.util.UuidUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.concurrent.Callable

internal abstract class NumberResolver<T : Number>(displayName: String) : ArgumentResolver<T>(false) {

    private val displayName: String;

    init {
        this.displayName = displayName
    }

    override fun resolve(context: CommandContext, config: ArgumentConfig, input: String): T? {
        var resolved: T?

        try {
            resolved = handleResolve(context, config, input)
        } catch (ex: NumberFormatException) {
            throw ArgumentResolveException(getMessage("error.input_not"))
        }

        val tooSmall = config.isSet("min") && compare(resolved, config.get("min")!!) < 0
        val tooLarge = config.isSet("max") && compare(resolved, config.get("max")!!) > 0
        val isRange = config.isSet("min") && config.isSet("max")

        if (isRange && (tooSmall || tooLarge)) {
            throw ArgumentResolveException(getMessage("error.input_outside_range", config.get("min"), config.get("max")))
        } else if (tooLarge) {
            throw ArgumentResolveException(getMessage("error.input_too_large", config.get("max")))
        } else if (tooSmall) {
            throw ArgumentResolveException(getMessage("error.input_too_small", config.get("min")))
        }

        return resolved
    }

    protected abstract fun handleResolve(context: CommandContext, config: ArgumentConfig, input: String): T

    protected abstract fun compare(o1: T, o2: T): Int

    private fun getMessage(message: String, vararg replacements: Any?): Message {
        return Message.getGlobal("${message}_$displayName", replacements)
    }

    override fun getTabOptions(context: CommandContext, config: ArgumentConfig, input: String): List<String>? {
        return null
    }

}

internal object IntegerResolver : NumberResolver<Int>("integer") {

    override fun handleResolve(context: CommandContext, config: ArgumentConfig, input: String): Int {
        return input.toInt()
    }

    override fun compare(o1: Int, o2: Int): Int {
        return o1.compareTo(o2)
    }

}

internal object FloatResolver : NumberResolver<Float>("decimal") {

    override fun handleResolve(context: CommandContext, config: ArgumentConfig, input: String): Float {
        return input.toFloat()
    }

    override fun compare(o1: Float, o2: Float): Int {
        return o1.compareTo(o2)
    }

}

internal object DoubleResolver : NumberResolver<Double>("decimal") {

    override fun handleResolve(context: CommandContext, config: ArgumentConfig, input: String): Double {
        return input.toDouble()
    }

    override fun compare(o1: Double, o2: Double): Int {
        return o1.compareTo(o2)
    }

}

internal object BooleanResolver : ArgumentResolver<Boolean>(false) {

    private val tabOptions: List<String> = arrayListOf("true", "false")

    override fun resolve(context: CommandContext, config: ArgumentConfig, input: String): Boolean? {
        try {
            return BooleanUtils.fromString(input)
        } catch (ex: IllegalArgumentException) {
            throw ArgumentResolveException("error.input_not_boolean")
        }
    }

    override fun getTabOptions(context: CommandContext, config: ArgumentConfig, input: String): List<String>? {
        return tabOptions
    }

}

internal object StringResolver : ArgumentResolver<String>(false) {

    override fun resolve(context: CommandContext, config: ArgumentConfig, input: String): String? {
        val minLength = config.get("min", -1)!!
        val maxLength = config.get("max", -1)!!
        val tooShort = minLength > 0 && input.length < minLength
        val tooLong = maxLength > 0 && input.length > maxLength
        val isRange = minLength > 0 && maxLength > 0

        if (isRange && (tooShort || tooLong)) {
            throw ArgumentResolveException("error.string_outside_range", minLength, maxLength)
        } else if (tooLong) {
            throw ArgumentResolveException("error.string_too_long", maxLength)
        } else if (tooShort) {
            throw ArgumentResolveException("error.string_too_short", minLength)
        }
        return input
    }

    override fun getTabOptions(context: CommandContext, config: ArgumentConfig, input: String): List<String>? {
        return null
    }

}

internal object PlayerResolver : ArgumentResolver<PlayerReference>(true) {

    override fun resolve(context: CommandContext, config: ArgumentConfig, input: String): PlayerReference? {
        // NO ASYNC/BLOCKING CALLS IN THIS METHOD
        var player: Player? = null

        // 1) Check if input is UUID
        try {
            player = Bukkit.getPlayer(UuidUtils.fromString(input))
        } catch (ex: IllegalArgumentException) { }

        // 2) Check if input is name
        if (player == null) {
            player = Bukkit.getPlayerExact(input)
        }

        // 3) Perform checks
        if (player != null) {
            val ref = PlayerReference(player)
            performChecks(context, config, ref)
            return ref
        }

        return null
    }

    override fun resolveAsync(context: CommandContext, config: ArgumentConfig, input: String): PlayerReference? {
        var ref: PlayerReference? = null

        // 1) Check if input is UUID (this method will potentially perform a lookup)
        try {
            ref = PlayerReference(UuidUtils.fromString(input))
        } catch (ex: IllegalArgumentException) { }

        // 2) Check if input is name (this method will potentially perform a lookup)
        if (ref == null) {
            try {
                ref = PlayerReference(input)
            } catch (ex: UnknownPlayerException) {
                throw ArgumentResolveException("error.player_not_found", input)
            }
        }

        // 3) Perform checks
        try {
            Bukkit.getScheduler().callSyncMethod(context.command.plugin, Callable { performChecks(context, config, ref!!) })
        } catch (ex: Exception) {
            if (ex.cause != null && ex.cause is ArgumentResolveException) {
                throw ex.cause as ArgumentResolveException
            }

            LogHelper.severe(context.command.plugin, "An exception occurred while resolving argument", ex)
            throw ArgumentResolveException("error.internal_error")
        }

        return ref
    }

    private fun performChecks(context: CommandContext, config: ArgumentConfig, player: PlayerReference) {
        if (!player.isOnline() && config.isSet("online")) {
            throw ArgumentResolveException("error.player_not_online", player.name)
        }

        if (player.isOnline() && config.isSet("notSender") && context.sender == player.online) {
            throw ArgumentResolveException("error.player_cannot_be_sender")
        }
    }

    override fun getTabOptions(context: CommandContext, config: ArgumentConfig, input: String): List<String>? {
        return null
    }

}

/*internal object SessionResolver : AutoArgumentResolver<CommandSession>(false) {

    override fun getDefault(context: CommandContext, config: ArgumentConfig): CommandSession? {
        val module = FlexPlugin.getGlobalModule(CommandModule::class)

        val id: String = config.get("id") ?: throw ArgumentResolveException("error.session_id_not_set")

        val create = config.isSet("create")

        val session = module!!.getSession(context.command.plugin.javaClass.kotlin, context.sender!!, id, create)
        if (session != null && !config.isSet("optional")) {
            throw ArgumentResolveException("error.session_does_not_exist")
        }
        return session
    }

}*/
