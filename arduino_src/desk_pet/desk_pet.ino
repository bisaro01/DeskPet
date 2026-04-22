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


void setup() {

}

void loop() {

}