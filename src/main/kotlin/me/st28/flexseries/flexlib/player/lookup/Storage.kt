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
package me.st28.flexseries.flexlib.player.lookup

import kotlinx.support.jdk7.use
import me.st28.flexseries.flexlib.util.UuidUtils
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.util.*

internal interface Storage {

    fun getName(): String

    fun enable(module: PlayerLookupModule, config: ConfigurationSection)
    fun disable()

    fun update(uuid: UUID, name: String)

    fun getEntry(name: String): CacheEntry?
    fun getEntry(uuid: UUID): CacheEntry?

}

internal class Storage_SQLite : Storage {

    private val queries: MutableMap<String, String> = HashMap()

    private lateinit var connection: Connection

    override fun getName(): String {
        return "sqlite"
    }

    override fun enable(module: PlayerLookupModule, config: ConfigurationSection) {
        val file = File(module.dataFolder.toString() + File.separator + config.getString("file", "uuidIndex.db"))
        if (!file.exists()) {
            file.createNewFile()
        }

        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:" + file)

        // Load queries
        module.getResource("sqlite-queries.yml").reader().use {
            val queryConfig = YamlConfiguration.loadConfiguration(it)

            // Load table names
            val tableNames: MutableMap<String, String> = HashMap()
            for ((key, value) in queryConfig.getConfigurationSection("tables").getValues(true)) {
                tableNames.put(key, value as String)
            }

            for ((nameKey, nameVal) in tableNames) {
                val sec = queryConfig.getConfigurationSection(nameKey)
                for ((key, value) in sec.getValues(true)) {
                    queries.put(
                        "$nameKey.$key",
                        (value as String).replace("{TABLE_$nameKey}", nameVal)
                    )
                }
            }

            // Setup tables
            for (table in tableNames.keys) {
                val ps = connection.prepareStatement(queries["$table.create_table"])
                try {
                    ps.executeUpdate()
                } finally {
                    ps?.close()
                }
            }
        }
    }

    override fun disable() {
        connection.close()
    }

    override fun update(uuid: UUID, name: String) {
        connection.prepareStatement(queries["ENTRY.delete_name"]).use {
            it.setString(1, name.toLowerCase())
            it.executeUpdate()
        }

        connection.prepareStatement(
            queries["ENTRY.delete_uuid"]!!.replace("{UUID}", uuid.toString().replace("-", ""))
        ).use(PreparedStatement::executeUpdate)

        connection.prepareStatement(
                queries["ENTRY.insert_entry"]!!.replace("{UUID}", uuid.toString().replace("-", ""))
        ).use {
            it.setString(1, name.toLowerCase())
            it.setString(2, name)
            it.executeUpdate()
        }
    }

    override fun getEntry(name: String): CacheEntry? {
        connection.prepareStatement(queries["ENTRY.select_name"]!!).use {
            it.setString(1, name.toLowerCase())

            it.executeQuery().use {
                if (it.next()) {
                    return CacheEntry(UuidUtils.fromString(it.getString(1)), it.getString(2))
                }
            }
        }
        return null
    }

    override fun getEntry(uuid: UUID): CacheEntry? {
        connection.prepareStatement(
                queries["ENTRY.select_uuid"]!!.replace("{UUID}", uuid.toString().replace("-", ""))
        ).use {
            it.executeQuery().use {
                if (it.next()) {
                    return CacheEntry(uuid, it.getString(2))
                }
            }
        }
        return null
    }

}
