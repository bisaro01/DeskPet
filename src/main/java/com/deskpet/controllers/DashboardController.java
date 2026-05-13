package com.deskpet.controllers;

import com.deskpet.data.DatabaseHandler;
import com.deskpet.models.DashboardModel;
import javafx.beans.property.FloatProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.beans.binding.Bindings;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import com.deskpet.DeskPetApplication;
import javafx.scene.control.Label;

public class DashboardController {
    @FXML
    private ProgressBar happinessBar;
    @FXML
    private ProgressBar hungerBar;
    @FXML
    private ProgressBar comfortBar;
    @FXML
    private ProgressBar socialBar;

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

    public void setActivePetName(String name) {
        activePetLabel.setText(name);
    }

    // Creates a background scheduler that can run tasks later or repeatedly
    private final ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor();

    private DashboardModel model = new DashboardModel();

    public DashboardModel getModel() {
        return model;
    }

    public void setModelDbHandler(DatabaseHandler dbHandler) {model.setDbHandler(dbHandler);}

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
