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
        pool.shutdown();
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