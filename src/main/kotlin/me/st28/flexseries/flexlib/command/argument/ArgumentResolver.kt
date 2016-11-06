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

import me.st28.flexseries.flexlib.FlexLib
import me.st28.flexseries.flexlib.command.CommandContext
import me.st28.flexseries.flexlib.logging.LogHelper
import me.st28.flexseries.flexlib.plugin.FlexPlugin
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

abstract class ArgumentResolver<T : Any?>(isAsync: Boolean) {

    companion object {

        internal val resolvers: MutableMap<String, ArgumentResolver<*>> = HashMap()

        fun register(plugin: FlexPlugin?, identifier: String, resolver: ArgumentResolver<*>): Boolean {
            val fullIdentifier = (if (plugin == null) "" else (plugin.name.toLowerCase() + "::")) + identifier

            if (resolvers.containsKey(fullIdentifier)) {
                LogHelper.warning(JavaPlugin.getPlugin(FlexLib::class.java), "Argument resolver '$fullIdentifier' is already registered")
                return false
            }

            resolvers.put(fullIdentifier, resolver)
            return true
        }

        fun getResolver(identifier: String): ArgumentResolver<*>? = resolvers[identifier]

    }

    val isAsync: Boolean

    init {
        this.isAsync = isAsync
    }

    abstract fun resolve(context: CommandContext, config: ArgumentConfig, input: String): T?

    open fun resolveAsync(context: CommandContext, config: ArgumentConfig, input: String): T? {
        throw UnsupportedOperationException()
    }

    abstract fun getTabOptions(context: CommandContext, config: ArgumentConfig, input: String): List<String>?

    open fun getDefault(context: CommandContext, config: ArgumentConfig): T? {
        throw UnsupportedOperationException("Argument does not have a default value")
    }

    open fun getDefaultAsync(context: CommandContext, config: ArgumentConfig): T? {
        throw UnsupportedOperationException("Argument does not have a default value")
    }

    open fun getPermissionString(arg: T) = arg.toString()

}
