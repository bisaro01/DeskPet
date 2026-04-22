package com.deskpet;
// Imports might have to change depending on file structure
// Assumes following classes exists: SensorData, Pet

import com.deskpet.SensorDataProvider;
import com.deskpet.PetDataProvider;

import java.sql.*;
import java.time.LocalDateTime;

// Handles database interactions.
/* Assumes Class SensorData exists.
Validation must occur externally.
Assumes SensorData has following functions:
- getTemperature(), returns double
- getPetId(), returns long
- getRecordedAt(), returns time and date
- getHumidity(), returns double
 */
public class DatabaseHandler {
    // Inserts all sensor values
    public void insertSensorData(SensorDataProvider sensorData) throws SQLException {
        String sql = """
                INSERT INTO sensor_data (pet_id, senor_type, value, recorded_at)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Writes temperature values into the database.
            statement.setLong(1, sensorData.getPetId());
            statement.setString(2, "TEMPERATURE");
            statement.setDouble(3, sensorData.getTemperature());
            statement.setTimestamp(4, Timestamp.valueOf(sensorData.getRecordedAt().toLocalDateTime()));
            statement.executeUpdate();

            // Writes humidity values into the database.
            statement.setLong(1, sensorData.getPetId());
            statement.setString(2, "HUMIDITY");
            statement.setDouble(3, sensorData.getHumidity());
            statement.setTimestamp(4, Timestamp.valueOf(sensorData.getRecordedAt().toLocalDateTime()));
            statement.executeUpdate();

            //TODO: Write noise values into the database (Version 2)
            //TODO: Write movement values into the database (Version 2)
        }
    }

    // Inserts a feeding event.

    public void insertFeedingEvent(long petId, long foodId, LocalDateTime fedAt) throws SQLException {
        String sql = """
                INSERT INTO feeding_log (pet_id, food_id, fed_at)
                VALUES (?, ?, ?)
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, petId);
            statement.setLong(2, foodId);
            statement.setTimestamp(3, Timestamp.valueOf(fedAt));
            statement.executeUpdate();
        }
    }

    // Updates the current pet_state.
    /* Assumes Class Pet exists.
    Validation must occur externally.
    Assumes Pet has following functions:
    - getHunger(), returns int
    - getSocial(), returns int
    - getComfort(), returns int
    - getHappiness(), returns int
    - getPetId(), returns long
     */
    public void updatePetState(PetDataProvider pet) throws SQLException {
        String sql = """
                UPDATE pet_state
                SET hunger = ?, social = ?, comfort = ?, happiness = ?, updated_at = CURRENT_TIMESTAMP
                WHERE pet_id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, pet.getHunger());
            statement.setInt(2, pet.getSocial());
            statement.setInt(3, pet.getComfort());
            statement.setInt(4, pet.getHappiness());
            statement.setLong(5, pet.getPetId());
            statement.executeUpdate();
        }
    }

    // Load current pet_state (initial load).
    /*
    Assumes Pet has a constructor: Pet(long petId, int hunger, int social, int comfort)
    */
//    public Pet loadPet(long petId) throws SQLException {
//        String sql = """
//                SELECT hunger, social, comfort, happiness
//                FROM pet_state
//                WHERE pet_id = ?
//                """;
//
//        try (Connection connection = Database.getConnection();
//            PreparedStatement statement = connection.prepareStatement(sql)) {
//
//            statement.setLong(1, petId);
//
//            try (ResultSet resultSet = statement.executeQuery()) {
//                if (resultSet.next()) {
//                    int hunger = resultSet.getInt("hunger");
//                    int social = resultSet.getInt("social");
//                    int comfort = resultSet.getInt("comfort");
//
//                    return new Pet(petId, hunger, social, comfort);
//                }
//            }
//        }
//        throw new SQLException("No pet_state found for pet_id = " + petId);
//    }

    // Get food_id from RFID tag.
    public long getFoodIdFromTag(String rfidTag) throws SQLException {
        String sql = """
                SELECT food_id
                FROM food
                WHERE rfid_tag = ?
                """;

        try (Connection connection = Database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, rfidTag);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("food_id");
                }
            }
        }
        throw new SQLException("No food found for RFID tag: " + rfidTag);
    }
}
