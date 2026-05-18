package com.deskpet.controllers;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.deskpet.DeskPetApplication;
import com.deskpet.data.DatabaseHandler;
import com.deskpet.models.DashboardModel;

import javafx.beans.binding.Bindings;
import javafx.beans.property.FloatProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;

public class DashboardController {
    @FXML
    private ProgressBar happinessBar;
    @FXML
    private ProgressBar hungerBar;
    @FXML
    private ProgressBar comfortBar;
    @FXML
    private ProgressBar socialBar;
    @FXML
    private Label lastMovement;
    private DatabaseHandler dbHandler;
    @FXML 
    private Label lastSound;

    private DeskPetApplication app;

    public void setApp(DeskPetApplication app) {
        this.app = app;
    }

    @FXML
    private void openSettings() {
        if (app != null) {
            app.openSettingsWindow();
        }
    }

    @FXML
    private Label activePetLabel;

    @FXML
    private Label favouriteFoodLabel;

    @FXML
    private ListView<String> foodStatsListView;

    @FXML
    private ListView<String> feedingHistoryListView;

    public void setActivePetName(String name) {
        activePetLabel.setText(name);
    }

    public void setActivePetFavouriteFood(String favouriteFood) {
        favouriteFoodLabel.setText(favouriteFood);
    }

    public void setFoodStats(List<String> foodStats) {
        foodStatsListView.getItems().setAll(foodStats);
    }

    public void setFeedingHistory(List<String> feedingHistory) {
        feedingHistoryListView.getItems().setAll(feedingHistory);
    }

    // Creates a background scheduler that can run tasks later or repeatedly
    private final ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor();

    public void updateLastMovement(long petId) {
        try {
            java.time.LocalDateTime time = dbHandler.getLastMovementTime(petId);
            if (time != null) {
                lastMovement.setText(time.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM  HH:mm")));
            }
            else {
                lastMovement.setText("No data");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateLastSound(String time) {
        if (lastSound != null) {
            lastSound.setText(time);
        }
    }

    private DashboardModel model = new DashboardModel();

    public DashboardModel getModel() {
        return model;
    }

    public void setModelDbHandler(DatabaseHandler dbHandler) {
        this.dbHandler = dbHandler;
        model.setDbHandler(dbHandler);
    }

    public void bindProgressBarWithColor(ProgressBar progressBar, FloatProperty property) {
        progressBar.progressProperty().bind(property);

        progressBar.styleProperty().bind(
            Bindings.createStringBinding(() -> {
                double value = property.get();

                if (value < 0.3) {
                    return "-fx-accent: red";
                } else if (value < 0.6) {
                    return "-fx-accent: yellow";
                } else {
                    return "-fx-accent: green";
                }
            }, property)
        );
    }

    public void initialize() {
        // No base decay added for happiness
        bindProgressBarWithColor(happinessBar, model.happinessProperty());

        bindProgressBarWithColor(hungerBar, model.hungerProperty());

        bindProgressBarWithColor(comfortBar, model.comfortProperty());

        bindProgressBarWithColor(socialBar, model.socialProperty());
    }
}
