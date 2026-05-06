#!/bin/bash

# Update apt
echo "Updating apt..."
apt-get update
cd ~

# Install Arduino CLI
echo "Installing Arduino CLI"
apt-get install curl -y
curl -fsSL https://raw.githubusercontent.com/arduino/arduino-cli/master/install.sh | sh
export PATH=$PATH:/root/bin
echo "Arduino CLI version:"
arduino-cli version

# Create config file and install Seeed Studio boards
echo "Create Arduino CLI config..."
echo "board_manager:
  additional_urls:
    - https://files.seeedstudio.com/arduino/package_seeeduino_boards_index.json" > .arduino-cli.yaml

echo "Updating board index..."
arduino-cli core update-index --config-file .arduino-cli.yaml
echo "Installing Seeed SAMD core..."
arduino-cli core install Seeeduino:samd --config-file .arduino-cli.yaml

# Install libraries
echo "Installing libraries..."
arduino-cli lib install "PubSubClient"
arduino-cli lib install "MFRC522"
arduino-cli lib install "Grove Temperature And Humidity Sensor"
arduino-cli lib install "Seeed Arduino rpcWiFi"

# Verify installation
echo "Installed cores:"
arduino-cli core list

echo "Installed libraries:"
arduino-cli lib list

echo "Arduino CI setup complete."

# Return to previous working directory
cd -



