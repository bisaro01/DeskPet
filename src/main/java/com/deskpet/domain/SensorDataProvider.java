package com.deskpet.domain;

import java.sql.Timestamp;

/* Assumes Class SensorData exists
Assumes SensorData has following functions:
- getTemperature(), returns double
- getPetId(), returns long
- getRecordedAt(), returns time and date
- getHumidity(), returns double
 */
public interface SensorDataProvider {
    double getTemperature();
    long getPetId();
    Timestamp getRecordedAt();
    double getHumidity();
}
