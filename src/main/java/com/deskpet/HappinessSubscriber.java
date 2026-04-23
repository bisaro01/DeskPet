package com.deskpet;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class HappinessSubscriber implements MqttCallback { // MqttCallback handles incoming messages 
    private IMqttClient mqttClient; // MQTT client instance
    private final static String BROKER = "tcp://localhost:1883"; // If on another computer, use IP instead
    private final static String CLIENT_ID = "happiness-subscriber"; // Client name
    private final static String TOPIC = "deskpet/happiness"; // Topic we want to listen to

    // This is where the program starts
//    public static void main(String[] args) {
//        try {
//            // Create the subscriber and start it
//            HappinessSubscriber subscriber = new HappinessSubscriber();
//            subscriber.start();
//        } catch (MqttException e) { // Triggered when MQTT connection fails
//            System.out.println("Failed to start: " + e.getMessage());
//        }
//    }

    // Constructor, creates a new HappinessSubscriber
    public HappinessSubscriber() throws MqttException {
        mqttClient = new MqttClient(BROKER, CLIENT_ID);
        mqttClient.setCallback(this); // Client uses this class to handle messages

        MqttConnectOptions options = new MqttConnectOptions();
        options.setKeepAliveInterval(60);
        options.setAutomaticReconnect(true);

        mqttClient.connect(options);
        System.out.println("Connected to MQTT broker!");
    }

    // This is where it starts listening for messages
    public void start() throws MqttException {
        mqttClient.subscribe(TOPIC);

        System.out.println("Listening to: " + TOPIC);
        System.out.println("Waiting for happiness values...");
        System.out.println("Press Ctrl+C to stop");

        // Keep the program running
//        while (true) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                break;
//            }
//        }
    }

    // Runs when a message arrives
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload()); // Convert message to string
        System.out.println("Happiness = " + payload + "%");
    }

    // Runs when connection is lost
    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost: " + cause.getMessage());
    }

    // This is required but we don't need it for subscribing
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}