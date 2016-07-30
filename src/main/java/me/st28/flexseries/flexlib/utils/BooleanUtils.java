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
package me.st28.flexseries.flexlib.utils;

import org.apache.commons.lang.Validate;

import java.util.Arrays;
import java.util.List;

public final class BooleanUtils {

    private static final List<String> TRUE_VALUES = Arrays.asList("true", "t", "yes", "y");
    private static final List<String> FALSE_VALUES = Arrays.asList("false", "f", "no", "n");

    private BooleanUtils() { }

    public static String[] getTrueValues() {
        return TRUE_VALUES.toArray(new String[TRUE_VALUES.size()]);
    }

    public static String[] getFalseValues() {
        return FALSE_VALUES.toArray(new String[FALSE_VALUES.size()]);
    }

    public static boolean fromString(String input) {
        Validate.notNull(input, "Input cannot be null");
        if (TRUE_VALUES.contains(input.toLowerCase())) {
            return true;
        } else if (FALSE_VALUES.contains(input.toLowerCase())) {
            return false;
        }
        throw new IllegalArgumentException("Invalid boolean value '" + input + "'");
    }

}