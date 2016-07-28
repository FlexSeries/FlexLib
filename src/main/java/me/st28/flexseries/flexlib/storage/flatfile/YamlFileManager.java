/**
 * Copyright 2016 Stealth2800 <http://stealthyone.com/>
 * Copyright 2016 Contributors <https://github.com/FlexSeries>
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