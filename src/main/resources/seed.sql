-- Reset all demo data so everyone gets the same database state
TRUNCATE TABLE feeding_log, sensor_data, pet_state, food, pet RESTART IDENTITY CASCADE;

INSERT INTO pet (pet_id, name, created_at)
VALUES (1, 'Bob', CURRENT_TIMESTAMP);

INSERT INTO pet_state (pet_id, hunger, social, comfort, happiness, updated_at)
VALUES (1, 80, 75, 85, 80, CURRENT_TIMESTAMP);

INSERT INTO food (food_id, name, rfid_tag, hunger_gain)
VALUES (1, 'Fish', '2A:88:64:06', 40),
       (2, 'Cookie', '9C:DA:6C:06', 40);

INSERT INTO pet_food_preferences (pet_id, liked_food, disliked_food)
VALUES (1, 1, 2);

INSERT INTO sensor_data (sensor_data_id, pet_id, sensor_type, value, recorded_at)
VALUES (1, 1, 'TEMPERATURE', 21.5, CURRENT_TIMESTAMP - INTERVAL '20 minutes'),
       (2, 1, 'HUMIDITY', 45.0, CURRENT_TIMESTAMP - INTERVAL '20 minutes'),
       (3, 1, 'NOISE', 52.0, CURRENT_TIMESTAMP - INTERVAL '20 minutes'),

       (4, 1, 'TEMPERATURE', 22.0, CURRENT_TIMESTAMP - INTERVAL '10 minutes'),
       (5, 1, 'HUMIDITY', 47.0, CURRENT_TIMESTAMP - INTERVAL '10 minutes'),
       (6, 1, 'NOISE', 60.0, CURRENT_TIMESTAMP - INTERVAL '10 minutes'),

       (7, 1, 'TEMPERATURE', 23.0, CURRENT_TIMESTAMP - INTERVAL '5 minutes'),
       (8, 1, 'HUMIDITY', 44.0, CURRENT_TIMESTAMP - INTERVAL '5 minutes'),
       (9, 1, 'NOISE', 49.0, CURRENT_TIMESTAMP - INTERVAL '5 minutes'),

       (10, 1, 'MOVEMENT', 1, CURRENT_TIMESTAMP - INTERVAL '3 minutes'),
       (11, 1, 'MOVEMENT', 1, CURRENT_TIMESTAMP - INTERVAL '1 minute');

INSERT INTO feeding_log (feeding_id, pet_id, food_id, fed_at)
VALUES (1, 1, 1, CURRENT_TIMESTAMP - INTERVAL '2 hours'),
       (2, 1, 2, CURRENT_TIMESTAMP - INTERVAL '30 minutes');