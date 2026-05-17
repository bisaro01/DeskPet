package com.deskpet;

import java.io.IOException;
import java.sql.SQLException;

import com.deskpet.domain.PetDisplayMapper;
import com.deskpet.domain.PetDisplayState;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.deskpet.controllers.DashboardController;
import com.deskpet.data.DatabaseHandler;
import com.deskpet.domain.Pet;
import com.deskpet.models.DashboardModel;
import com.deskpet.mqtt.ComfortSubscriber;
import com.deskpet.mqtt.HappinessPublisher;
import com.deskpet.mqtt.HungerSubscriber;
import com.deskpet.mqtt.SocialSubscriber;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DeskPetApplication extends Application {
    private final DatabaseHandler dbHandler;
    private HappinessPublisher happinessPublisher;
    private ComfortSubscriber comfortSubscriber;
    private SocialSubscriber socialSubscriber;
    private HungerSubscriber hungerSubscriber;
    private PetDisplayMapper displayMapper;
    private Pet pet;

    private long lastUpdate;

    public DeskPetApplication() {
        dbHandler = new DatabaseHandler();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(DeskPetApplication.class.getResource("/com/deskpet/views/dashboardView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        DashboardController controller = fxmlLoader.getController();
        controller.setModelDbHandler(dbHandler);
        DashboardModel model = controller.getModel();

        displayMapper = new PetDisplayMapper();

        try {
            pet = dbHandler.loadPet(1);

            if (pet == null) {
                System.out.println("DB error: unable to load pet. Using demo pet.");
                pet = new Pet(1, 80, 80, 80, dbHandler);
            } else {
                System.out.println("Loaded pet 1:");
                System.out.println("Happiness: " + pet.getHappiness());
                System.out.println("Hunger: " + pet.getHunger());
                System.out.println("Comfort: " + pet.getComfort());
                System.out.println("Social: " + pet.getSocial());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        try {
            happinessPublisher = new HappinessPublisher();
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            System.out.println("Failed to create HappinessPublisher" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        try {
            comfortSubscriber = new ComfortSubscriber(pet);
            comfortSubscriber.start();
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            System.out.println("Failed to create ComfortSubscriber" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        try {
            hungerSubscriber = new HungerSubscriber(pet);
            hungerSubscriber.start();
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            System.out.println("Failed to create HungerSubscriber" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        try {
            socialSubscriber = new SocialSubscriber(pet);
            socialSubscriber.start();
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            System.out.println("Failed to create SocialSubscriber" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        lastUpdate = System.currentTimeMillis();
        PetDisplayState lastPublishedState;

        Timeline fixedUpdate = new Timeline(
            new KeyFrame(Duration.millis(3000), e -> {
                long now = System.currentTimeMillis();
                double delta = (now - lastUpdate) / 1000.0;
                lastUpdate = now;

                if (pet.wasMotionDetected()) {
                    try {
                        dbHandler.logSensorData(pet.getPetId(), "MOVEMENT", 1);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }


                else if (pet.wasNoiseDetected()) {
                    try {
                        dbHandler.logSensorData(pet.getPetId(), "NOISE", 1);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }

                pet.update(delta);

                try {
                    model.updateState(pet);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                PetDisplayState currentPetState = displayMapper.map(pet);

                try {
                    // TODO(Mark): publish currentPetState instead of happiness. Maybe rename publisher. Refactor WIO sketch.
                    happinessPublisher.publish(currentPetState);
//                    happinessPublisher.publish(pet.getHappiness());
                } catch (MqttException ex) {
                    ex.printStackTrace();
                }

                System.out.println("----------------------------------");
                System.out.println("Happiness: " + pet.getHappiness());
                System.out.println("Comfort: " + pet.getComfort());
                System.out.println("Social: " + pet.getSocial());
                System.out.println("Hunger: " + pet.getHunger());
            })
        );

        fixedUpdate.setCycleCount(Animation.INDEFINITE);
        fixedUpdate.play();

        stage.setTitle("Desk Pet");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> {
            if (happinessPublisher != null) {
                try {
                    happinessPublisher.stop();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
            if (hungerSubscriber != null) {
                try {
                    hungerSubscriber.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (comfortSubscriber != null) {
                try {
                    comfortSubscriber.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (socialSubscriber != null) {
                try {
                    socialSubscriber.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Platform.exit();
            System.exit(0);
        });
    }
    public void run() {
        Application.launch(this.getClass());
    }
}