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

import org.bukkit.Bukkit;

/**
 * Miscellaneous utility methods.
 */
public final class MiscUtils {

    private MiscUtils() {}

    /**
     * Returns the number of pages required to fit a number of items.
     *
     * @param itemCount The total number of items.
     * @param amtPerPage How many items can exist per page.
     * @return The number of pages required.
     */
    public static int getPageCount(int itemCount, int amtPerPage) {
        if (itemCount == 0 || amtPerPage == 0) {
            return 0;
        }

        int pageCount = 0;
        for (int i = 1; i <= itemCount; i++) {
            if (i % amtPerPage == 1) pageCount++;
        }
        return pageCount;
    }

    /**
     * Verifies that this method is being called by an appropriate thread.
     *
     * @param isPrimary True to validate that this method is being called on the primary thread.<br />
     *                  False to validate that it is not being called on the primary thread.
     */
    public static void validateBukkitThread(boolean isPrimary) {
        if (isPrimary && !Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("This method can only be called by the primary thread.");
        } else if (Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("This method cannot be called by the primary thread.");
        }
    }

}