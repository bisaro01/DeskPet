package com.deskpet.controllers;

import com.deskpet.models.DashboardModel;
import javafx.application.Platform;
import javafx.beans.property.FloatProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.beans.binding.Bindings;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DashboardController {
    @FXML
    private ProgressBar happinessBar;
    @FXML
    private ProgressBar hungerBar;
    @FXML
    private ProgressBar comfortBar;

    // Creates a background scheduler that can run tasks later or repeatedly
    private final ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor();

    private DashboardModel model = new DashboardModel();

    public DashboardModel getModel() {
        return model;
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
        scheduler.scheduleAtFixedRate(() -> {
            // Hunger decreases with -0.5 per minute (0.5 of 100 = 0.005 of 1), GUI gets updated every second.
            model.setHunger(model.getHunger() - (0.005f / 60));
        }, 0, 1, TimeUnit.SECONDS);

        // No base decay added for comfort
        bindProgressBarWithColor(comfortBar, model.comfortProperty());
    }
}
