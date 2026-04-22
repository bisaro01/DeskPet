#include <TFT_eSPI.h>
#include "rpcWiFi.h"
#include <PubSubClient.h>
#include "Grove_Temperature_And_Humidity_Sensor.h"

// ==================================== CONSTANTS ==============================================
// WiFi login
// *** HIDE THESE IN LATER IMLEMENTATION ***
const char* wifiName = "Bahnhof-511514";
const char* password = "R6BXX52FMGFZTX";

// MQTT server
const char* mqttServer = "192.168.1.126";
const int mqttPort = 1883;

// MQTT client instance
WiFiClient wifiClient;
PubSubClient client(wifiClient);


// <<< SENSOR TYPES >>>
#define DHTTYPE DHT11       // humidity and temp specifying type DH11


// <<< DEFINED PINS >>>
#define BUZZER WIO_BUZZER   // inbuilt buzzer
#define DHTPIN 2            // humidity and temp sensor pin D2 on WIO diagram


// <<< OTHER INSTANCES >>>
TFT_eSPI tft;               // graphical drawing object
DHT dht(DHTPIN, DHTTYPE);   // humidity and temp object

// ============================  WiFi & MQTT functions  =======================================

void connectWifi() {
    tft.fillScreen(TFT_WHITE);
    tft.setTextColor(TFT_BLACK);
    tft.setTextSize(2);
    tft.drawString("Connecitng to WiFi...", 40, 100);

    // beginn connection attempt
    WiFi.begin(wifiName, password);

    // keep trying to connect every 5ms untill successfull
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.println(".");
    }

    // display sucess message once connected on both serial and WIO
    tft.fillScreen(TFT_WHITE);
    tft.drawString("WiFi connected.", 80, 100);

    Serial.println("WiFi connected");
    Serial.print("IP address: ");
    Serial.println(WiFi.localIP());

    delay(1000);
}

void connectMQTT() {
    while(!client.connected()) {
        Serial.println("COnnecting to MQTT...");

        if(client.connect("WioTerminal")) {
            Serial.println("MQTT connected.");
        } else {
            Serial.println("MQTT Connection failed.");
            Serial.println(client.state());
            Serial.println("Retrying in 1 second");

            delay(1000);
        }
    }
}

// ===============================  facial functions  =========================================

void happyFace() {
    // reset background
    tft.fillScreen(TFT_WHITE);

    // mouth full circle pixel points
    int mouthX = 165;
    int mouthY = 110;
    int radius = 25;
    int thickness = 6;

    // draw illusion of thick smile by stacking multiple circles
    // with decreasing radius
    for(int i = 0; i < thickness; i++) {
        tft.drawCircle(mouthX, mouthY, radius - i, TFT_BLACK);
    }

    // cut off top half to make smile
    tft.fillRect(
    mouthX - radius - 2,
    mouthY - radius - 2,
    (radius * 2) + 4,
    radius + 2,
    TFT_WHITE
    );

    // eyes
    tft.fillCircle(130, 80, 8, TFT_BLACK);
    tft.fillCircle(200, 80, 8, TFT_BLACK);
}

void sadFace() {
    // reset background
    tft.fillScreen(TFT_WHITE);

    // full cirlce mouth pixel points
    int mouthX = 165;
    int mouthY = 130;
    int radius = 25;
    int thickness = 6;

    for(int i = 0; i < thickness; i++) {
        tft.drawCircle(mouthX, mouthY, radius - i, TFT_BLACK);
    }

    // Cutt off bottom half of mouth
    tft.fillRect(
    mouthX - radius - 2,
    mouthY + 2,
    (radius *2) + 4,
    radius + 2,
    TFT_WHITE
    );

    // eyes
    tft. fillCircle(130, 80, 8, TFT_BLACK);
    tft. fillCircle(200, 80, 8, TFT_BLACK);
}

// ==================================  set up stage  =========================================
void setup() {

}

// ===================================  main loop =========================================
void loop() {

}