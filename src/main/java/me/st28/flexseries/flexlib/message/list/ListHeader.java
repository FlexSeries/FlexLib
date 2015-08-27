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
package me.st28.flexseries.flexlib.message.list;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;
import java.util.Map.Entry;

public final class ListHeader {

    private String leftPrefix;
    private String leftText;
    private String leftSuffix;
    private String rightPrefix;
    private String rightText;
    private String rightSuffix;
    private String center;

    ListHeader(String name) {
        center = "&cUnknown header format: &6{NAME}&c.".replace("{NAME}", name);
    }

    public ListHeader(ConfigurationSection config) {
        ConfigurationSection leftSec = config.getConfigurationSection("left side");
        if (leftSec != null) {
            leftPrefix = leftSec.getString("prefix", "");
            leftText = leftSec.getString("text", "");
            leftSuffix = leftSec.getString("suffix", "");
        } else {
            leftPrefix = "";
            leftText = "";
            leftSuffix = "";
        }

        ConfigurationSection rightSec = config.getConfigurationSection("right side");
        if (rightSec != null) {
            rightPrefix = rightSec.getString("prefix",  "");
            rightText = rightSec.getString("text", "");
            rightSuffix = rightSec.getString("suffix", "");
        } else {
            rightPrefix = "";
            rightText = "";
            rightSuffix = "";
        }

        center = config.getString("center", "");
    }

    public String getFormattedHeader(int page, int maxPages, String key, String value) {
        if (value == null) value = "";

        String newCenter = ChatColor.translateAlternateColorCodes('&', center)
                .replace("{PAGE}", Integer.toString(page))
                .replace("{MAXPAGES}", Integer.toString(maxPages))
                .replace("{KEY}", key)
                .replace("{VALUE}", value);

        return getFinalizedFormat(newCenter);
    }

    public String getFormattedHeader(int page, int maxPages, Map<String, String> replacements) {
        String newCenter = ChatColor.translateAlternateColorCodes('&', center)
                .replace("{PAGE}", Integer.toString(page))
                .replace("{MAXPAGES}", Integer.toString(maxPages));

        for (Entry<String, String> entry : replacements.entrySet()) {
            newCenter = newCenter.replace(entry.getKey(), entry.getValue());
        }

        return getFinalizedFormat(newCenter);
    }

    private String getFinalizedFormat(String newCenter) {
        int centerLength = ChatColor.stripColor(newCenter).length();

        int sideLength = (ListManager.getInstance().lineLength - centerLength) / 2;
        if (sideLength <= 0) {
            sideLength = 1;
        }

        StringBuilder sb = new StringBuilder();

        if (!leftText.isEmpty()) {
            int leftRepeat = sideLength / leftText.length();
            if (leftRepeat > 0) {
                sb.append(leftPrefix);

                for (int i = 0; i < leftRepeat; i++) {
                    sb.append(leftText);
                }

                sb.append(leftSuffix);
            }
        }

        sb.append(newCenter);

        if (!rightText.isEmpty()) {
            int rightRepeat = sideLength / rightText.length();
            if (rightRepeat > 0) {
                sb.append(rightPrefix);

                for (int i = 0; i < rightRepeat; i++) {
                    sb.append(rightText);
                }

                sb.append(rightSuffix);
            }
        }

        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

}