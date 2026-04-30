package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String DEFAULT_DB_PATH = "clinic.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }

    private Database() {
    }

    public static String getUrl() {
        String configuredUrl = System.getProperty("clinic.db.url");
        if (configuredUrl != null && !configuredUrl.isBlank()) {
            return configuredUrl;
        }

        String dbPath = System.getProperty("clinic.db.path", DEFAULT_DB_PATH);
        return "jdbc:sqlite:" + dbPath;
    }

    public static Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(getUrl());
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
