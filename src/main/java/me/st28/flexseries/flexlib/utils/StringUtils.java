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