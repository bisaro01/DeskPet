package com.deskpet;
// import com.deskpet.Database; might have to be changed depending on file structure
import com.deskpet.Database;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class DatabaseTest {
    @Test
    void testDatabaseConnection() {
        try (Connection connection = Database.getConnection()) {
            assertNotNull(connection);
            System.out.println("Database connection successful");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to connect to database");
        }
    }
}