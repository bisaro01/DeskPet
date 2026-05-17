package com.deskpet.mqtt;

import com.deskpet.domain.PetStateListener;
import javafx.application.Platform;
import org.eclipse.paho.client.mqttv3.*;

/* HungerSubscriber listens to the MQTT messages and updates the HungerSystem accordingly */
public class HungerSubscriber implements MqttCallback {
    private IMqttClient mqttClient;
    private static final String BROKER = "tcp://localhost:1883";
    private static final String CLIENT_ID = "hunger-subscriber";
    private static final String TOPIC = "deskpet/events/food"; //Changed to rfid_tag topic for testing purposes

    private PetStateListener pet;

    // Constructor
    public HungerSubscriber(PetStateListener pet) throws MqttException {
        this.pet = pet;

        mqttClient = new MqttClient(BROKER, CLIENT_ID);
        mqttClient.setCallback(this);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setKeepAliveInterval(60);
        options.setAutomaticReconnect(true);

        mqttClient.connect(options);
    }

    public void start() throws MqttException {
        mqttClient.subscribe(TOPIC);
    }

    public void stop() throws MqttException {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());

        switch (topic) {
            case TOPIC:
                Platform.runLater(() -> {
                    pet.feed(payload);
                });
                break;
        }
//        System.out.println("Hunger = " + hungerSystem.getHunger() + "%");
//        System.out.println("RFID Tag = " + payload);
//
//        if (hungerSystem != null) {
//            hungerSystem.updateHunger(payload);
//        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost hunger: " + cause.getMessage());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}