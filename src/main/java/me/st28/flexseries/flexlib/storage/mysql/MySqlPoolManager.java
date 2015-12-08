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
package me.st28.flexseries.flexlib.storage.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.pool.HikariPool;
import me.st28.flexseries.flexlib.log.LogHelper;
import me.st28.flexseries.flexlib.plugin.module.FlexModule;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Helper class for modules for setting up a MySQL connection pool (using HikariCP).
 */
public class MySqlPoolManager {

    private FlexModule module;

    private HikariPool pool;
    private String tablePrefix;

    public MySqlPoolManager(FlexModule module) {
        this.module = module;
    }

    /**
     * Loads configuration from the default section (storage.MySQL)
     */
    public void load() {
        load(module.getConfig().getConfigurationSection("storage.MySQL"));
    }

    public void disable() {
        try {
            pool.shutdown();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Loads from a specific configuration section.
     */
    public void load(ConfigurationSection config) {
        Validate.notNull(config, "Config cannot be null.");

        LogHelper.info(module, "Initializing MySQL connection pool...");

        HikariConfig dbConfig = new HikariConfig();

        String host = config.getString("host", "localhost");
        int port = config.getInt("port", 3306);
        String database = config.getString("database", "minecraft");
        String username = config.getString("username", "username");
        String password = config.getString("password", "password");
        tablePrefix = config.getString("prefix", "ft_");

        dbConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        dbConfig.setUsername(username);
        dbConfig.setPassword(password);

        LogHelper.info(module, "Attempting to connect to " + host + ":" + port + "/" + database);

        try {
            pool = new HikariPool(dbConfig);
        } catch (Exception ex) {
            LogHelper.severe(module, "An exception occurred while loading the MySQL connection pool.", ex);
            return;
        }

        LogHelper.info(module, "Successfully connected to MySQL database.");
    }

    public HikariPool getPool() {
        return pool;
    }

    public String getTableName(String table) {
        Validate.notNull(table, "Table cannot be null.");
        return tablePrefix + table;
    }

}