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

import me.st28.flexseries.flexlib.FlexLib
import me.st28.flexseries.flexlib.command.argument.ArgumentParser
import me.st28.flexseries.flexlib.command.argument.PlayerParser
import me.st28.flexseries.flexlib.plugin.FlexModule
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.defaultType

/***
 * The main command manager for the FlexLib command framework.
 */
class CommandModule(plugin: FlexLib) : FlexModule<FlexLib>(plugin, "commands", "Manages the FlexLib command framework") {

    internal val commands: MutableMap<KClass<out FlexPlugin>, MutableMap<String, FlexCommand>> = HashMap()

    //internal val argumentParsers: MutableMap<KClass<out Any>, ArgumentParser<Any>> = HashMap()
    //internal val argumentParsers: MutableMap<KType, ArgumentParser<Any>> = HashMap()
    internal val argumentParsers: MutableMap<String, ArgumentParser<Any>> = HashMap()

    override fun handleEnable() {
        registerArgumentParser(Player::class, PlayerParser)
    }

    fun getBaseCommand(plugin: KClass<out FlexPlugin>, command: String): FlexCommand? {
        if (!commands.containsKey(plugin)) {
            return null
        }
        return commands[plugin]!![command]
    }

    fun registerCommand(command: FlexCommand) {
        val plugin = command.plugin.javaClass.kotlin
        if (!commands.containsKey(plugin)) {
            commands.put(plugin, HashMap())
        }

        commands[plugin]!!.put(command.label.toLowerCase(), command)
    }

    /**
     * Registers an [ArgumentParser].
     *
     * @param type The class the parser handles.
     * @param parser The parser for the specified class type.
     * @return True if the parser was successfully registered.
     *         False if there already is a parser registered for the specified type.
     */
    fun <T: Any> registerArgumentParser(type: KClass<T>, parser: ArgumentParser<T>): Boolean {
        val key = type.defaultType.toString()
        if (argumentParsers.containsKey(key)) {
            return false
        }

        argumentParsers.put(key, parser)
        return true
    }

    fun getArgumentParser(type: KType): ArgumentParser<Any>? {
        return argumentParsers[type.toString().replace("?", "")]
        //return argumentParsers[type]
    }

}