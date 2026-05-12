package com.deskpet.controllers;

import com.deskpet.DeskPetApplication;
import com.deskpet.data.DatabaseHandler;
import com.deskpet.domain.Pet;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class SettingsController {
    private DeskPetApplication app;
    private DatabaseHandler dbHandler;
    private Pet currentPet;

    @FXML
    private ComboBox<String> petComboBox;

    @FXML
    private TextField newPetNameField;

    @FXML
    private TextField tempMinField;

    @FXML
    private TextField tempMaxField;

    @FXML
    private TextField humidityMinField;

    @FXML
    private TextField humidityMaxField;

    @FXML
    private TextField noiseMinField;

    @FXML
    private TextField noiseMaxField;

    @FXML
    private Label messageLabel;

    @FXML
    private Label currentTempMinLabel;

    @FXML
    private Label currentTempMaxLabel;

    @FXML
    private Label currentHumidityMinLabel;

    @FXML
    private Label currentHumidityMaxLabel;

    @FXML
    private Label currentNoiseMinLabel;

    @FXML
    private Label currentNoiseMaxLabel;

    public void setApp(DeskPetApplication app) {
        this.app = app;
    }

    public void setDbHandler(DatabaseHandler dbHandler) {
        this.dbHandler = dbHandler;
        loadPets();
    }

    public void setCurrentPet(Pet currentPet) {
        this.currentPet = currentPet;
        updateCurrentSettingsLabels();
    }

    private void loadPets() {
        try {
            petComboBox.getItems().setAll(dbHandler.getAllPets());
        } catch (SQLException e) {
            messageLabel.setText("Could not load pets.");
            e.printStackTrace();
        }
    }

    private int getNewOrCurrentValue(TextField field, double currentValue) {
        String text = field.getText().trim();

        if (text.isEmpty()) {
            return (int) currentValue;
        }

        return Integer.parseInt(text);
    }

    @FXML
    private void createPet() {
        String name = newPetNameField.getText().trim();

        if (name.isEmpty()) {
            messageLabel.setText("Pet name cannot be empty.");
            return;
        }

        try {
            Pet newPet = dbHandler.createPet(name);
            loadPets();
            newPetNameField.clear();
            messageLabel.setText("Created pet: " + name);

        } catch (SQLException e) {
            messageLabel.setText(e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void loadSelectedPet() {
        String selected = petComboBox.getValue();

        if (selected == null) {
            messageLabel.setText("Choose a pet first.");
            return;
        }

        long petId = Long.parseLong(selected.split(":")[0]);

        app.switchPet(petId);
        currentPet = app.getCurrentPet();

        updateCurrentSettingsLabels();

        messageLabel.setText("Loaded pet " + petId);
    }

    private void updateCurrentSettingsLabels() {
        currentTempMinLabel.setText(String.valueOf((int) currentPet.getMinTemp()));
        currentTempMaxLabel.setText(String.valueOf((int) currentPet.getMaxTemp()));
        currentHumidityMinLabel.setText(String.valueOf((int) currentPet.getMinHumidity()));
        currentHumidityMaxLabel.setText(String.valueOf((int) currentPet.getMaxHumidity()));
        currentNoiseMinLabel.setText(String.valueOf((int) currentPet.getMinNoise()));
        currentNoiseMaxLabel.setText(String.valueOf((int) currentPet.getMaxNoise()));
    }

    @FXML
    private void saveSettings() {
        if (currentPet == null) {
            messageLabel.setText("No pet loaded.");
            return;
        }

        try {
            int tempMin = getNewOrCurrentValue(tempMinField, currentPet.getMinTemp());
            int tempMax = getNewOrCurrentValue(tempMaxField, currentPet.getMaxTemp());
            int humidityMin = getNewOrCurrentValue(humidityMinField, currentPet.getMinHumidity());
            int humidityMax = getNewOrCurrentValue(humidityMaxField, currentPet.getMaxHumidity());
            int noiseMin = getNewOrCurrentValue(noiseMinField, currentPet.getMinNoise());
            int noiseMax = getNewOrCurrentValue(noiseMaxField, currentPet.getMaxNoise());

            dbHandler.updatePetSettings(currentPet.getPetId(), tempMin, tempMax, humidityMin, humidityMax, noiseMin, noiseMax);

            currentPet.setTemperatureSettings(tempMin, tempMax);
            currentPet.setHumiditySettings(humidityMin, humidityMax);
            currentPet.setNoiseSettings(noiseMin, noiseMax);

            updateCurrentSettingsLabels();

            tempMinField.clear();
            tempMaxField.clear();
            humidityMinField.clear();
            humidityMaxField.clear();
            noiseMinField.clear();
            noiseMaxField.clear();

            messageLabel.setText("Settings saved.");

        } catch (NumberFormatException e) {
            messageLabel.setText("All settings must be numbers.");
        } catch (SQLException e) {
            messageLabel.setText("Could not save settings.");
            e.printStackTrace();
        }

    }
}