package com.deskpet;

import com.deskpet.controllers.DashboardController;
import com.deskpet.models.systems.ComfortSystem;
import com.deskpet.models.DashboardModel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.sql.Connection;

public class DeskPetApplication extends Application {
    private final Connection db;
    private HappinessSubscriber subscriber;
//    private HappinessPublisher publisher;
    private ComfortSubscriber comfortSubscriber;


    public DeskPetApplication() {
        try{
            db = Database.getConnection();
        } catch (java.sql.SQLException e) {
            System.out.println("Failed to create Database connection" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(DeskPetApplication.class.getResource("/com/deskpet/views/dashboardView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        DashboardController controller = fxmlLoader.getController();
        DashboardModel model = controller.getModel();

        try {
            subscriber = new HappinessSubscriber();
            subscriber.setHappinessListener(controller.getModel());
            subscriber.start();
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            System.out.println("Failed to create HappinessSubscriber: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

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


        model.setHappinessSubscriber(subscriber);
        stage.setTitle("Desk Pet");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> {
            if (subscriber != null) {
                subscriber.stop();
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
