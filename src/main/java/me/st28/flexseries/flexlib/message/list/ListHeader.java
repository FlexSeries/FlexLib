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