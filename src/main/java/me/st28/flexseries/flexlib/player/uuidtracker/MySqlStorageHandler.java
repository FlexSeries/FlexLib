package me.st28.flexseries.flexlib.player.uuidtracker;

import me.st28.flexseries.flexlib.log.LogHelper;
import me.st28.flexseries.flexlib.storage.mysql.MySqlPoolManager;
import me.st28.flexseries.flexlib.utils.ArgumentCallback;
import me.st28.flexseries.flexlib.utils.TaskChain;
import me.st28.flexseries.flexlib.utils.TaskChain.AsyncGenericTask;
import me.st28.flexseries.flexlib.utils.TaskChain.VariableGenericTask;
import me.st28.flexseries.flexlib.utils.UuidUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
            "VALUES (UNHEX(?), ?, ?);";

    private final static String STATEMENT_SELECT_ALL =
            "SELECT HEX(`uuid`), `name`, `time` FROM `{TABLE}`;";

    private final static String TABLE_PLAYER_UUID = "player_uuid";

    // ------------------------------------------------------------------------------------------ //

    private MySqlPoolManager dbHelper;

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
        }
    }

    @Override
    void loadAll(ArgumentCallback<Set<UuidEntry>> callback) {
        new TaskChain().add(new AsyncGenericTask() {
            @Override
            protected void run() {
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
                    return;
                }

                callback.call(returnSet);
            }
        }).execute();
    }

    @Override
    void loadSingle(UUID uuid, ArgumentCallback<UuidEntry> callback) {
        NYI;
    }

    @Override
    void save(boolean async, Set<UuidEntry> entries) {
        new TaskChain().add(new VariableGenericTask(async) {
            @Override
            protected void run() {
                NYI;
            }
        }).execute();
    }

}