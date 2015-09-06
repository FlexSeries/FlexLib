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

import java.util.*;

/**
 * String-related utilities.
 */
public final class StringUtils {

    private StringUtils() {}

    public static <T> String collectionToString(Collection<T> collection, String delimiter, StringConverter<T> converter, String defaultValue) {
        String returnString = collectionToString(collection, delimiter, converter);

        return returnString.length() > 0 ? returnString : defaultValue;
    }

    public static <T> String collectionToString(Collection<T> collection, String delimiter, StringConverter<T> converter) {
        if (delimiter == null) {
            delimiter = ", ";
        }

        StringBuilder builder = new StringBuilder();

        for (T object : collection) {
            if (builder.length() != 0) {
                builder.append(delimiter);
            }

            builder.append(converter == null ? object.toString() : converter.toString(object));
        }

        return builder.toString();
    }

    public static <T> String collectionToString(Collection<T> collection, StringConverter<T> converter) {
        return collectionToString(collection, ", ", converter);
    }

    public static String collectionToString(Collection<String> collection, String delimiter) {
        return collectionToString(collection, delimiter, null);
    }

    public static String collectionToString(Collection<String> collection) {
        return collectionToString(collection, ", ", null);
    }

    public static <T> String collectionToSortedString(Collection<T> collection, String delimiter, StringConverter<T> converter, Comparator<? super String> comparator) {
        List<String> values = new ArrayList<>();

        for (T object : collection) {
            values.add(converter == null ? object.toString() : converter.toString(object));
        }

        Collections.sort(values, comparator);

        return collectionToString(values, delimiter);
    }

    public static <T> String collectionToSortedString(Collection<T> collection, String delimiter, StringConverter<T> converter) {
        return collectionToSortedString(collection, delimiter, converter, null);
    }

    public static <T> String collectionToSortedString(Collection<T> collection, StringConverter<T> converter) {
        return collectionToSortedString(collection, ", ", converter, null);
    }

    public static String collectionToSortedString(Collection<String> collection, String delimiter) {
        return collectionToSortedString(collection, delimiter, null, null);
    }

    public static String collectionToSortedString(Collection<String> collection) {
        return collectionToSortedString(collection, ", ", null, null);
    }

}