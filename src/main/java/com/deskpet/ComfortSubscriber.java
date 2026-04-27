package com.deskpet;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ComfortSubscriber implements MqttCallback {
    private IMqttClient MqttClient;
    private final static String BROKER = "tpc://localhost:1883";
    private final static String CLIENT_ID = "comfort-subscriber";
    private final static String TOPIC = "deskpet/comfort";


    // Constructor
    public ComfortSubscriber() throws  MqttException {
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


    // Subscriber methods
    public void start() throws MqttException {
        MqttClient.subscribe(TOPIC);

        // Debugging lines
        // System.out.println("Listening to: " + TOPIC);
        // System.out.println("Waiting for happiness values...");
        // System.out.println("Press Ctrl+C to stop");
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
