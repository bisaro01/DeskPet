package com.deskpet.mqtt;

import com.deskpet.domain.PetDisplayState;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class HappinessPublisher {
    private IMqttClient mqttClient; // MQTT client instance
    private final static String BROKER = "tcp://localhost:1883"; // If on another computer, use IP instead
    private final static String CLIENT_ID = "happiness-publisher"; // Client name
    private final static String TOPIC = "deskpet/display/state"; // Topic we want to send to

    // Constructor, runs when we create a new HappinessPublisher
    public HappinessPublisher() throws MqttException {
        mqttClient = new MqttClient(BROKER, CLIENT_ID);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setKeepAliveInterval(60); // Sets interval to 60 seconds

        mqttClient.connect(options);
        System.out.println("Connected to MQTT broker!");
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

    public void publish(PetDisplayState state) throws MqttException {
        System.out.println("APP PUBLISHING: " + state.getMoodId());
        System.out.println("APP PUBLISHING: " + state.getIssueFlags());

        MqttMessage msg = new MqttMessage(state.toPayload().getBytes());

        mqttClient.publish(TOPIC, msg);
    }
}