import time
import paho.mqtt.client as mqtt

state = {
    "happiness": 80.0,
    "hunger": 80.0,
    "temp": 23.0,
    "humidity": 0.4
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

        state["hunger"] -= (0.5 / 60) * delta
        state["hunger"] = max(0, min(100, state["hunger"]))

        client.publish("deskpet/happiness", state["happiness"])
        client.publish("deskpet/hunger", state["hunger"])

        print(f"hunger={state['hunger']:.2f}")
        time.sleep(2)

client.on_connect = on_connect
client.on_message = on_message

client.connect("localhost", 1883)
client.loop_start()

loop()
