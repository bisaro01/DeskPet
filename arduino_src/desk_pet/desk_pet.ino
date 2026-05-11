#include <TFT_eSPI.h>
#include "rpcWiFi.h"
#include <PubSubClient.h>
#include "Grove_Temperature_And_Humidity_Sensor.h"
#include <SPI.h>
#include <MFRC522.h>

// ==================================== CONSTANTS ==============================================
// WiFi login
// *** HIDE THESE IN LATER IMPLEMENTATION ***

// Moa's hotspot
const char* wifiName = "OnePlusNord";
const char* password = "hello123";

// MQTT server
const char* mqttServer = "172.29.73.248";
const int mqttPort = 1883;

// MQTT client instance
WiFiClient wifiClient;
PubSubClient client(wifiClient);


// <<< SENSOR TYPES >>>
#define DHTTYPE DHT11       // Humidity and temperature specifying type DH11


// <<< DEFINED PINS >>>
#define BUZZER WIO_BUZZER   // Inbuilt buzzer
#define DHTPIN 2            // Humidity and temperature sensor pin D2 on WIO diagram
#define RFID_RST_PIN 0      // Change depending on physical build connections
#define RFID_SS_PIN 1       // Change depending on physical build connections
#define SOUND_SENSOR_PIN A3 // Sound sensor pin A3
#define PIRPIN 3            // PIR motion sensor pin D3 on WIO diagram


// <<< OTHER INSTANCES >>>
TFT_eSPI tft;                               // Graphical drawing object
DHT dht(DHTPIN, DHTTYPE);                   // Humidity and temperature object
MFRC522 rfid(RFID_SS_PIN, RFID_RST_PIN);    // RFID object

// RFID cooldown
unsigned long lastRfidReadTime = 0;
const unsigned long RFID_COOLDOWN = 5000;   // 5 second

// PIR cooldown
int lastPirState = LOW;
unsigned long lastPirEvent = 0;
const unsigned long PIR_COOLDOWN = 3000;    // 3 second

unsigned long lastSensorRead = 0;
const unsigned long SENSOR_INTERVAL = 2000;

int mood = 100;
int lastMood = -1; // 0 = sad, 1 = happy

unsigned long lastMqttReconnectAttempt = 0;
const unsigned long MQTT_RETRY_INTERVAL = 1000;

// Noise levels
int previousNoiseDecibels = -1; 
unsigned long lastNoiseReadTime = 0; // Last time noise was read
const unsigned long NOISE_READ_INTERVAL = 3000;  // Read noise level every 3 seconds

// ============================  WiFi & MQTT FUNCTIONS  =======================================

void connectWifi() {
    tft.fillScreen(TFT_WHITE);
    tft.setTextColor(TFT_BLACK);
    tft.setTextSize(2);
    tft.drawString("Connecitng to WiFi...", 40, 100);

    // Begin connection attempt
    WiFi.begin(wifiName, password);

    // Keep trying to connect every 5ms untill successfull
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.println(".");
    }

    // Display sucess message once connected on both serial and WIO
    tft.fillScreen(TFT_WHITE);
    tft.drawString("WiFi connected.", 80, 100);

    Serial.println("WiFi connected");
    Serial.print("IP address: ");
    Serial.println(WiFi.localIP());

    delay(1000);
}

void connectMQTT() {
    // Already connected
    if (client.connected()) {
        return;
    }

    // Retry only once per second
    if (millis() - lastMqttReconnectAttempt < MQTT_RETRY_INTERVAL) {
        return;
    }

    lastMqttReconnectAttempt = millis();

    Serial.println("Connecting to MQTT...");

    // Unique client ID
    String clientId = "WioTerminal-" + String(random(0xffff), HEX);

    if (client.connect(clientId.c_str())) {
        Serial.println("MQTT connected.");

        client.subscribe("app/state/happiness");
    }
    else {
        Serial.print("MQTT Connection failed. State: ");
        Serial.println(client.state());
    }
}


// ===============================  FACIAL FUNCTIONS  =========================================

