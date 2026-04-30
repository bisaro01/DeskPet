import time
import paho.mqtt.client as mqtt
import random

state = {
    "happiness": 80.0,
    "hunger": 80.0,
    "temp": 24.0,
    "humidity": 48.0,
    "rfid_tag": "9C:DA:6C:06",
}

client = mqtt.Client()

def on_connect(client, userdata, flags, rc):
    print("Connected with code:", rc)
    client.subscribe("deskpet/#")

def on_message(client, userdata, msg):
    topic = msg.topic
    payload = msg.payload.decode()

    if topic == "deskpet/happiness":
        try:
            state["happiness"] = float(payload)
            print("Happiness updated from app:", state["happiness"])
        except ValueError:
            pass

    elif topic == "deskpet/feed":
        state["hunger"] = min(100, state["hunger"] + 20)

def loop():
    last_time = time.time()

    while True:
        now = time.time()
        delta = now - last_time
        last_time = now

        state["hunger"] -= (30 / 60) * delta
        state["hunger"] = max(0, min(100, state["hunger"]))
        state["happiness"] -= (30 / 60) * delta
        state["happiness"] = max(0, min(100, state["happiness"]))

        # Simulated humidity and temp tests
        state["humidity"] += random.uniform(-0.2, 0.2)
        state["temp"] += random.uniform(-0.5, 0.5)

        # Sends signals at random that a rfid tag has been registered
        value = random.randint(0, 100)
        if value < 10:
            client.publish("deskpet/rfid_tag", state["rfid_tag"])

        client.publish("deskpet/happiness", state["happiness"])
        client.publish("deskpet/hunger", state["hunger"])
        client.publish("deskpet/comfort", f"{state["humidity"]},{state["temp"]}")

        print(f"MOCK PET: hunger={state['hunger']:.2f}")
        print(f"MOCK PET: happiness={state['happiness']:.2f}")
        print(f"MOCK PET: humidty={state['humidity']:.2f}, temp={state['temp']:.2f}")
        time.sleep(2)

client.on_connect = on_connect
client.on_message = on_message

client.connect("localhost", 1883)
client.loop_start()

loop()
