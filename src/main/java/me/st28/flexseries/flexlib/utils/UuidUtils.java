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

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods related to the {@link UUID} class.
 */
public final class UuidUtils {

    private static final Pattern UUID_FIXER_PATTERN = Pattern.compile("(\\w{8})-?(\\w{4})-?(\\w{4})-?(\\w{4})-?(\\w{12})");

    private UuidUtils() { }

    /**
     * Converts a raw UUID (with or without dashes) to a UUID.
     *
     * @return The converted UUID.
     * @throws IllegalArgumentException Thrown if the input is not a valid format.
     */
    public static UUID fromString(String raw) {
        Matcher matcher = UUID_FIXER_PATTERN.matcher(raw);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid UUID format '" + raw + "'");
        }
        return UUID.fromString(matcher.replaceAll("$1-$2-$3-$4-$5"));
    }

}