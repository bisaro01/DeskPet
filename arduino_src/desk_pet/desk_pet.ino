#include <TFT_eSPI.h>
#include "rpcWiFi.h"
#include <PubSubClient.h>
#include "Grove_Temperature_And_Humidity_Sensor.h"
#include <SPI.h>
#include <MFRC522.h>

// ================= WIFI =================
// Moas laptop hotspot (For demo only)
const char* wifiName = "Deskpet";
const char* password = "deskpet123";

// ================= MQTT =================
// IP set-up for demo
const char* mqttServer = "10.42.0.1"; //Moa
const int mqttPort = 1883;
const char* MQTT_CLIENT_ID = "deskpet-wio";

const char* PET_STATE_TOPIC = "deskpet/display/state";

// MQTT client instance
WiFiClient wifiClient;
PubSubClient client(wifiClient);

// ================= SENSOR TYPES =================
#define DHTTYPE DHT11       // Humidity and temperature sensor

// ================= PINS =================
#define BUZZER WIO_BUZZER   // Built-in buzzer
#define DHTPIN 2            // Humidity and temperature sensor
#define RFID_RST_PIN 0      // RFID
#define RFID_SS_PIN 1       // RFID
#define SOUND_SENSOR_PIN A3 // Sound sensor
#define PIRPIN 4            // PIR motion sensor

// ================= INSTANCES =================
TFT_eSPI tft;   // Graphical drawing object
DHT dht(DHTPIN, DHTTYPE);   // Humidity and temperature object
MFRC522 rfid(RFID_SS_PIN, RFID_RST_PIN);    // RFID object

// ================= TIMERS =================
// RFID cooldown
unsigned long lastRfidReadTime = 0;
const unsigned long RFID_COOLDOWN = 1000;

// Movement cooldown
int lastPirState = LOW;
unsigned long lastPirEvent = 0;
const unsigned long PIR_COOLDOWN = 3000;

// Sensor cooldown
unsigned long lastSensorRead = 0;
const unsigned long SENSOR_INTERVAL = 2000;

// Sound cooldown
unsigned long lastNoiseReadTime = 0;
const unsigned long NOISE_READ_INTERVAL = 3000;

// MQTT Reconnection cooldown
unsigned long lastMqttReconnectAttempt = 0;
const unsigned long MQTT_RETRY_INTERVAL = 1000;

// ================= STATE =================
int faceId = 2;
int previousFaceId = -1;

byte issues = 0;
byte previousIssues = 255;

#define FL_HUNGRY (1 << 0)
#define FL_HOT (1 << 1)
#define FL_COLD (1 << 2)
#define FL_HUMID (1 << 3)
#define FL_DRY (1 << 4)
#define FL_LONELY (1 << 5)

int previousNoiseDecibels = -1;

// =============== RENDERING ===============
#define ICON_HEIGHT 40

// ================= SOUND =================
int melodyNotes[5];
int melodyDurations[5];

int melodyLength = 0;
int melodyIndex = 0;

unsigned long lastNoteTime = 0;
bool melodyPlaying = false;

// ================= WIFI =================
void connectWifi() {
    tft.fillScreen(TFT_WHITE);
    tft.setTextColor(TFT_BLACK);
    tft.setTextSize(2);
    tft.drawString("Connecting to WiFi...", 40, 100);

    // Begin connection attempt
    WiFi.begin(wifiName, password);

    // Wait until WiFi is connected
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
    }

    // Display sucess message once connected on both serial and WIO
    tft.fillScreen(TFT_WHITE);
    tft.drawString("WiFi connected.", 80, 100);

    Serial.println("WiFi connected");
    Serial.print("IP address: ");
    Serial.println(WiFi.localIP());
}

// ================= MQTT =================
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

    // Clears TCP sockets before reconnecting
    wifiClient.stop();

    if (client.connect(MQTT_CLIENT_ID)) {
        Serial.println("MQTT connected");
        client.subscribe(PET_STATE_TOPIC);
    }
}

// ================ DISPLAY ===============
int mouthX = 165;
int mouthY = 110;

void drawEyes() {
    // Eyes
    tft.fillCircle(130, 80, 8, TFT_BLACK);
    tft.fillCircle(200, 80, 8, TFT_BLACK);
}

void clearFace() {
    tft.fillRect(0, ICON_HEIGHT, 320, 240 - ICON_HEIGHT, TFT_WHITE);
}

void veryHappyFace() {
    clearFace();
    
    int radius = 35;
    int thickness = 8;

    for (int i = 0; i < thickness; i++) {
        tft.drawCircle(mouthX, mouthY, radius - i, TFT_BLACK);
    }

    // Cut top half of mouth-circle
    tft.fillRect(
        mouthX - radius - 2,
        mouthY - radius - 2,
        radius * 2 + 4,
        radius + 2,
        TFT_WHITE
    );

    drawEyes();
}

