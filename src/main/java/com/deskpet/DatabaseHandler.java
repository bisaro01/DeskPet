package com.deskpet;
// Imports might have to change depending on file structure
// Assumes following classes exists: SensorData
import com.deskpet.SensorDataProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

// Handles database interactions
/* Assumes Class SensorData exists.
Validation must occur externally.
Assumes SensorData has following functions:
- getTemperature(), returns double
- getPetId(), returns int?
- getRecordedAt(), returns time and date
- getHumidity(), returns double
 */
public class DatabaseHandler {
    // Inserts all sensor values
    public  void insertSensorData(SensorDataProvider sensorData) throws SQLException {
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

}
