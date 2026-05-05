package com.deskpet;

import com.deskpet.controllers.DashboardController;
import com.deskpet.models.Pet;
import com.deskpet.models.systems.ComfortSystem;
import com.deskpet.models.DashboardModel;
import com.deskpet.models.systems.HungerSystem;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class DeskPetApplication extends Application {
    private final Connection db;
    private final DatabaseHandler dbHandler;
    private HappinessPublisher happinessPublisher;
    private ComfortSubscriber comfortSubscriber;
    private HungerSubscriber hungerSubscriber;

    public DeskPetApplication() {
        try{
            db = Database.getConnection();
        } catch (java.sql.SQLException e) {
            System.out.println("Failed to create Database connection" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        dbHandler = new DatabaseHandler(db);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(DeskPetApplication.class.getResource("/com/deskpet/views/dashboardView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        DashboardController controller = fxmlLoader.getController();
        controller.setModelDbHandler(dbHandler);
        DashboardModel model = controller.getModel();

        try {
            pet = dbHandler.loadPet(1);

            if (pet == null) {
                System.out.println("DB error: unable to load pet. Using demo pet.");
                pet = new Pet(1, 80, 80, 80);
            } else {
                System.out.println("Loaded pet 1:");
                System.out.println("Happiness: " + pet.getHappiness());
                System.out.println("Hunger: " + pet.getHunger());
                System.out.println("Comfort: " + pet. getComfort());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        /*try {
            subscriber = new HappinessSubscriber();
            subscriber.setHappinessListener(controller.getModel());
            subscriber.start();
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            System.out.println("Failed to create HappinessSubscriber: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        } */

//        try {
//            publisher = new HappinessPublisher();
//        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
//            System.out.println("Failed to create HappinessPublisher" + e.getMessage());
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }

        // added the comfort subscriber system
        try {
            ComfortSystem comfortSystem = new ComfortSystem();
            comfortSubscriber = new ComfortSubscriber(comfortSystem);
            comfortSubscriber.setComfortListener(model);
            comfortSubscriber.start();
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            System.out.println("Failed to create ComfortSubscriber: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        try {
            HungerSystem hungerSystem = new HungerSystem();
            hungerSubscriber = new HungerSubscriber(hungerSystem);
            hungerSystem.setHungerListener(model);
            hungerSubscriber.start();
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            System.out.println("Failed to create HungerSubscriber: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        model.setHappinessSubscriber(subscriber);
        stage.setTitle("Desk Pet");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> {
            if (subscriber != null) {
                subscriber.stop();
            }
            if (hungerSubscriber != null) {
                try { hungerSubscriber.stop();} catch (Exception e) {e.printStackTrace();}
            }
            if (comfortSubscriber != null) {
                try { comfortSubscriber.stop();}  catch (Exception e) {e.printStackTrace();}
            }
            Platform.exit();
            System.exit(0);
        });
    }

    public void run() {
        Application.launch(this.getClass());
    }
}
