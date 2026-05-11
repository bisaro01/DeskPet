package com.deskpet.mqtt;

import com.deskpet.domain.PetStateListener;
import javafx.application.Platform;
import org.eclipse.paho.client.mqttv3.*;

public class SocialSubscriber implements MqttCallback {
    private IMqttClient MqttClient;
    private final static String BROKER = "tcp://localhost:1883";
    private final static String CLIENT_ID = "social-subscriber";
    private final static String MOTION_TOPIC = "wio/sensors/motion";
    private final static String SOUND_TOPIC = "wio/sensors/sound";

    // talking directly to the dashboard through the PetStateListener
    private PetStateListener pet;


    public SocialSubscriber (PetStateListener pet) throws MqttException {
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


    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}
