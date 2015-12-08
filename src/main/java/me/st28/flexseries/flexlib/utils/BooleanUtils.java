/**
 * Copyright 2015 Stealth2800 <http://stealthyone.com/>
 * Copyright 2015 Contributors <https://github.com/FlexSeries>
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
package me.st28.flexseries.flexlib.utils;

import org.apache.commons.lang.Validate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for booleans.
 */
public final class BooleanUtils {

    public final static Set<String> ALIASES_TRUE = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("true", "t", "yes", "y")));
    public final static Set<String> ALIASES_FALSE = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("false", "f", "no", "n")));

    private BooleanUtils() {}

    public static boolean parseBoolean(String input) {
        Validate.notNull(input, "Input cannot be null.");

        input = input.toLowerCase();

        if (ALIASES_TRUE.contains(input)) {
            return true;
        } else if (ALIASES_FALSE.contains(input)) {
            return false;
        }
        throw new IllegalArgumentException("'" + input + "' is not a valid boolean.");
    }

}