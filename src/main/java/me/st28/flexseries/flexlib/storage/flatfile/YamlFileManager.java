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
package me.st28.flexseries.flexlib.storage.flatfile;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Helper class for reading and writing YAML files.
 */
public class YamlFileManager {

    public final static Pattern YAML_FILE_PATTERN = Pattern.compile("^^.+\\.yml");

    private File file;
    private FileConfiguration config;

    public YamlFileManager(String filePath) {
        Validate.notNull(filePath, "File path cannot be null.");

        file = new File(filePath);
        reload();
    }

    public YamlFileManager(File file) {
        Validate.notNull(file, "File cannot be null.");

        this.file = file;
        reload();
    }

    /**
     * Reloads the configuration file.
     */
    public void reload() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                save();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Saves the configuration to the file.
     */
    public void save() {
        if (file == null || config == null) {
            reload();
        } else {
            try {
                config.save(file);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * @return the configuration instance.
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * @return true if the configuration is empty.
     */
    public boolean isEmpty() {
        return config.getKeys(false).size() == 0;
    }

    /**
     * Copies default values from another configuration.
     */
    public void copyDefaults(Configuration otherConfig) {
        Validate.notNull(otherConfig, "Other config cannot be null.");

        config.addDefaults(otherConfig);
        config.options().copyDefaults(true);
        save();
    }

    /**
     * @return the file the configuration is saved in.
     */
    public File getFile() {
        return file;
    }

    /**
     * @return the name of the file.
     */
    public String getName() {
        return file.getName();
    }

}