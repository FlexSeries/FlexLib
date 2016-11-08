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
import me.st28.flexseries.flexlib.command.argument.*
import me.st28.flexseries.flexlib.plugin.FlexModule
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import java.util.*
import kotlin.reflect.KClass

/**
 * The main command manager for the FlexLib command framework.
 */
class CommandModule(plugin: FlexLib) : FlexModule<FlexLib>(plugin, "commands", "Manages the FlexLib command framework") {

    val commands: MutableMap<KClass<out FlexPlugin>, MutableMap<String, FlexCommand>> = HashMap()

    //val sessions: MutableMap<String, MutableMap<String, CommandSession>> = HashMap()

    override fun handleEnable() {
        ArgumentResolver.register(null, "boolean", BooleanResolver)
        ArgumentResolver.register(null, "integer", IntegerResolver)
        //ArgumentResolver.register(null, "long", LongResolver)
        ArgumentResolver.register(null, "float", FloatResolver)
        ArgumentResolver.register(null, "double", DoubleResolver)
        ArgumentResolver.register(null, "player", PlayerResolver)
        ArgumentResolver.register(null, "string", StringResolver)
        //ArgumentResolver.register(null, "session", SessionResolver)
        ArgumentResolver.register(null, "flexplugin", FlexPluginResolver)
        ArgumentResolver.register(null, "flexmodule", FlexModuleResolver)
    }

    fun getCommand(plugin: KClass<out FlexPlugin>, command: String): BasicCommand? {
        if (!commands.containsKey(plugin)) {
            return null
        }

        val path = command.split(" ")

        var found: BasicCommand? = commands[plugin]!![path[0].toLowerCase()]
        for (i in 1 until path.size) {
            found = found!!.subcommands[path[i].toLowerCase()]
            if (found == null) {
                break
            }
        }
        return found
    }

    fun registerCommand(plugin: KClass<out FlexPlugin>, command: FlexCommand) {
        if (!commands.containsKey(plugin)) {
            commands.put(plugin, HashMap())
        }

        commands[plugin]!!.put(command.label.toLowerCase(), command)
    }

    /*fun getSession(plugin: KClass<out FlexPlugin>, sender: CommandSender, id: String, create: Boolean): CommandSession? {
        val key = "${plugin.java.canonicalName}#$id"
        val subKey = "${sender.javaClass.canonicalName}#${sender.name}"

        if (!sessions.containsKey(key)) {
            sessions.put(key, HashMap())
        }

        val subMap = sessions[key]!!
        if (!subMap.containsKey(subKey) && create) {
            subMap.put(subKey, CommandSession())
        }
        return subMap[subKey]
    }*/

}