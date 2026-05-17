import time
import random
import pygame
import paho.mqtt.client as mqtt

# ============================================================
# PYGAME SETUP
# ============================================================

pygame.init()

SCREEN_WIDTH = 320
SCREEN_HEIGHT = 240

screen = pygame.display.set_mode((SCREEN_WIDTH, SCREEN_HEIGHT))
clock = pygame.time.Clock()

# ============================================================
# DISPLAY LAYOUT
# ============================================================

ICON_HEIGHT = 40

ICON_REGION = pygame.Rect(0, 0, SCREEN_WIDTH, ICON_HEIGHT)
FACE_REGION = pygame.Rect(0, ICON_HEIGHT, SCREEN_WIDTH, SCREEN_HEIGHT - ICON_HEIGHT)

# ============================================================
# ISSUE FLAGS
# ============================================================

HUNGRY = 0b00000001
HOT    = 0b00000010
COLD   = 0b00000100
HUMID  = 0b00001000
DRY    = 0b00010000
LONELY = 0b00100000

# ============================================================
# PET STATE
# ============================================================

state = {
    "faceId": 2,
    "issues": 0
}

previous_state = {
    "faceId": None,
    "issues": None
}

state_received = False

# ============================================================
# SENSOR STATE
# ============================================================

sensors = {
    "temperature": 24.0,
    "humidity": 48.0,
    "rfid_tag": ["9C:DA:6C:06", "2A:88:64:06"],
    "motion_active": "0",
    "last_motion_time": 0,
    "sound_level": 0,
}

# ============================================================
# MQTT
# ============================================================

client = mqtt.Client()

STATE_TOPIC = "deskpet/display/state"

# ============================================================
# FACE SURFACES
# ============================================================

def create_face_surface(face_id):
    surface = pygame.Surface((FACE_REGION.width, FACE_REGION.height))
    surface.fill("white")

    eye_y = 60

    pygame.draw.circle(surface, "black", (110, eye_y), 8)
    pygame.draw.circle(surface, "black", (210, eye_y), 8)

    # VERY SAD
    if face_id == 0:
        pygame.draw.arc(
            surface,
            "black",
            pygame.Rect(120, 100, 80, 50),
            0,
            3.14,
            6
        )

    # SAD
    elif face_id == 1:
        pygame.draw.arc(
            surface,
            "black",
            pygame.Rect(120, 110, 80, 40),
            0.3,
            2.84,
            6
        )

    # NEUTRAL
    elif face_id == 2:
        pygame.draw.line(surface, "black", (130, 130), (190, 130), 6)

    # HAPPY
    elif face_id == 3:
        pygame.draw.arc(
            surface,
            "black",
            pygame.Rect(120, 100, 80, 50),
            3.14,
            2 * 3.14,
            6
        )

    # VERY HAPPY
    elif face_id == 4:
        pygame.draw.arc(
            surface,
            "black",
            pygame.Rect(110, 90, 100, 70),
            3.14,
            2 * 3.14,
            8
        )

    return surface

faces = {
    0: create_face_surface(0),
    1: create_face_surface(1),
    2: create_face_surface(2),
    3: create_face_surface(3),
    4: create_face_surface(4),
}

# ============================================================
# DRAWING
# ============================================================

def draw_face(face_id):
    screen.fill("white", FACE_REGION)

    face_surface = faces.get(face_id)

    if face_surface:
        screen.blit(face_surface, FACE_REGION.topleft)

    pygame.display.update(FACE_REGION)

def draw_icons(issue_flags):
    screen.fill("lightgray", ICON_REGION)

    x = 10

    def draw_icon(text):
        nonlocal x

        font = pygame.font.SysFont(None, 24)

        text_surface = font.render(text, True, "black")

        screen.blit(text_surface, (x, 10))

        x += 50

    if issue_flags & HUNGRY:
        draw_icon("FOOD")

    if issue_flags & HOT:
        draw_icon("HOT")

    if issue_flags & COLD:
        draw_icon("COLD")

    if issue_flags & HUMID:
        draw_icon("HUMID")

    if issue_flags & DRY:
        draw_icon("DRY")

    if issue_flags & LONELY:
        draw_icon("LONE")

    pygame.display.update(ICON_REGION)

# ============================================================
# DISPLAY UPDATE
# ============================================================

def refresh_display():
    if state["faceId"] != previous_state["faceId"]:
        draw_face(state["faceId"])
        previous_state["faceId"] = state["faceId"]

    if state["issues"] != previous_state["issues"]:
        draw_icons(state["issues"])
        previous_state["issues"] = state["issues"]

# ============================================================
# MQTT CALLBACKS
# ============================================================

def on_connect(client, userdata, flags, rc):
    print("Connected with code:", rc)
    client.subscribe(STATE_TOPIC)

def on_message(client, userdata, msg):
    payload = msg.payload.decode()

    items = payload.split("|")

    state["faceId"] = int(items[0])
    state["issues"] = int(items[1])

    print(f"FaceId: {state['faceId']} ||| Issues: {state['issues']}")

# ============================================================
# SENSOR SIMULATION
# ============================================================

def sensor_sound():
    phase = int(time.time() / 10) % 2

    if phase == 0:
        sensors["sound_level"] = random.randint(41, 80)
    else:
        sensors["sound_level"] = random.randint(10, 40)

    if random.randint(0, 100) < 5:
        sensors["sound_level"] = random.randint(81, 120)

def sensor_movement():
    phase = int(time.time() / 10) % 2

    motion_threshold = 30 if phase == 0 else 5

    if random.randint(0, 100) < motion_threshold:
        sensors["motion_active"] = "1"
        sensors["last_motion_time"] = time.time()
    else:
        sensors["motion_active"] = "0"

def simulate_sensors():
    sensors["humidity"] += random.uniform(-0.2, 0.2)
    sensors["temperature"] += random.uniform(-0.5, 0.5)

    sensor_movement()
    sensor_sound()

# ============================================================
# MQTT PUBLISH
# ============================================================

def mqtt_publish():
    client.publish("deskpet/sensors/temperature", sensors["temperature"])
    client.publish("deskpet/sensors/humidity", sensors["humidity"])
    client.publish("deskpet/sensors/motion", sensors["motion_active"])
    client.publish("deskpet/sensors/sound", sensors["sound_level"])

# ============================================================
# SETUP
# ============================================================

def setup():
    screen.fill("white")

    refresh_display()

    pygame.display.flip()

# ============================================================
# MAIN LOOP
# ============================================================

def loop():
    running = True

    publish_accumulator = 0
    sensor_accumulator = 0

    while running:
        delta = clock.tick(60) / 1000.0

        publish_accumulator += delta
        sensor_accumulator += delta

        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                running = False

        if sensor_accumulator > 0.05:
            simulate_sensors()
            sensor_accumulator = 0

        if publish_accumulator > 3.0:
            mqtt_publish()
            publish_accumulator = 0

        refresh_display()

# ============================================================
# STARTUP
# ============================================================

client.on_connect = on_connect
client.on_message = on_message

client.connect("localhost", 1883)
client.loop_start()

setup()
loop()

pygame.quit()