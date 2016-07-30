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
package me.st28.flexseries.flexlib.player.lookup;

import me.st28.flexseries.flexlib.utils.UuidUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

abstract class Storage {

    abstract String getName();

    abstract void enable(PlayerLookupModule module, ConfigurationSection config) throws Exception;
    abstract void disable() throws Exception;

    abstract void update(UUID uuid, String name) throws Exception;

    abstract CacheEntry getEntry(String name) throws Exception;
    abstract CacheEntry getEntry(UUID uuid) throws Exception;

    // ------------------------------------------------------------------------------------------ //

    final static class SqliteStorage extends Storage {

        final static String NAME = "sqlite";

        private final Map<String, String> queries = new HashMap<>();

        private Connection connection;

        @Override
        String getName() {
            return NAME;
        }

        @Override
        void enable(PlayerLookupModule module, ConfigurationSection config) throws Exception {
            File file = new File(module.getDataFolder() + File.separator + config.getString("file", "uuidIndex.db"));
            if (!file.exists()) {
                file.createNewFile();
            }

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + file);

            // Load queries
            try (InputStream is = module.getResource("sqlite-queries.yml")) {
                try (InputStreamReader isr = new InputStreamReader(is)) {
                    YamlConfiguration queryConfig = YamlConfiguration.loadConfiguration(isr);

                    // Load table names
                    final Map<String, String> tableNames = new HashMap<>();
                    for (Entry<String, Object> entry : queryConfig.getConfigurationSection("tables").getValues(true).entrySet()) {
                        tableNames.put(entry.getKey(), (String) entry.getValue());
                    }

                    for (Entry<String, String> nameEntry : tableNames.entrySet()) {
                        final String curKey = nameEntry.getKey();

                        ConfigurationSection sec = queryConfig.getConfigurationSection(nameEntry.getKey());
                        for (Entry<String, Object> entry : sec.getValues(true).entrySet()) {
                            queries.put(
                                    curKey + "." + entry.getKey(),
                                    ((String) entry.getValue()).replace("{TABLE_" + curKey + "}", nameEntry.getValue())
                            );
                        }
                    }

                    // Setup tables
                    for (String table : tableNames.keySet()) {
                        try (PreparedStatement ps = connection.prepareStatement(queries.get(table + ".create_table"))) {
                            ps.executeUpdate();
                        }
                    }
                }
            }
        }

        @Override
        void disable() throws Exception {
            connection.close();
        }

        @Override
        void update(UUID uuid, String name) throws Exception {
            Validate.notNull(uuid, "UUID cannot be null");
            Validate.notNull(name, "Name cannot be null");

            try (PreparedStatement ps = connection.prepareStatement(queries.get("ENTRY.delete_name"))) {
                ps.setString(1, name.toLowerCase());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = connection.prepareStatement(
                    queries.get("ENTRY.delete_uuid").replace("{UUID}", uuid.toString().replace("-", ""))))
            {
                ps.executeUpdate();
            }

            try (PreparedStatement ps = connection.prepareStatement(queries.get("ENTRY.insert_entry")
                    .replace("{UUID}", uuid.toString().replace("-", ""))))
            {
                ps.setString(1, name.toLowerCase());
                ps.setString(2, name);
                ps.executeUpdate();
            }
        }

        @Override
        CacheEntry getEntry(String name) throws Exception {
            Validate.notNull(name, "Name cannot be null");

            try (PreparedStatement ps = connection.prepareStatement(queries.get("ENTRY.select_name"))) {
                ps.setString(1, name.toLowerCase());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new CacheEntry(UuidUtils.fromString(rs.getString(1)), rs.getString(2));
                    }
                }
            }

            return null;
        }

        @Override
        CacheEntry getEntry(UUID uuid) throws Exception {
            Validate.notNull(uuid, "UUID cannot be null");

            try (PreparedStatement ps = connection.prepareStatement(
                    queries.get("ENTRY.select_uuid").replace("{UUID}", uuid.toString().replace("-", ""))))
            {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new CacheEntry(uuid, rs.getString(2));
                    }
                }
            }

            return null;
        }

    }


}