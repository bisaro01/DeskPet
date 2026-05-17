CREATE TABLE IF NOT EXISTS pet (
    pet_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS pet_state (
    pet_id BIGINT PRIMARY KEY REFERENCES pet(pet_id),
    hunger INTEGER NOT NULL CHECK (hunger BETWEEN 0 AND 100),
    social INTEGER NOT NULL CHECK (social BETWEEN 0 AND 100),
    comfort INTEGER NOT NULL CHECK (comfort BETWEEN 0 AND 100),
    happiness INTEGER NOT NULL CHECK (happiness BETWEEN 0 AND 100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS sensor_data (
    sensor_data_id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL REFERENCES pet(pet_id),
    sensor_type VARCHAR(20) NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_sensor_type CHECK (sensor_type IN ('TEMPERATURE', 'HUMIDITY', 'NOISE', 'MOVEMENT'))
    );

CREATE TABLE IF NOT EXISTS food (
    food_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    rfid_tag VARCHAR(100) UNIQUE NOT NULL,
    hunger_gain INTEGER DEFAULT 40
    );

CREATE TABLE IF NOT EXISTS pet_food_preferences (
    pet_id BIGSERIAL PRIMARY KEY REFERENCES pet(pet_id),
    liked_food BIGSERIAL REFERENCES food(food_id),
    disliked_food BIGSERIAL REFERENCES food(food_id)
    );

CREATE TABLE IF NOT EXISTS feeding_log (
    feeding_id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL REFERENCES pet(pet_id),
    food_id BIGINT NOT NULL REFERENCES food(food_id),
    fed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reaction VARCHAR(50)
    );

CREATE TABLE IF NOT EXISTS pet_settings (
    pet_id BIGINT PRIMARY KEY REFERENCES pet(pet_id),
    temp_min INTEGER DEFAULT 18 CHECK (temp_min BETWEEN 0 AND 50),
    temp_max INTEGER DEFAULT 24 CHECK (temp_max BETWEEN 0 AND 50),
    humidity_min INTEGER DEFAULT 30 CHECK (humidity_min BETWEEN 20 AND 90),
    humidity_max INTEGER DEFAULT 50 CHECK (humidity_max BETWEEN 20 AND 90),
    noise_min INTEGER DEFAULT 30 CHECK (noise_min BETWEEN 0 AND 100),
    noise_max INTEGER DEFAULT 100 CHECK (noise_max BETWEEN 0 AND 100),

    CHECK (temp_min < temp_max),
    CHECK (humidity_min < humidity_max),
    CHECK (noise_min < noise_max)
    );