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

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Utility methods related to the {@link java.util.UUID} class.
 */
public final class UuidUtils {

    private static final Pattern UUID_FIXER_PATTERN = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

    private UuidUtils() {}

    public static UUID fromStringNoDashes(String rawUuid) {
        return UUID.fromString(UUID_FIXER_PATTERN.matcher(rawUuid).replaceAll("$1-$2-$3-$4-$5"));
    }

}