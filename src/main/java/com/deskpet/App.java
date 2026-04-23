package com.deskpet;

import java.sql.Connection;
import javafx.application.Application;

public class App {
    public static int add(int a, int b) {
        return a + b;
    }

    public static void main(String[] args) {
        DeskPetApplication app = new DeskPetApplication();
        app.run();
    }
}