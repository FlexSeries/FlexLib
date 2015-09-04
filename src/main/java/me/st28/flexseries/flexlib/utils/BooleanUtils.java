/**
 * FlexLib - Licensed under the MIT License (MIT)
 *
 * Copyright (c) Stealth2800 <http://stealthyone.com/>
 * Copyright (c) contributors <https://github.com/FlexSeries>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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