void happyFace() {
    // Reset background
    tft.fillScreen(TFT_WHITE);

    // Mouth full circle pixel points
    int mouthX = 165;
    int mouthY = 110;
    int radius = 25;
    int thickness = 6;

    // Draw illusion of thick smile by stacking multiple circles
    // With decreasing radius
    for(int i = 0; i < thickness; i++) {
        tft.drawCircle(mouthX, mouthY, radius - i, TFT_BLACK);
    }

    // Cut off top half to make smile
    tft.fillRect(
    mouthX - radius - 2,
    mouthY - radius - 2,
    (radius * 2) + 4,
    radius + 2,
    TFT_WHITE
    );

    // Eyes
    tft.fillCircle(130, 80, 8, TFT_BLACK);
    tft.fillCircle(200, 80, 8, TFT_BLACK);
}

void sadFace() {
    // Reset background
    tft.fillScreen(TFT_WHITE);

    // Full cirlce mouth pixel points
    int mouthX = 165;
    int mouthY = 130;
    int radius = 25;
    int thickness = 6;

    for(int i = 0; i < thickness; i++) {
        tft.drawCircle(mouthX, mouthY, radius - i, TFT_BLACK);
    }

    // Cut off bottom half of mouth
    tft.fillRect(
    mouthX - radius - 2,
    mouthY + 2,
    (radius *2) + 4,
    radius + 2,
    TFT_WHITE
    );

    // Eyes
    tft. fillCircle(130, 80, 8, TFT_BLACK);
    tft. fillCircle(200, 80, 8, TFT_BLACK);
}

void happySound() {
    //TODO: Consider a different design:
    // Array of tones + array of durations
    // startMelody(const int* notes, const int* durations, int length) to start playing a startMelody
    // in loop(): updateMelody(), bool isPlaying
    // in loop(): if (millis() - lastNoteTime >= currentDurations[melodyIndex])
    //     if (melodyIndex >= melodyLength) isPlaying = false, noTone
    //     lastNoteTime = millis()
    //     play next tone
    tone(BUZZER, 1000);
    delay(80);

    tone(BUZZER, 1600);
    delay(80);

    tone(BUZZER, 1200);
    delay(120);

    noTone(BUZZER);
    delay(50);

    tone(BUZZER, 1800);
    delay(100);

    noTone(BUZZER);
}

void sadSound() {
    tone(BUZZER, 1000);
    delay(300);

    tone(BUZZER, 500);
    delay(200);

    tone(BUZZER, 200);
    delay(100);

    noTone(BUZZER);
}

// ===============================  SENSOR FUNCTIONS  ========================================

void readTempHumidity() {
    /*
        Reads the humidity and temperature values trough the DHT11 sensro, and publihses to
        defined MQTT broker.

        - Stores the values in tempHumVal array
        - tempHumVal[0] = humidity
        - tempHumVal[1] = temperature
        - publishes data to MQTT broke on topic "wio/humTemp"
    */

    // Defines array of size 2 for the readings and initialises both as 0
    float tempHumVal[2] = {0};

    // Check if reading didnt fail read the values
    if(!dht.readTempAndHumidity(tempHumVal)) {
        float humidity = tempHumVal[0];
        float temperature = tempHumVal[1];

        // Debugging terminal print
        // Serial.print(humidity);
        // Serial.print("%, ");
        // Serial.print(temperature);
        // Serial.println("C");

        // Publish readings to MQTT broker
        // String payload = String(humidity) + "," + String(temperature);
        String tempStr = String(temperature);
        String humStr = String(humidity);
        client.publish("wio/sensors/temperature", tempStr.c_str());
        client.publish("wio/sensors/humidity", humStr.c_str());
        // client.publish("wio/humTemp", payload.c_str());
    }
    // Otherwise debug on serial
    else {
        Serial.print("Failed to get humidity and temperature values.");
    }
}

