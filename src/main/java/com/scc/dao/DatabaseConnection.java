package com.scc.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:appliance.db";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);

        // Print the exact absolute file path so you can open this *exact* file in DB Browser
        File dbFile = new File("appliance.db");
        System.out.println("-> ACTIVE DATABASE PATH: " + dbFile.getAbsolutePath());

        // SQLite does not enforce foreign keys unless this is turned on for every connection
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }
}