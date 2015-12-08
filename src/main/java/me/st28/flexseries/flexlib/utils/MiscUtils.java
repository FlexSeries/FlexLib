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