void happyFace() {
    clearFace();
    
    int radius = 25;
    int thickness = 6;

    for (int i = 0; i < thickness; i++) {
        tft.drawCircle(mouthX, mouthY, radius - i, TFT_BLACK);
    }

    // Cut top half of mouth-circle
    tft.fillRect(
        mouthX - radius - 2,
        mouthY - radius - 2,
        radius * 2 + 4,
        radius + 2,
        TFT_WHITE
    );
    
    drawEyes();
}

void neutralFace() {
    clearFace();

    // Straight line mouth
    tft.fillRect(135, 115, 60, 6, TFT_BLACK);

    drawEyes();
}

void sadFace() {
    clearFace();

    int radius = 25;
    int thickness = 6;

    int sadY = 135;

    for (int i = 0; i < thickness; i++) {
        tft.drawCircle(mouthX, sadY, radius - i, TFT_BLACK);
    }

    // Cut bottom half of mouth-circle
    tft.fillRect(
        mouthX - radius - 2,
        sadY + 2,
        radius * 2 + 4,
        radius + 2,
        TFT_WHITE
    );

    drawEyes();
}

void verySadFace() {
    clearFace();

    int radius = 35;
    int thickness = 8;

    int sadY = 145;

    for (int i = 0; i < thickness; i++) {
        tft.drawCircle(mouthX, sadY, radius - i, TFT_BLACK);
    }

    // Cut bottom half of mouth-circle
    tft.fillRect(
        mouthX - radius - 2,
        sadY + 2,
        radius * 2 + 4,
        radius + 2,
        TFT_WHITE
    );

    drawEyes();
}

void drawIcons() {
    tft.fillRect(0, 0, 320, ICON_HEIGHT, TFT_LIGHTGREY);

    int x = 10;

    tft.setTextColor(TFT_BLACK);
    tft.setTextSize(2);

    if (issues & FL_HUNGRY) {
        tft.drawString("FOOD", x, 10);
        x += 55;
    }

    if (issues & FL_HOT) {
        tft.drawString("HOT", x, 10);
        x += 45;
    }

    if (issues & FL_COLD) {
        tft.drawString("COLD", x, 10);
        x += 55;
    }

    if (issues & FL_HUMID) {
        tft.drawString("HUMID", x, 10);
        x += 70;
    }

    if (issues & FL_DRY) {
        tft.drawString("DRY", x, 10);
        x += 50;
    }

    if (issues & FL_LONELY) {
        tft.drawString("LONE", x, 10);
    }
}

void drawFace() {
    switch(faceId) {
        case 0:
            verySadFace();
            break;
        case 1:
            sadFace();
            break;
        case 2:
            neutralFace();
            break;
        case 3:
            happyFace();
            break;
        case 4:
            veryHappyFace();
            break;
        default:
            neutralFace();
            break;
    }
}

// ================= MELODY =================
// Update the melody without blocking
void updateSound() {
    if (!melodyPlaying) {
        return;
    }

    if (millis() - lastNoteTime < melodyDurations[melodyIndex]) {
        return;
    }

    lastNoteTime = millis();

    if (melodyNotes[melodyIndex] == 0) {
        noTone(BUZZER);
    } else {
        tone(BUZZER, melodyNotes[melodyIndex]);
    }

    melodyIndex++;

    if (melodyIndex >= melodyLength) {
        melodyPlaying = false;
        noTone(BUZZER);
    }
}

void happySound() {
    melodyNotes[0] = 1000;
    melodyNotes[1] = 1600;
    melodyNotes[2] = 1200;
    melodyNotes[3] = 0;
    melodyNotes[4] = 1800;

    melodyDurations[0] = 80;
    melodyDurations[1] = 80;
    melodyDurations[2] = 120;
    melodyDurations[3] = 50;
    melodyDurations[4] = 100;

    melodyLength = 5;
    melodyIndex = 0;
    melodyPlaying = true;
    lastNoteTime = millis() - melodyDurations[0];

    updateSound();
}

void sadSound() {
    melodyNotes[0] = 1000;
    melodyNotes[1] = 500;
    melodyNotes[2] = 200;

    melodyDurations[0] = 300;
    melodyDurations[1] = 200;
    melodyDurations[2] = 100;

    melodyLength = 3;
    melodyIndex = 0;
    melodyPlaying = true;
    lastNoteTime = millis() - melodyDurations[0];

    updateSound();
}

