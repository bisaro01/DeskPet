package com.deskpet.data;
// Imports might have to change depending on file structure
// Assumes following classes exists: SensorData, Pet

import com.deskpet.domain.PetDataProvider;
import com.deskpet.domain.Pet;

import java.sql.*;

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
            statement.setInt(2, 100); // TODO: Change back to pet.getSocial(); in sprint 2
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
                SELECT hunger, comfort 
                FROM pet_state
                WHERE pet_id = ?
                """;

        try (
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, petId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int hunger = resultSet.getInt("hunger");
                    // int social = resultSet.getInt("social"); TODO: Add back in sprint 2
                    int comfort = resultSet.getInt("comfort");

                    return new Pet(petId, hunger, comfort);
                }
            }
        }
        throw new SQLException("No pet_state found for pet_id = " + petId);
    }
}
