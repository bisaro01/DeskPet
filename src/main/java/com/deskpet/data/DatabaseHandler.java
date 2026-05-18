package com.deskpet.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.deskpet.domain.Pet;
import com.deskpet.domain.PetDataProvider;

// Handles database interactions.
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

    // Load current pet_state and settings (initial load).
    public Pet loadPet(long petId) throws SQLException {
        String sql = """
                SELECT
                    p.name,
                    ps.hunger,
                    ps.social,
                    ps.comfort,
                    pset.temp_min,
                    pset.temp_max,
                    pset.humidity_min,
                    pset.humidity_max,
                    pset.noise_min,
                    pset.noise_max
                FROM pet_state ps
                JOIN pet_settings pset ON ps.pet_id = pset.pet_id
                JOIN pet p ON ps.pet_id = p.pet_id
                WHERE ps.pet_id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, petId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int hunger = resultSet.getInt("hunger");
                    int social = resultSet.getInt("social");
                    int comfort = resultSet.getInt("comfort");

                    String name = resultSet.getString("name");

                    String favouriteFood = getFavouriteFood(petId);

                    Pet pet = new Pet(petId, name, favouriteFood, hunger, social, comfort, this);

                    pet.setTemperatureSettings(resultSet.getInt("temp_min"), resultSet.getInt("temp_max"));

                    pet.setHumiditySettings(resultSet.getInt("humidity_min"), resultSet.getInt("humidity_max"));

                    pet.setNoiseSettings(resultSet.getInt("noise_min"), resultSet.getInt("noise_max"));

                    return pet;
                }
            }
        }

        throw new SQLException("No pet found for pet_id = " + petId);
    }

    // Update the settings in database
    public void updatePetSettings(
            long petId,
            int tempMin,
            int tempMax,
            int humidityMin,
            int humidityMax,
            int noiseMin,
            int noiseMax
    ) throws SQLException {
        String sql = """
        UPDATE pet_settings
        SET temp_min = ?,
            temp_max = ?,
            humidity_min = ?,
            humidity_max = ?,
            noise_min = ?,
            noise_max = ?
        WHERE pet_id = ?
        """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, tempMin);
            statement.setInt(2, tempMax);
            statement.setInt(3, humidityMin);
            statement.setInt(4, humidityMax);
            statement.setInt(5, noiseMin);
            statement.setInt(6, noiseMax);
            statement.setLong(7, petId);
            statement.executeUpdate();
        }
    }

    // Get the first pet_id for loading pet at start
    public long getFirstPetId() throws SQLException {
        String sql = """
        SELECT pet_id
        FROM pet
        ORDER BY pet_id
        LIMIT 1
        """;

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getLong("pet_id");
            }
        }

        throw new SQLException("No pets found in database.");
    }

    // Get list of all pets
    public List<String> getAllPets() throws SQLException {
        String sql = """
        SELECT pet_id, name
        FROM pet
        ORDER BY pet_id
        """;

        List<String> pets = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                pets.add(resultSet.getLong("pet_id") + ": " + resultSet.getString("name"));
            }
        }

        return pets;
    }

    // Create a new pet
    public Pet createPet(String name) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM pet";

        try (PreparedStatement statement = connection.prepareStatement(countSql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next() && resultSet.getInt(1) >= 3) {
                throw new SQLException("Maximum number of pets reached.");
            }
        }

        long petId;

        String petSql = """
        INSERT INTO pet (name)
        VALUES (?)
        RETURNING pet_id
        """;

        try (PreparedStatement statement = connection.prepareStatement(petSql)) {
            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Could not create pet.");
                }

                petId = resultSet.getLong("pet_id");
            }
        }

        String stateSql = """
        INSERT INTO pet_state (pet_id, hunger, social, comfort, happiness)
        VALUES (?, 80, 80, 80, 80)
        """;

        try (PreparedStatement statement = connection.prepareStatement(stateSql)) {
            statement.setLong(1, petId);
            statement.executeUpdate();
        }

        String settingsSql = """
        INSERT INTO pet_settings (
            pet_id, temp_min, temp_max, humidity_min, humidity_max, noise_min, noise_max
        )
        VALUES (?, 18, 24, 30, 50, 30, 100)
        """;

        try (PreparedStatement statement = connection.prepareStatement(settingsSql)) {
            statement.setLong(1, petId);
            statement.executeUpdate();
        }

        return loadPet(petId);
    }

    public void deletePet(long petId) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM pet";

        try (PreparedStatement statement = connection.prepareStatement(countSql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next() && resultSet.getInt(1) <= 1) {
                throw new SQLException("Cannot delete the last pet.");
            }
        }

        deleteFromTable("feeding_log", petId);
        deleteFromTable("sensor_data", petId);
        deleteFromTable("pet_food_preferences", petId);
        deleteFromTable("pet_settings", petId);
        deleteFromTable("pet_state", petId);

        String sql = "DELETE FROM pet WHERE pet_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, petId);
            statement.executeUpdate();
        }
    }

    private void deleteFromTable(String tableName, long petId) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE pet_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, petId);
            statement.executeUpdate();
        }
    }

    public String getFavouriteFood(Long petId) throws SQLException {
        String sql = """
            SELECT food.name AS favourite_food
            FROM pet_food_preferences
            JOIN food ON pet_food_preferences.liked_food = food.food_id
            WHERE pet_food_preferences.pet_id = ?
    """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, petId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    String name = resultSet.getString("favourite_food");
                    return name;
                }
            }
        }
        // Default if no favourite food exists yet
        return "N/A";
    }

    public String getDislikedFood(long petId) throws SQLException {
        String sql = """
        SELECT food.name AS disliked_food
        FROM pet_food_preferences
        JOIN food ON pet_food_preferences.disliked_food = food.food_id
        WHERE pet_food_preferences.pet_id = ?
        """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, petId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("disliked_food");
                }
            }
        }

        return "N/A";
    }

    public Long getFavouriteFoodId(Long petId) throws SQLException {
        String sql = """
            SELECT liked_food AS favourite_food
            FROM pet_food_preferences
            WHERE pet_food_preferences.pet_id = ?
    """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, petId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    Long favouriteFood = resultSet.getLong("favourite_food");
                    return favouriteFood;
                }
            }
        }
        return -1L;
    }

    public Double getHungerGain(String rfidTag) throws SQLException {
        String sql = """
                SELECT rfid_tag, hunger_gain
                FROM food
                WHERE rfid_tag = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, rfidTag);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    double hunger_gain = resultSet.getDouble("hunger_gain");
                    return hunger_gain;
                }
            }
        }
        throw new SQLException("No hunger gain found for rfid_tag = " + rfidTag);
    }

    public Long getFoodId(String rfidTag) throws SQLException {
        String sql = """
                SELECT rfid_tag, food_id
                FROM food
                WHERE rfid_tag = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, rfidTag);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    long food_id = resultSet.getLong("food_id");
                    return food_id;
                }
            }
        }
        throw new SQLException("No food id found for rfid_tag = " + rfidTag);
    }

    public void appendFeedingLog(long petId, long foodId) throws SQLException {
        String sql = """
                    INSERT INTO feeding_log(pet_id, food_id, fed_at, reaction)
                    VALUES (?, ?, CURRENT_TIMESTAMP,?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, petId);
            statement.setLong(2, foodId);

            if (getFavouriteFoodId(petId) == foodId) {
                statement.setString(3, "🤤");
            } else {
                statement.setString(3, "🤢");
            }

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("No feeding log found for pet_id = " + petId);
            }
        }
    }

    public List<String> getFeedingHistory(long petId) throws SQLException {
        List<String> feedingHistory = new ArrayList<>();

        String sql = """
                SELECT food.name, fed_at, reaction
                FROM feeding_log
                JOIN food ON feeding_log.food_id = food.food_id
                WHERE feeding_log.pet_id = ?
                ORDER BY fed_at DESC
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, petId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    String foodName = resultSet.getString("name");
                    LocalDateTime fedAtDt = resultSet.getTimestamp("fed_at").toLocalDateTime();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");
                    String fedAt = fedAtDt.format(formatter);
                    String reaction = resultSet.getString("reaction");

                    feedingHistory.add(foodName + "  " + fedAt + "  " + reaction);
                }
            }
        }
        return feedingHistory;
    }

    public List<String> getFoodStats(Long petId) throws SQLException {
        List<String> foodStats = new ArrayList<>();

        String sql = """
                SELECT food.name, COUNT(*) AS eat_count
                FROM feeding_log
                JOIN food ON feeding_log.food_id = food.food_id
                WHERE feeding_log.pet_id = ?
                GROUP BY food.name
                ORDER BY eat_count DESC
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, petId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    String foodName = resultSet.getString("name");
                    int count = resultSet.getInt("eat_count");
                    foodStats.add(foodName + ": " + count);
                }
            }
        }
        return foodStats;
    }

    public List<String> getAllFood() throws SQLException {
        String sql = """
                SELECT food_id, name
                FROM food
                ORDER BY food_id
                """;

        List<String> foods = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                foods.add(resultSet.getString("food_id") + ": " + resultSet.getString("name"));
            }
        }
        return foods;
    }

    public void updateFoodPreferences(long petId, long likedFoodId, long dislikedFoodId) throws SQLException {
        String sql = """
        INSERT INTO pet_food_preferences (pet_id, liked_food, disliked_food)
        VALUES (?, ?, ?)
        ON CONFLICT (pet_id)
        DO UPDATE SET
        liked_food = EXCLUDED.liked_food,
        disliked_food = EXCLUDED.disliked_food
        """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, petId);
            statement.setLong(2, likedFoodId);
            statement.setLong(3, dislikedFoodId);
            statement.executeUpdate();
        }
    }
}
