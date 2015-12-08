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