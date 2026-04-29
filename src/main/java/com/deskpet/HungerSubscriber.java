package com.deskpet;

import com.deskpet.domain.HungerListener;
import com.deskpet.models.HungerSystem;
import org.eclipse.paho.client.mqttv3.*;

public class HungerSubscriber implements MqttCallback {
    private IMqttClient mqttClient;
    private final static String BROKER = "tcp://localhost:1883";
    private final static String CLIENT_ID = "happiness-subscriber";
    private final static String TOPIC = "deskpet/happiness";

    private HungerListener hungerListener;
    private HungerSystem hungerSystem;

    // Constructor
    public HungerSubscriber(HungerSystem hungerSystem) throws MqttException {
        this.hungerSystem = hungerSystem;
        mqttClient = new MqttClient(BROKER, CLIENT_ID);
        mqttClient.setCallback(this);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setKeepAliveInterval(60);
        options.setAutomaticReconnect(true);
        mqttClient.connect(options);
    }

    public void setHungerListener(HungerListener listener) {
        hungerListener = listener;
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
        System.out.println("Hunger = " + payload + "%");


        if (hungerListener != null) {
            hungerListener.updateHunger(hungerSystem.getHunger());
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost: " + cause.getMessage());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}