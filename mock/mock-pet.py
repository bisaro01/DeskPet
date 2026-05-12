import time
import paho.mqtt.client as mqtt
import random

state = {
    "happiness": 80.0,
    "hunger": 80.0,
    "temp": 24.0,
    "humidity": 48.0,
    "rfid_tag": "9C:DA:6C:06",
    "motion_active": False,
    "last_motion_time": 0,
}

client = mqtt.Client()

def on_connect(client, userdata, flags, rc):
    print("Connected with code:", rc)
    client.subscribe("app/state/happiness")

def on_message(client, userdata, msg):
    topic = msg.topic
    payload = msg.payload.decode()

    if topic == "app/state/happiness":
        try:
            state["happiness"] = float(payload)
            print("Happiness updated from app:", state["happiness"])
        except ValueError:
            pass

    elif topic == "deskpet/feed":
        state["hunger"] = min(100, state["hunger"] + 20)

def loop():
    last_time = time.time()
    loop_count = 0

    while True:
        now = time.time()
        delta = now - last_time
        last_time = now
        loop_count += 1

        # ====================== OTHER SENSORS TESTING ==============================
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
            client.publish("wio/events/food", state["rfid_tag"])
        client.publish("wio/sensors/temperature", state["temp"])
        client.publish("wio/sensors/humidity", state["humidity"])

        # ========================= MOTION TESTING =================================
        # Every 10 loops alternate between active (30% chance) and quiet (5% chance)
        in_active_period = (loop_count // 10) % 2 == 0
        motion_threshold = 30 if in_active_period else 5
        if random.randint(0, 100) < motion_threshold:
            state["motion_active"] = True
            state["last_motion_time"] = now
            print("MOCK: motion detected")
            client.publish("wio/sensors/motion", "1")


        # ========================= SOUND TESTING =================================
        in_loud_period = (loop_count // 15) % 2 == 0
        if in_loud_period:
            noise = random.randint(50, 80)   # within range
            sound_label = "normal"
        else:
            noise = random.randint(10, 35)   # below threshold — no social gain
            sound_label = "quiet"

        # Occasionally spike very loud
        if random.randint(0, 100) < 5:
            noise = random.randint(101, 120)
            sound_label = "TOO LOUD"

        client.publish("wio/sensors/sound", str(noise))



        # ========================= Print statements =================================
        motion_status = "ACTIVE" if (now - state["last_motion_time"]) < 30 else "idle"
        print(f"--- loop {loop_count} ---")
        print(f"MOCK PET: hunger={state['hunger']:.2f}")
        print(f"MOCK PET: happiness={state['happiness']:.2f}")
        print(f"MOCK PET: humidty={state['humidity']:.2f}, temp={state['temp']:.2f}")
        print(f"  motion={motion_status}  sound={noise}dB ({sound_label})")

        time.sleep(2)

client.on_connect = on_connect
client.on_message = on_message

client.connect("localhost", 1883)
client.loop_start()

loop()
