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