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
package me.st28.flexseries.flexlib.player.uuidtracker;

import me.st28.flexseries.flexlib.log.LogHelper;
import me.st28.flexseries.flexlib.storage.mysql.MySqlPoolManager;
import me.st28.flexseries.flexlib.utils.UuidUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

public class MySqlStorageHandler extends UuidTrackerStorageHandler {

    private final static String STATEMENT_CREATE_TABLE =
            "CREATE TABLE `{TABLE}` (" +
            "`uuid` BINARY(16) NOT NULL," +
            "`name` VARCHAR(16) NOT NULL," +
            "`time` TIMESTAMP NOT NULL," +
            "PRIMARY KEY (`uuid`, `name`)," +
            "INDEX (`uuid`)," +
            "INDEX (`name`)" +
            ") ENGINE=InnoDB CHARACTER SET=UTF8;";

    private final static String STATEMENT_INSERT_ENTRY =
            "INSERT INTO `{TABLE}` (`uuid`, `name`, `time`) " +
            "VALUES (UNHEX(?), ?, ?) ON DUPLICATE KEY UPDATE `time` = ?;";

    private final static String STATEMENT_SELECT_ALL =
            "SELECT HEX(`uuid`), `name`, `time` FROM `{TABLE}`;";

    private final static String STATEMENT_SELECT_SINGLE =
            "SELECT `name`, `time` FROM `{TABLE}` WHERE `uuid` = UNHEX(?);";

    private final static String TABLE_PLAYER_UUID = "player_uuid";

    // ------------------------------------------------------------------------------------------ //

    private MySqlPoolManager dbHelper;

    private Queue<UuidEntry> queuedUpdates = new LinkedBlockingQueue<>();
    private BukkitRunnable consumer;

    MySqlStorageHandler(PlayerUuidTracker manager) {
        super(manager, "MySQL");
    }

    @Override
    void enable() {
        dbHelper = new MySqlPoolManager(manager);

        try (Connection connection = dbHelper.getPool().getConnection()) {
            PreparedStatement ps = connection.prepareStatement(STATEMENT_CREATE_TABLE.replace("{TABLE}", dbHelper.getTableName(TABLE_PLAYER_UUID)));
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            LogHelper.severe(manager, "An exception occurred while initializing UUID database.", ex);
            return;
        }

        int runTime = manager.getConfig().getInt("storage.MySQL.consumer.run time");
        int interval = manager.getConfig().getInt("storage.MySQL.consumer.run interval");

        consumer = new BukkitRunnable() {
            @Override
            public void run() {
                if (queuedUpdates.isEmpty()) {
                    return;
                }

                final long startTime = System.currentTimeMillis();

                try (Connection connection = dbHelper.getPool().getConnection()) {
                    connection.setAutoCommit(false);

                    PreparedStatement ps = connection.prepareStatement(STATEMENT_INSERT_ENTRY.replace("{TABLE}", dbHelper.getTableName(TABLE_PLAYER_UUID)));

                    while (!queuedUpdates.isEmpty() && System.currentTimeMillis() - startTime < runTime) {
                        UuidEntry entry = queuedUpdates.poll();
                        if (entry == null || entry.names.isEmpty()) {
                            continue;
                        }

                        for (Entry<String, Long> nameEntry : entry.names.entrySet()) {
                            ps.setString(1, entry.uuid.toString());
                            ps.setString(2, nameEntry.getKey());
                            ps.setLong(3, nameEntry.getValue());
                            ps.setLong(4, nameEntry.getValue());
                        }

                        ps.addBatch();
                    }

                    ps.executeBatch();
                    connection.commit();
                } catch (SQLException ex) {
                    LogHelper.severe(manager, "An exception occurred while running the MySQL consumer.", ex);
                }
            }
        };

        consumer.runTaskTimerAsynchronously(manager.getPlugin(), interval, interval);
    }

    @Override
    Set<UuidEntry> loadAll() {
        Set<UuidEntry> returnSet = new HashSet<>();

        try (Connection connection = dbHelper.getPool().getConnection()) {
            PreparedStatement ps = connection.prepareStatement(STATEMENT_SELECT_ALL.replace("{TABLE}", dbHelper.getTableName(TABLE_PLAYER_UUID)));
            ResultSet rs = ps.executeQuery();

            // Load raw data
            Map<UUID, UuidEntry> rawData = new HashMap<>();
            while (rs.next()) {
                UUID uuid = UuidUtils.fromStringNoDashes(rs.getString("uuid"));

                if (!rawData.containsKey(uuid)) {
                    rawData.put(uuid, new UuidEntry(uuid));
                }

                UuidEntry entry = rawData.get(uuid);

                entry.names.put(rs.getString("name"), rs.getTimestamp("time").getTime());
            }

            // Determine current name
            for (UuidEntry entry : rawData.values()) {
                entry.determineCurrentName();
                returnSet.add(entry);
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LogHelper.severe(manager, "An exception occurred while loading all UUIDs.", ex);
            return null;
        }

        return returnSet;
    }

    @Override
    UuidEntry loadSingle(UUID uuid) {
        UuidEntry entry = null;

        try (Connection connection = dbHelper.getPool().getConnection()) {
            PreparedStatement ps = connection.prepareStatement(STATEMENT_SELECT_SINGLE.replace("{TABLE}", dbHelper.getTableName(TABLE_PLAYER_UUID)));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (entry == null) {
                    entry = new UuidEntry(uuid);
                }

                entry.names.put(rs.getString("name"), rs.getTimestamp("time").getTime());
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LogHelper.severe(manager, "An exception occurred while loading entry for UUID '" + uuid.toString() + "'", ex);
            return null;
        }

        return entry;
    }

    @Override
    void queueUpdate(UuidEntry entry) {
        Validate.notNull(entry, "Entry cannot be null.");
        queuedUpdates.add(entry);
    }

    @Override
    void save(boolean async, Set<UuidEntry> entries) {
        if (!async) {
            // If not async, server is likely shutting down/disabling plugins.
            // Force the consumer to run
            while (!queuedUpdates.isEmpty()) {
                consumer.run();
            }
        }
    }

}