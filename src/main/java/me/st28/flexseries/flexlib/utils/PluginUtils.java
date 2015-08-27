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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for Bukkit's JavaPlugin class.
 */
public final class PluginUtils {

    private PluginUtils() { }

    private static Map<String, String> PLUGIN_NAMES = new HashMap<>();

    /**
     * @return The proper capitalization for a plugin's name, based on a raw name.
     */
    public static String getProperPluginName(String rawName) {
        if (PLUGIN_NAMES.containsKey(rawName.toLowerCase())) {
            return PLUGIN_NAMES.get(rawName.toLowerCase());
        }

        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin.getName().equalsIgnoreCase(rawName)) {
                String newName = plugin.getName();
                PLUGIN_NAMES.put(rawName.toLowerCase(), newName);
                return newName;
            }
        }
        return null;
    }

    /**
     * @return a plugin according to its raw name.
     */
    public static Plugin getPlugin(String rawName) {
        rawName = getProperPluginName(rawName);
        return rawName == null ? null : Bukkit.getPluginManager().getPlugin(rawName);
    }

    public static boolean saveFile(JavaPlugin plugin, String filePath) throws IOException {
        return saveFile(plugin, filePath, plugin.getDataFolder() + File.separator + filePath);
    }

    public static boolean saveFile(JavaPlugin plugin, String filePath, String toPath) throws IOException {
        filePath = filePath.replace(File.separator, "/");

        InputStream is = plugin.getResource(filePath);
        if (is == null) return false;

        Files.copy(is, new File(toPath).toPath());
        return true;
    }

}