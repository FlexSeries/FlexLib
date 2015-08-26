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
import java.util.List;

/**
 * Utilities for arrays.
 */
public final class ArrayUtils {

    private ArrayUtils() {}

    /**
     * Convenience method for sublisting a string array into another string array object.
     *
     * @param array The original array.
     * @param startIndex The beginning index to include.
     * @param endIndex The ending index to include.
     * @return The new string array.
     */
    public static String[] stringArraySublist(String[] array, int startIndex, int endIndex) {
        Validate.notNull(array);

        List<String> tempList = Arrays.asList(array).subList(startIndex, endIndex);
        return tempList.toArray(new String[tempList.size()]);
    }

    /**
     * Converts a string array into a string, with each value separated by a space.
     * @see #stringArrayToString(String[], String)
     */
    public static String stringArrayToString(String[] array) {
        return stringArrayToString(array, " ");
    }

    /**
     * Converts a string array into a string, with a configurable delimiter.
     *
     * @param delimiter What to use to separate each value of the string array.
     */
    public static String stringArrayToString(String[] array, String delimiter) {
        Validate.notNull(array);
        Validate.notNull(delimiter);

        StringBuilder sb = new StringBuilder();
        for (String str : array) {
            if (sb.length() > 0) {
                sb.append(delimiter);
            }
            sb.append(str);
        }
        return sb.toString();
    }

}