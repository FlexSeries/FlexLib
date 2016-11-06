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
package me.st28.flexseries.flexlib.plugin.storage.flatfile

import org.bukkit.configuration.Configuration
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * Helper class for reading and writing YAML files.
 */
class YamlFileManager(filePath: String) {

    val file: File
    var config: FileConfiguration
        private set

    init {
        file = File(filePath)
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        config = YamlConfiguration.loadConfiguration(file)
        save()
        reload()
    }

    fun reload() {
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
            save()
        }
        config = YamlConfiguration.loadConfiguration(file)
    }

    fun save() {
        config.save(file)
    }

    /**
     * @return True if the configuration is empty.
     */
    fun isEmpty(): Boolean {
        return config.getKeys(false).size == 0
    }

    fun copyDefaults(other: Configuration) {
        config.addDefaults(other)
        config.options().copyDefaults(true)
        save()
    }

}