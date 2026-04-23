// Test happiness values, not actual ones yet 

package com.deskpet;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class HappinessPublisher {
    private IMqttClient mqttClient; // MQTT client instance
    private final static String BROKER = "tcp://localhost:1883"; // If on another computer, use IP instead
    private final static String CLIENT_ID = "happiness-publisher"; // Client name
    private final static String TOPIC = "deskpet/happiness"; // Topic we want to send to

    // This is where the program starts
//    public static void main(String[] args) {
//        try {
//            HappinessPublisher publisher = new HappinessPublisher();
//            publisher.sendTestMessages(); // Test messages
//
//        } catch (MqttException e) {
//            System.out.println("Failed: " + e.getMessage());
//        }
//    }

    // Constructor, runs when we create a new HappinessPublisher
    public HappinessPublisher() throws MqttException {
        mqttClient = new MqttClient(BROKER, CLIENT_ID);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setKeepAliveInterval(60); // Sets interval to 60 seconds
        
        mqttClient.connect(options);
        System.out.println("Connected to MQTT broker!");
    }

    // Sends test happiness values
    public void sendTestMessages() throws MqttException {
        for (int i = 0; i < 5; i++) {
            int happiness = 60 + i * 10;  // 60, 70, 80, 90, 100
            String payload = String.valueOf(happiness); // Convert message to string

            MqttMessage message = new MqttMessage(payload.getBytes()); // Creates an MQTT message
            mqttClient.publish(TOPIC, message); // Sends the message
            
            System.out.println("Sent: happiness = " + happiness);
            
            // Wait 2 seconds before sending next one
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Disconnect once done
        mqttClient.disconnect();
        System.out.println("All messages sent. Disconnected.");
    }
}