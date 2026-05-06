package com.deskpet.mqtt;

// talking directly to the dashboard through the comfortListener same as Happiness system
import com.deskpet.domain.PetStateListener;

import javafx.application.Platform;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ComfortSubscriber implements MqttCallback {
    private IMqttClient MqttClient;
    private final static String BROKER = "tcp://localhost:1883";
    private final static String CLIENT_ID = "comfort-subscriber";
    private final static String TEMP_TOPIC = "wio/sensors/temperature";
    private final static String HUM_TOPIC = "wio/sensors/humidity";


    // talking directly to the dashboard through the PetStateListener
    private PetStateListener pet;


    // Constructor
    public ComfortSubscriber(PetStateListener pet) throws  MqttException {
        // talking directly to the dashboard through the comfortListener same as Happiness system
        this.pet = pet;

        MqttClient = new MqttClient(BROKER, CLIENT_ID);
        MqttClient.setCallback(this);
        // basic set up for connection keep connection actively alive every
        // 60 seconds
        MqttConnectOptions options = new MqttConnectOptions();
        options.setKeepAliveInterval(60);
        options.setAutomaticReconnect(true);
        MqttClient.connect(options);
        System.out.println("Connected to MQTT broker.");
    }

    // ComfortSubscriber start and stop MQTT broker connection methods
    public void start() throws MqttException {
        MqttClient.subscribe(TEMP_TOPIC);
        MqttClient.subscribe(HUM_TOPIC);

        // Debugging lines
        // System.out.println("Listening to: " + TOPIC);
        // System.out.println("Waiting for happiness values...");
        // System.out.println("Press Ctrl+C to stop");
    }

    public void stop() throws MqttException {
        try {
            if (MqttClient != null && MqttClient.isConnected()) {
                MqttClient.disconnect();
                // System.out.println("MQTT disconnected.");
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    // Mandatory MqttCallback abstract methods
    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost: " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());

        Platform.runLater(() -> {
            pet.updateNeed(topic, payload);
        });
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}