void readRfidTag() {
    // Check if cooldown has passed
    if (millis() - lastRfidReadTime < RFID_COOLDOWN) {
        return;
    }

    // Check if a tag is close enough to read
    if (!rfid.PICC_IsNewCardPresent()) {
        return;
    }

    // Read tag
    if (!rfid.PICC_ReadCardSerial()) {
        return;
    }

    String uid = "";

    // Loop through UID
    for (byte i = 0; i < rfid.uid.size; i++) {
        if (rfid.uid.uidByte[i] < 0x10) {
        uid += "0";
        }

        // Add as hecadecimal text
        uid += String(rfid.uid.uidByte[i], HEX);

        // Add ':' between UID sections
        if (i < rfid.uid.size - 1) {
        uid += ":";
        }
    }

    // Make UID uppercase
    uid.toUpperCase();

    // Debugging
    Serial.print("RFID UID: ");
    Serial.println(uid);

    // Publish reading to MQTT
    client.publish("wio/events/food", uid.c_str());

    // Time last read, for cooldown
    lastRfidReadTime = millis();

    // Ends communication with current tag
    // rfid.PICC_HaltA();
    // rfid.PCD_StopCrypto1();
}

void readNoiseLevel() {
    const char* mqttNoiseTopic = "wio/sensors/sound";
    int rawSoundValue = analogRead(SOUND_SENSOR_PIN); // Read raw sound value (0-1023)

    // Map raw value (0-1023) to around 40-100 dB
    int noiseDecibels = map(rawSoundValue, 0, 1023, 40, 100);

    // Ignore readings that happen at the extremes (0-10 and 1013-1023)
    if (rawSoundValue > 10 && rawSoundValue < 1013) {
        if (abs(noiseDecibels - previousNoiseDecibels) > 3) {
            previousNoiseDecibels = noiseDecibels;
            client.publish(mqttNoiseTopic, String(noiseDecibels).c_str());
            Serial.print("Noise level: ");
            Serial.print(noiseDecibels);
            Serial.println(" dB");
        }
    }
}

void callback(char* topic, byte* payload, unsigned int length) {
    String message = "";

    for (int i = 0; i < length; i++) {
        message += (char)payload[i];
    }

    Serial.print("Message received on topic: ");
    Serial.println(topic);
    Serial.print("payload: ");
    Serial.println(message);

    if (strcmp(topic, "app/state/happiness") == 0) {
        mood = message.toInt();
    }
}

void readPirSensor() {
    int currentPirState = digitalRead(PIRPIN);

    // Trigger a publish only if state changes
    if(currentPirState == HIGH && lastPirState == LOW) {

        // Reduce MQTT spam
        if(millis() - lastPirEvent > PIR_COOLDOWN) {
            client.publish("wio/sensors/motion", "1");
            lastPirEvent = millis();
        }
    }
    lastPirState = currentPirState;
}
// ==================================  SET UP STAGE  =========================================
// Main set up
void setup() {
    // <<< PINS / HARDWARE >>>
    pinMode(BUZZER, OUTPUT);
    pinMode(PIRPIN, INPUT);


    // Start serial (Arduino IDE terminal)
    Serial.begin(9600);

    // Begin graphics drawing
    tft.begin();
    tft.setRotation(3);
    // Begin temperature & humidity sensor
    dht.begin();

    // Begin RFID reader
    SPI.begin();
    rfid.PCD_Init();

    // <<< CONNECTIVITY >>>
    // Connect to WiFi
    connectWifi();

    // Connect MQTT
    client.setServer(mqttServer, mqttPort);
    client.setCallback(callback);

    client.setKeepAlive(15);
    client.setSocketTimeout(3);
}

// ===================================  MAIN LOOP =========================================
void loop() {
    // Maintains MQTT connection on every iteration of main loop
    if(!client.connected()) {
        tft.drawString("MQTT: OFF", 10, 10);
        connectMQTT();
    } else {
        tft.drawString("MQTT: ON", 10, 10);
        client.loop();
    }

    // Test code for facial expressions
    // int mood = random(0, 100);

    int currentMood = (mood < 50) ? 0 : 1;

    if (currentMood != lastMood) {
        lastMood = currentMood;

        if (currentMood == 0) {
            sadFace();
            sadSound();
        } else {
            happyFace();
            happySound();
        }
    }

    if (millis() - lastSensorRead > SENSOR_INTERVAL) {
        lastSensorRead = millis();
        // Temperature and humidity test
        readTempHumidity();
    }

    // Read noise sensor
    if (millis() - lastNoiseReadTime > NOISE_READ_INTERVAL) {
        lastNoiseReadTime = millis();
        readNoiseLevel();
    }

    // RFID scanner function
    readRfidTag();
    // PIR motion function
    readPirSensor();

    delay(10);
}