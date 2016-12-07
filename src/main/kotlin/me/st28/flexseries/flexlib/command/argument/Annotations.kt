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

/**
 * Sets the default input value for an argument.
 * If an empty string, will default to the argument type's default.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Default(
        /**
         * The raw value that will be parsed as the default input.
         */
        val value: String = "",

        /**
         * The minimum user arguments required for the argument.
         * This is used for parsers that can provide default output based on less than the required
         * number of given arguments.
         */
        val minArgs: Int = -1
)

/**
 * Provides additional information for parameters that use [ArgumentParser]s that consume more than
 * one raw argument from the user.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MultiArg(
        /**
         * The labels for arguments two or more. Argument parsers provide default values for this.
         */
        val labels: Array<String> = arrayOf()
)
