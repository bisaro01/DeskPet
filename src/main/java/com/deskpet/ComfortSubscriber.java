package com.deskpet;

// talking directly to the dashboard through the comfortListener same as Happiness system
import com.deskpet.domain.ComfortListener;
import com.deskpet.domain.HappinessListener;
import com.deskpet.models.systems.ComfortSystem;

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

    // talking directly to the dashboard through the comfortListener same as Happiness system
    private ComfortSystem comfortSystem;
    private ComfortListener comfortListener;


    // Constructor
    public ComfortSubscriber(ComfortSystem comfortSystem) throws  MqttException {
        // talking directly to the dashboard through the comfortListener same as Happiness system
        this.comfortSystem = comfortSystem;


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

    public void setComfortListener(ComfortListener listener) {
        comfortListener = listener;
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

        // Payload will be a string e.g. ("50.0, 21.0")
        String[] sensorReading = payload.split(",");
        if (sensorReading.length == 2) {
            float humidity = Float.parseFloat(sensorReading[0]);
            float temperature = Float.parseFloat(sensorReading[1]);

            // for debugging when we run Gradle
            System.out.printf("Humidity: %.1f%% | Temp: %.1fC%n", humidity, temperature);

            // pass the readings to the comfort system to calculate the value
            comfortSystem.updateComfort(humidity, temperature);

            // for debugging when we run Gradle
            System.out.printf("Comfort: %.1f%%%n", comfortSystem.getComfort());

            // pass the comfort value to the dashboard
            if (comfortListener != null) {
                comfortListener.updateComfort(comfortSystem.getComfort());
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}
