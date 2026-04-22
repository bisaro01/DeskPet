package com.deskpet;

import java.sql.Connection;

public class App {
    public static int add(int a, int b) {
        return a + b;
    }

    public static void main(String[] args) {
        System.out.println("hello deskpet");
        try (Connection connection = Database.getConnection()) {
            System.out.println("Database connection successful");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}