package com.deskpet;

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
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.sql.SQLException;

public class DeskPetApplication extends Application {
    private DashboardModel model;
    private DashboardController controller;
    private final DatabaseHandler dbHandler;
    private HappinessPublisher happinessPublisher;
    private ComfortSubscriber comfortSubscriber;
    private SocialSubscriber socialSubscriber;
    private HungerSubscriber hungerSubscriber;
    private Pet pet;

    private long lastUpdate;
    private long lastSensorLogTime = 0;
    private static final long SENSOR_LOG_INTERVAL = 60000;  // save hum&tem every 60s to avoid spamming DB

    public DeskPetApplication() {
        dbHandler = new DatabaseHandler();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(DeskPetApplication.class.getResource("/com/deskpet/views/dashboardView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        controller = fxmlLoader.getController();
        controller.setApp(this);
        controller.setModelDbHandler(dbHandler);
        model = controller.getModel();

        try {
            long firstPetId = dbHandler.getFirstPetId();
            pet = dbHandler.loadPet(firstPetId);

            if (pet == null) {
                System.out.println("DB error: unable to load pet. Using demo pet.");
                pet = new Pet(1, "Bob", "Fish", 80, 80, 80, dbHandler);
            } else {
                System.out.println("Loaded pet " + pet.getPetId() + ":");
                System.out.println("Happiness: " + pet.getHappiness());
                System.out.println("Hunger: " + pet.getHunger());
                System.out.println("Comfort: " + pet.getComfort());
                System.out.println("Social: " + pet.getSocial());
            }

            model.updateState(pet);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        controller.setActivePetName(pet.getName());
        controller.setActivePetFavouriteFood(pet.getFavouriteFood());

        try {
            controller.setFoodStats(dbHandler.getFoodStats(pet.getPetId()));
        } catch (SQLException e) {
            System.out.println("Failed to create FoodStats" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        try {
            controller.setFeedingHistory(dbHandler.getFeedingHistory(pet.getPetId()));
        } catch (SQLException e) {
            System.out.println("Failed to create FeedingHistory" + e.getMessage());
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

        Timeline fixedUpdate = new Timeline(new KeyFrame(Duration.millis(3000), e -> {
            long now = System.currentTimeMillis();
            double delta = (now - lastUpdate) / 1000.0;
            lastUpdate = now;

                // ======================    save sensor data to the DB ========================
                // movement
                if (pet.wasMotionDetected()) {
                    try {
                        dbHandler.logSensorData(pet.getPetId(), "MOVEMENT", 1);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }

                // noise
                if (pet.wasNoiseDetected()) {
                    try {
                        dbHandler.logSensorData(pet.getPetId(), "NOISE", 1);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }

                // hum & temp
                if (now - lastSensorLogTime > SENSOR_LOG_INTERVAL) {
                    try {
                        dbHandler.logSensorData(pet.getPetId(), "TEMPERATURE", pet.getTemperature());
                        dbHandler.logSensorData(pet.getPetId(), "HUMIDITY", pet.getHumidity());
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }

            try {
                controller.setFoodStats(dbHandler.getFoodStats(pet.getPetId()));
            } catch (SQLException ex) {
                System.out.println("Failed to create FoodStats" + ex.getMessage());
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }

            try {
                controller.setFeedingHistory(dbHandler.getFeedingHistory(pet.getPetId()));
            } catch (SQLException ex) {
                System.out.println("Failed to create FeedingHistory" + ex.getMessage());
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }

            pet.update(delta);

            try {
                model.updateState(pet);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            try {
                happinessPublisher.publish(pet.getHappiness());
            } catch (MqttException ex) {
                ex.printStackTrace();
            }

            System.out.println("----------------------------------");
            System.out.println("Happiness: " + pet.getHappiness());
            System.out.println("Comfort: " + pet.getComfort());
            System.out.println("Social: " + pet.getSocial());
            System.out.println("Hunger: " + pet.getHunger());
        }));

        fixedUpdate.setCycleCount(Animation.INDEFINITE);
        fixedUpdate.play();

        stage.setTitle("Desk Pet");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> {
            try {
                dbHandler.updatePetState(pet);
            } catch (SQLException e) {
                e.printStackTrace();
            }

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

    public void openSettingsWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(DeskPetApplication.class.getResource("/com/deskpet/views/settingsView.fxml"));

            Scene scene = new Scene(loader.load());

            com.deskpet.controllers.SettingsController controller = loader.getController();
            controller.setApp(this);
            controller.setDbHandler(dbHandler);
            controller.setCurrentPet(pet);

            Stage settingsStage = new Stage();
            settingsStage.setTitle("Desk Pet Settings");
            settingsStage.setScene(scene);
            settingsStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Pet getCurrentPet() {
        return pet;
    }

    // Load a new pet
    public void switchPet(long petId) {
        try {
            dbHandler.updatePetState(pet);

            pet = dbHandler.loadPet(petId);

            controller.setActivePetName(pet.getName());
            controller.setActivePetFavouriteFood(pet.getFavouriteFood());
            controller.setFoodStats(dbHandler.getFoodStats(petId));

            comfortSubscriber.setPet(pet);
            hungerSubscriber.setPet(pet);
            socialSubscriber.setPet(pet);

            model.updateState(pet);

            lastUpdate = System.currentTimeMillis();

            System.out.println("Switched to pet " + petId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        Application.launch(this.getClass());
    }
}