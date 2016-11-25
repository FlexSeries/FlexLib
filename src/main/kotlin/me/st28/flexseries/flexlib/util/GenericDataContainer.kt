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
package me.st28.flexseries.flexlib.util

import java.util.*

open class GenericDataContainer {

    protected val data: MutableMap<String, Any?> = HashMap()

    fun isSet(name: String): Boolean = data.containsKey(name)

    fun <T> get(name: String, defaultValue: T? = null) : T? = data.get(name) as T? ?: defaultValue

    fun set(name: String, value: Any?) = data.put(name, value)

    fun remove(name: String): Any? = data.remove(name)

}