// ================= TEMP/HUMIDITY =================
void readTempHumidity() {
    // If not connected to MQTT do not attempt to send sensor value
    if (!client.connected()) {return;}

    // Defines array of size 2 for the readings and initialises both as 0
    float tempHumVal[2] = {0};

    // Check if reading didnt fail read the values
    if (!dht.readTempAndHumidity(tempHumVal)) {
        float humidity = tempHumVal[0];
        float temperature = tempHumVal[1];

        // Publish readings to MQTT broker
        String tempStr = String(temperature);
        String humStr = String(humidity);
        client.publish("wio/sensors/temperature", tempStr.c_str());
        client.publish("wio/sensors/humidity", humStr.c_str());
    } else {
        Serial.print("Failed to get humidity and temperature values.");
    }
}

// ================= RFID =================
void readRfidTag() {
    // If not connected to MQTT do not attempt to send sensor value
    if (!client.connected()) {return;}

    if (millis() - lastRfidReadTime < RFID_COOLDOWN) {return;}

    if (!rfid.PICC_IsNewCardPresent()) {return;}

    if (!rfid.PICC_ReadCardSerial()) {return;}

    String uid = "";

    // Loop through UID string
    for (byte i = 0; i < rfid.uid.size; i++) {
        if (rfid.uid.uidByte[i] < 0x10) {
            uid += "0";
        }

        // Add as hexadecimal text
        uid += String(rfid.uid.uidByte[i], HEX);

        // Add ':' between UID sections
        if (i < rfid.uid.size - 1) {
            uid += ":";
        }
    }
    // Make UID uppercase
    uid.toUpperCase();

    // Publish reading to MQTT
    client.publish("wio/events/food", uid.c_str());

    lastRfidReadTime = millis();

    // Ends communication with current tag
    rfid.PICC_HaltA();
    rfid.PCD_StopCrypto1();
}

// ================= SOUND SENSOR =================
void readNoiseLevel() {
    // If not connected to MQTT do not attempt to send sensor value
    if (!client.connected()) {return;}

    const char* mqttNoiseTopic = "wio/sensors/sound";
    int rawSoundValue = analogRead(SOUND_SENSOR_PIN); // Read raw sound value (0-1023)

    // Map raw value (0-1023) to around 40-100 dB
    int noiseDecibels = map(rawSoundValue, 0, 1023, 40, 100);

    // Ignore readings that happen at the extremes (0-10 and 1013-1023)
    if (rawSoundValue > 10 && rawSoundValue < 1013) {
        if (abs(noiseDecibels - previousNoiseDecibels) > 3) {
            previousNoiseDecibels = noiseDecibels;
            client.publish(mqttNoiseTopic, String(noiseDecibels).c_str());
        }
    }
}

// ================= PIR SENSOR =================
void readPirSensor() {
    // If not connected to MQTT do not attempt to send sensor value
    if (!client.connected()) {return;}

    int currentPirState = digitalRead(PIRPIN);

    // Trigger a publish only if state changes
    if (currentPirState == HIGH && lastPirState == LOW) {
        // Reduce MQTT spam
        if (millis() - lastPirEvent > PIR_COOLDOWN) {
            client.publish("wio/sensors/motion", "1");
            lastPirEvent = millis();
        }
    }
    lastPirState = currentPirState;
}

// ================= CALLBACK =================
// Receives happiness value from the backend and stores it for the display happy/sad face logic
void callback(char* topic, byte* payload, unsigned int length) {
    String message = "";

    for (unsigned int i = 0; i < length; i++) {
        message += (char)payload[i];
    }

    if (strcmp(topic, PET_STATE_TOPIC) == 0) {
        int separatorIndex = message.indexOf('|');

        if (separatorIndex == -1) {
            return;
        }

        String facePart = message.substring(0, separatorIndex);
        String issuePart = message.substring(separatorIndex + 1);

        faceId = facePart.toInt();
        issues = (byte)issuePart.toInt();

        Serial.print("Face: ");
        Serial.print(faceId);

        Serial.print(" Issues: ");
        Serial.print(issues);
    }
}

// ================= SETUP =================
void setup() {
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

    // Connect to WiFi
    connectWifi();

    // Connect MQTT
    client.setServer(mqttServer, mqttPort);
    client.setCallback(callback);
    client.setKeepAlive(15);
    client.setSocketTimeout(1);
}

// ================= LOOP =================
void loop() {
    // Maintains MQTT connection on every iteration of main loop
    if (!client.connected()) {
        connectMQTT();
    } else {
        client.loop();
        updateSound();
    }

    if (faceId != previousFaceId) {
        previousFaceId = faceId;

        drawFace();

        if (faceId <= 1) {
            sadSound();
        } else if (faceId >= 3) {
            happySound();
        }
    }

    if (issues != previousIssues) {
        previousIssues = issues;

        drawIcons();
    }

    // Read temperature and humidity
    if (millis() - lastSensorRead > SENSOR_INTERVAL) {
        lastSensorRead = millis();
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