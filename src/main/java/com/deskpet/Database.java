package com.deskpet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String DATABASE = "deskpet";
    private static final String USER = "postgres";

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:postgresql:" + DATABASE;
        return DriverManager.getConnection(url, USER, null);
    }
}