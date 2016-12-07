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

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandHandler (

    /**
     * The path for executing the command (base label and subcommand labels).
     */
    val command: String,

    /**
     * The aliases for the command.
     */
    val aliases: Array<String> = arrayOf(),

    /**
     * The description of the command.
     */
    val description: String = "",

    /**
     * The permission required to run this command.
     */
    val permission: String = "",

    /**
     * True indicates that the command handler is the default for the parent.
     */
    val isDefault: Boolean = false,

    /**
     * True indicates that the command handler is a placeholder and contains no logic.
     */
    val dummy: Boolean = false

)
