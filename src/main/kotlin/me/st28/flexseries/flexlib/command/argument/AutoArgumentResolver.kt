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

/**
 * An {@link ArgumentResolver} used only for auto arguments. The only method necessary to implement
 * is {@link #getDefault(CommandContext, ArgumentConfig)}.
 *
 * If the argument resolver should be able to be used as a normal argument resolver, then it should
 * be implemented as an {@link ArgumentResolver} instead of an AutoArgumentResolver.
 */
abstract class AutoArgumentResolver<T: Any?>(isAsync: Boolean) : ArgumentResolver<T>(isAsync) {

    override fun resolve(context: CommandContext, config: ArgumentConfig, input: String): T? {
        throw UnsupportedOperationException()
    }

    override fun getTabOptions(context: CommandContext, config: ArgumentConfig, input: String): List<String>? {
        throw UnsupportedOperationException()
    }

    abstract override fun getDefault(context: CommandContext, config: ArgumentConfig): T?

}