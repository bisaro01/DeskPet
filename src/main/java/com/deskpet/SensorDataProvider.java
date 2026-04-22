package com.deskpet;

import java.sql.Timestamp;
import java.util.Optional;

/* Assumes Class SensorData exists
Assumes SensorData has following functions:
- getTemperature(), returns double
- getPetId(), returns int?
- getRecordedAt(), returns time and date
- getHumidity(), returns double
 */
public interface SensorDataProvider {
    double getTemperature();
    long getPetId();
    Timestamp getRecordedAt();
    double getHumidity();
}
