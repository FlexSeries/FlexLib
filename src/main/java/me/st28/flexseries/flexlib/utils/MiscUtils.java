package me.st28.flexseries.flexlib.utils;

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

}