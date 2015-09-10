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
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for Bukkit's ChatColor class.
 */
public final class ChatColorUtils {

    private ChatColorUtils() {}

    /**
     * Basically ChatColor.translateAlternateColorCodes on a string array.
     *
     * @param altColorChar Code to use.
     * @param arrayToTranslate String array to format.
     * @return Newly formatted string array.
     */
    public static String[] translateAlternateColorCodes(char altColorChar, String[] arrayToTranslate) {
        List<String> returnList = new ArrayList<>();
        for (String string : arrayToTranslate) {
            returnList.add(ChatColor.translateAlternateColorCodes(altColorChar, string));
        }
        return returnList.toArray(new String[returnList.size()]);
    }

    /**
     * Replaces the color codes in ChatColor where found.
     *
     * @param string String to colorize.
     * @return Newly colorized string.
     */
    public static String colorizeString(String string) {
        Validate.notNull(string, "String cannot be null.");
        for (ChatColor val : ChatColor.values()) {
            if (!val.isFormat()) {
                string = ChatColorUtils.singleFormatString(string, val);
            }
        }
        return string;
    }

    /**
     * Replaces the bold, italic, strikethrough, and underline effects where found.
     *
     * @param string String to format.
     * @return Newly formatted string.
     */
    public static String formatString(String string) {
        Validate.notNull(string, "String cannot be null.");
        for (ChatColor val : ChatColor.values()) {
            if (val.isFormat()) {
                string = ChatColorUtils.singleFormatString(string, val);
            }
        }
        return string;
    }

    /**
     * Replaces the 'magic' ChatColor in a string where found.
     *
     * @param string String to format.
     * @return Newly formatted string.
     */
    public static String magicfyString(String string) {
        Validate.notNull(string, "String cannot be null.");
        return ChatColorUtils.singleFormatString(string, ChatColor.MAGIC);
    }

    /**
     * Replaces the specified ChatColor in a given string.
     *
     * @param string String to format.
     * @param format ChatFormat to use.
     * @return The newly formatted string.
     */
    public static String singleFormatString(String string, ChatColor format) {
        Validate.notNull(string, "String cannot be null.");
        Validate.notNull(format, "Format cannot be null.");

        return string.replace("&" + format.getChar(), format.toString());
    }

}