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
    private final static String BROKER = "tcp://localhost:1883";
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


    // ComfortSubscriber start and stop MQTT broker connection methods
    public void start() throws MqttException {
        MqttClient.subscribe(TOPIC);

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
        System.out.println("Humidity&Temp = " + payload);

        // TODO: Split and pass data to the comfort system once Pet class available
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}
