package com.dbtool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.sql.*;
import java.util.List;

public class DatabaseManagerTest {
    private DatabaseManager dbManager;

    @BeforeEach
    void setUp() {
        dbManager = new DatabaseManager();
    }

    @AfterEach
    void tearDown() {
        if (dbManager != null) {
            dbManager.disconnect();
        }
    }

    @Test
    void testInitialization() {
        assertNotNull(dbManager);
        assertFalse(dbManager.isConnected());
    }

    @Test
    void testConnectionInfoWhenDisconnected() {
        String info = dbManager.getConnectionInfo();
        assertTrue(info.contains("Not connected"));
    }
}