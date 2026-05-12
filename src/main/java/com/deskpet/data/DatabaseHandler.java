package com.deskpet.data;
// Imports might have to change depending on file structure
// Assumes following classes exists: SensorData, Pet

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.deskpet.domain.Pet;
import com.deskpet.domain.PetDataProvider;

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
    Connection connection;
    public DatabaseHandler() {
        try {
            connection = Database.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logSensorData(long petId, String sensorType, double value) throws SQLException {
        String sql = """
            INSERT INTO sensor_data (pet_id, sensor_type, value)
            VALUES (?, ?, ?)
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, petId);
            statement.setString(2, sensorType);
            statement.setDouble(3, value);
            statement.executeUpdate();
        }
    }

    // reading historical movement data for displaying
    public java.time.LocalDateTime getLastMovementTime(long petId) throws SQLException {
        String sql = """
            SELECT recorded_at FROM sensor_data
            WHERE pet_id = ? AND sensor_type = 'MOVEMENT'
            ORDER BY recorded_at DESC
            LIMIT 1
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, petId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("recorded_at").toLocalDateTime();
                }
            }
        }
        return null;
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

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

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
    public Pet loadPet(long petId) throws SQLException {
        String sql = """
                SELECT hunger, social, comfort 
                FROM pet_state
                WHERE pet_id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, petId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int hunger = resultSet.getInt("hunger");
                    int social = resultSet.getInt("social");   
                    int comfort = resultSet.getInt("comfort");
                    
                    return new Pet(petId, hunger, social, comfort);
                }
            }
        }
        throw new SQLException("No pet_state found for pet_id = " + petId);
    }
}
