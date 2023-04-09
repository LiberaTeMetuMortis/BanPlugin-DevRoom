package com.github.liberatemetumortis.banplugin.database;

import com.github.liberatemetumortis.banplugin.BanPlugin;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.*;

public class Database {
    // Fetch bans from database
    private final Connection connection;
    public Connection getConnection() {
        return connection;
    }
    public Database(BanPlugin plugin) {
        ConfigurationSection databaseConfig = plugin.getConfig().getConfigurationSection("database");
        String host = databaseConfig.getString("host");
        String port = databaseConfig.getString("port");
        String databaseName = databaseConfig.getString("databaseName");
        String username = databaseConfig.getString("username");
        String password = databaseConfig.getString("password");
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + databaseName, username, password);
            connection.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS moderations (" +
                            "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                            "issuerID VARCHAR(36), " +
                            "issuerName VARCHAR(16), " +
                            "targetID VARCHAR(36), " +
                            "targetName VARCHAR(16), " +
                            "reason TEXT, " +
                            "timeIssued BIGINT, " +
                            "timeExpires BIGINT, " +
                            "isBan BOOLEAN);"
            );
        }catch (SQLException e) {
            throw new Error("Unable to connect to database", e);
        }
    }
}
