package com.deskpet.mqtt;

import com.deskpet.domain.PetStateListener;
import javafx.application.Platform;
import org.eclipse.paho.client.mqttv3.*;

public class SocialSubscriber implements MqttCallback {


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
