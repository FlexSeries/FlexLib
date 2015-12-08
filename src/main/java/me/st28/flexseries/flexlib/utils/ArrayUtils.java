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