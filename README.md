# Desk Pet

A desk pet companion that responds to its physical environment trough hardware sensors.
Your Desk Pet lives on a [Wio Terminal](https://wiki.seeedstudio.com/Wio-Terminal-Getting-Started/) and a [JavaFX](https://openjfx.io/) desktop application it reacts in real time to movement, sound, temperature, humidity and feeding events.


## Purpose and Benefits

Other productivity tools are purely functional the Desk Pet adds a layer of personality to your workspace by providing a physical device to a digital companion some of the benefits:

- **Engagement** - a physical pet that reacts to you and your environment and encourages you to maintain a comfortable workspace
- **Awareness** - real time feedback on hunger, temperature, humidity, movement and noise levels
- **Persistence** - the pets state saves between sessions so neglect has real consequences
- **Customization** - adjustable thresholds allowing for personal customization for what are comfortable levels


### Pets Needs
| Need | Affected By |
| ---- | ------------- |
| Hunger | RFID Food tags  |
| Comfort | Temperature & Humidity sensor |
| Social | PIR motion & Microphone sensors |
| Happiness | Calculated from all above needs |

## Demo Video
[Watch the demo here](<link>)

## Team Contributions
| Member   | Contributions |
|----------|--------------|
| Natalija | Created the initial Arduino baseline script loop, including core libraries, facial expressions, noise functions, wifi connectivity, and MQTT setup on the Wio, which the team built upon throughout the project. Owned the comfort system end-to-end hardware wiring, Arduino implementation, and logic for publishing temperature and humidity sensor readings via MQTT. Implemented backend logic for the comfort system affecting the Pet, ensuring the readings were saved in the database, as well as the UI presentation of the comfort progress bar. Assisted with code refactoring following the system overhaul, improving readability and removing redundant code. Collaborated on the social system, specifically motion-related functionality. Integrating the motion sensor from hardware through to the backend and database, and implemented the historical data view showing the last detected movement in the UI.|
| Lovisa   | Developed the base setup for the desktop UI so that the user can monitor the pet’s need levels in real time as they change. Implemented the hunger and feeding system, including retrieving hunger data from the database, increasing hunger level when an RFID tag is detected and updating the UI dynamically. Created the food preferences functionality, allowing pet profiles to store liked and disliked food, differentiate between different food types through the RFID tags, and apply different hunger increase values depending on the read food type. Additionally, implemented functionality for saving feeding events to the database and displaying feeding history, food preferences, feeding counts, timestamps, and pet redactions in the desktop interface. Also contributed to displaying the pet’s hunger, comfort, and happiness levels in the desktop app. |
| Sophie   | Set up MQTT backend communication (HappinessPublisher, HappinessSubscriber), created the base code for sad/happy face. Created the base for the Pet class with the applyDecay, updateNeed, and updateState functions. Implemented the sound sensor integration. Collaborated on the logic of the SocialSystem, specifically sound-related functionality (sound thresholds, calculation of social needs). Added the time of the last detected sound to the dashboard. Integrated social needs into the happiness calculation.|
| Moa      | Created the database schema, connection class, set-up script, and seed data script, as well as the README instructions for the database set-up. Wrote the Arduino code for the RFID scanner. Made the Arduino set-up script. Solved the MQTT connection and delay issues. Updated the WIO happy/sad sounds to be non-blocking. Created the settings page where the user can edit environment ranges and food preferences, create and delete pets. Authored the build script for the WIO sketch.|
| Mark     | Created a mock of the WIO with Python and MQTT to enable independent development when a member was not in possession of the WIO terminal. Connected the desktop application, MQTT subscribers to the domain model, database, and frontend application. Added MQTT publishing from desktop application to WIO terminal, and added code to WIO sketch to interpret Pet state and manage display of face and icons. Wrote readme guide for setting up and running the project. Setup Pet maintenance loop architecture to update Pet state continuously. Setup Continuous Integration with GitLab Runner, as well as the build script for Java/JavaFX. |


## Pre-requisites

1. **Java** (version 21+)
2. **PostgreSQL** (version 13+)
3. **MQTT Broker** (Mosquitto on 'localhost:1883')

See the below installation guide per OS.


### Pre-requisites installation

1. Java (version 21+)
    - Ensure installed:
      - Arch
      ```bash
        sudo pacman -S jdk21-openjdk
      ```
      - Ubuntu
        ```bash
        sudo apt install openjdk-21-jdk
        ```
      - MacOS
        ```bash
        brew install openjdk@21
        ```
      - Windows
        - Windows<br>
        Download installer from [official website](https://www.oracle.com/java/technologies/downloads/?utm_source=chatgpt.com)

      - Verify installation:
        ```bash
          java --version
        ```
        - Expected output
        ```bash
          openjdk 21
        ```
        or
        ```bash
          java version "21"
        ```

2. PostgreSQL (version 13+ recommended)
    - Ensure installed
      - Arch
        ```bash
        sudo pacman -S postgresql
        ``` 
      - Ubuntu
        ```bash
        sudo apt install postgresql postgresql-contrib
        ```
      - MacOS
        ```bash
        brew install postgresql
        ```
      - Windows<br>
        Download installer from [official website](https://www.postgresql.org/download/)
    - Ensure postgreSQL server is running
      - Arch & Ubuntu
        ```bash
        sudo systemctl start postgresql
        ``` 
      - MacOS
        ```bash
        brew services start postgresql
        ``` 
      - Windows<br>
        Starts automatically (or use Services panel)
    - Create a postgres user with a password (**password cannot be empty**)
      - Open PostgreSQL shell
        - Create user<br> 
         _Default: user 'postgre', password '123', database 'deskpet'_
          ```sql
          CREATE USER youruser WITH PASSWORD 'yourpassword';
          ``` 
        - Change password
          ```sql
          ALTER USER youruser WITH PASSWORD 'newpassword';
          ```
    - `src/main/resources/config.properties` exists and contains:
        - username
        - password
        - database name

3. MQTT Broker (Mosquitto)
    - Ensure installed
        - Arch
            ```bash
            sudo pacman -S mosquitto
            ```
        - Ubuntu
            ```bash
            sudo apt install mosquitto mosquitto-clients
            ```
        - MacOS
            ```bash
            brew install mosquitto
            ```
        - Windows<br>
            Download installer from [official website](https://mosquitto.org/download)
    - Broker must be running on:
        - host: `localhost`
        - port: `1883`


## Getting started

### Linux/MacOS

In a terminal:

1. Clone the repository:
    ```bash
    git clone <repo-url>
    cd <repo>
    ```
2. Set up config:
    ```bash
    cp src/main/resources/config.properties.example src/main/resources/config.properties
    ```
    Edit `config.properties` with your database credentials as explained in prerequisites installation.

3. Set up database:<br>
   From project root
     ```bash
     sh src/main/scripts/db-setup.sh
     ```

4. Build the project:
    ```bash
    ./gradlew build
    ```

### Windows (PowerShell)

1. Clone the repository:
    ```powershell
    git clone <repo-url>
    cd <repo>
    ```

2. Set up config:
```powershell
    copy src\main\resources\config.properties.example src\main\resources\config.properties
```
    Edit `config.properties` with your database credentials as explained in prerequisites installation.

3. Set up database:<br>
   From project root
    ```bash
    powershell -ExecutionPolicy Bypass -File src/main/scripts/db-setup.ps1
    ```

4. Build the project:
    ```powershell
    .\gradlew.bat build
    ```


PowerShell may block script execution:

```powershell
Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned
```

## Running

Start in order in separate terminals

**1. PostgreSQL**
  ```bash
    sudo systemctl start postgresql   # Linux
    brew services start postgresql    # MacOS
    # Windows: starts automatically
  ```

**2. MQTT Broker**
  ```bash
    mosquitto -v          # Linux/MacOS
    mosquitto.exe -v      # Windows
  ```

**3. Application**
  ```bash
    ./gradlew run         # Linux/macOS
    .\gradlew.bat run     # Windows
  ```

## Verify setup

Run tests to confirm everything is working:
  ```bash
    ./gradlew test        # Linux/macOS
    .\gradlew.bat test    # Windows
  ```


# Project Structure

## Hardware
- **Wio Terminal** - main microcontroller and display
- **DHT11** - temperature and humidity
- **MFRC522 RFID** - food tag reader
- **Mini PIR motion sensor** - motion detector
- **Sound Sensor** — sound level detection

## MQTT Topics
| Topic | Direction | Description |
|-------|-----------|-------------|
| `wio/sensors/temperature` | Wio → App | Temperature in °C |
| `wio/sensors/humidity` | Wio → App | Humidity % |
| `wio/sensors/motion` | Wio → App | PIR trigger (`"1"`) |
| `wio/sensors/sound` | Wio → App | Noise level in dB |
| `wio/events/food` | Wio → App | RFID tag UID |
| `deskpet/happiness` | App → Wio | Happiness 0–100 |


## Database Schema
- `pet` — pet identity
- `pet_state` — current hunger, social, comfort, happiness (updated every 3s)
- `pet_settings` — per-pet environment thresholds
- `sensor_data` — historical sensor readings (TEMPERATURE, HUMIDITY, NOISE, MOVEMENT)
- `food` — known RFID food tags
- `feeding_log` — feeding history


## Authors
Group 1 - DIT113, University of Gothenburg, 2026
