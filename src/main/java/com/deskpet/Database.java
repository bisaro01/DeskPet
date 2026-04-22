package com.deskpet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String HOST = "localhost";
    private static final String PORT = "5432";
    private static final String DATABASE = "deskpet";
    private static final String USER = "postgres";
    // Empty by default, if needed enter password here and in build.gradle(dbPassword).
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DATABASE;
        return DriverManager.getConnection(url, USER, PASSWORD);
    }
}
