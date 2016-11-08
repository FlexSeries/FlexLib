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
import java.util.regex.Matcher
import java.util.regex.Pattern

object UuidUtils {

    private val UUID_FIXER_PATTERN: Pattern = Pattern.compile("(\\w{8})-?(\\w{4})-?(\\w{4})-?(\\w{4})-?(\\w{12})")

    fun fromString(raw: String): UUID {
        val matcher: Matcher = UUID_FIXER_PATTERN.matcher(raw)
        if (!matcher.matches()) {
            throw IllegalArgumentException("Invalid UUID format '$raw'")
        }
        return UUID.fromString(matcher.replaceAll("$1-$2-$3-$4-$5"))
    }

}